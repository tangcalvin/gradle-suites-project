package com.philomath.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.*;

@Configuration
@EnableConfigurationProperties(IntrospectionConfig.KeycloakPrivateJwtProps.class)
public class IntrospectionConfig {

    @Bean
    OpaqueTokenIntrospector opaqueTokenIntrospector(
            OAuth2ResourceServerProperties resourceServerProps,
            KeycloakPrivateJwtProps props
    ) {
        String introspectionUri = resourceServerProps.getOpaquetoken().getIntrospectionUri();
        String clientId = resourceServerProps.getOpaquetoken().getClientId();
        RestTemplate rest = new RestTemplate();
        RSAPrivateKey privateKey = loadPrivateKey(props);
        String audience = props.clientAssertion().audience();

        return (String token) -> {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("token", token);
            form.add("client_id", clientId);
            form.add(
                    "client_assertion_type",
                    "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
            );
            form.add("client_assertion", buildClientAssertionJwt(clientId, audience, props, privateKey));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            @SuppressWarnings("unchecked")
            Map<String, Object> body =
                    rest.postForObject(introspectionUri, new HttpEntity<>(form, headers), Map.class);

            boolean active = body != null && Boolean.TRUE.equals(body.get("active"));
            if (!active)
                throw new IllegalArgumentException("Token is not active");

            // Keycloak introspection commonly returns numeric timestamps (epoch seconds) for exp/iat.
            // Spring/Security code and app code often expects Instant, so normalize here.
            Map<String, Object> attrs = new HashMap<>(Objects.requireNonNull(body));
            normalizeEpochSecondsToInstant(attrs, "exp");
            normalizeEpochSecondsToInstant(attrs, "iat");
            normalizeEpochSecondsToInstant(attrs, "nbf");

            return new DefaultOAuth2AuthenticatedPrincipal(attrs, List.of());
        };
    }

    private void normalizeEpochSecondsToInstant(Map<String, Object> attrs, String key) {
        Object v = attrs.get(key);
        if (v instanceof Number n) {
            attrs.put(key, Instant.ofEpochSecond(n.longValue()));
        }
    }

    private RSAPrivateKey loadPrivateKey(KeycloakPrivateJwtProps props) {
        try (InputStream is = props.keystore().location().getInputStream()) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(is, props.keystore().password().toCharArray());
            PrivateKey key = (PrivateKey) ks.getKey(
                    props.keystore().keyAlias(),
                    props.keystore().keyPassword().toCharArray()
            );
            return (RSAPrivateKey) key;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from keystore.jks", e);
        }
    }

    private String buildClientAssertionJwt(
            String clientId,
            String audience,
            KeycloakPrivateJwtProps props,
            RSAPrivateKey privateKey
    ) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(clientId)
                    .subject(clientId)
                    .audience(audience)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(60)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT);

            // Only set `kid` when you know it matches what Keycloak expects.
            String kid = props.keystore().kid();
            if (kid != null && !kid.isBlank()) {
                headerBuilder.keyID(kid);
            }

            JWSHeader header = headerBuilder.build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build/sign client assertion JWT", e);
        }
    }

    @ConfigurationProperties(prefix = "app.keycloak")
    public record KeycloakPrivateJwtProps(
            ClientAssertion clientAssertion,
            Keystore keystore
    ) {
        public record ClientAssertion(String audience) {
        }

        public record Keystore(
                Resource location,
                String password,
                String keyAlias,
                String keyPassword,
                String kid
        ) {
        }
    }
}