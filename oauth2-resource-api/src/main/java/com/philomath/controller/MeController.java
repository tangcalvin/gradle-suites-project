package com.philomath.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

@RestController
public class MeController {
    @GetMapping("/public/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "ok", true,
                "service", "spring-api",
                "at", Instant.now().toString()
        );
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(Principal principal) {
        return Map.of(
                "principal", principal,
                "at", Instant.now().toString()
        );
    }
}
