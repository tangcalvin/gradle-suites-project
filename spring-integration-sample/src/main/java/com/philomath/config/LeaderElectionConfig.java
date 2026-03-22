package com.philomath.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.leader.Candidate;
import org.springframework.integration.leader.Context;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.integration.util.UUIDConverter;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Database-backed leader election via Spring Integration.
 * <p>
 * <b>LOCK_KEY</b> (in leader_LOCK): Derived from {@code leader.role}.
 * LockRegistryLeaderInitiator passes role to LockRegistry.obtain(); JdbcLockRegistry converts it via
 * {@code UUIDConverter.getUUID(role)} (deterministic UUID from role string).
 * <p>
 * <b>CLIENT_ID</b> (in leader_LOCK): From {@code leader.client-id} or auto-generated.
 * Set via DefaultLockRepository(DataSource, clientId). Identifies this instance in the lock table.
 */
@Configuration
public class LeaderElectionConfig {

    private static final int LOCK_TTL_SECONDS = 30;

    @Bean
    public String leaderClientIdentifier(@Value("${leader.client-id:}") String clientIdentifier) {
        return (clientIdentifier == null || clientIdentifier.isBlank())
                ? "node-" + UUID.randomUUID().toString().substring(0, 8)
                : clientIdentifier;
    }

    @Bean
    public DefaultLockRepository lockRepository(
            DataSource dataSource,
            @Value("${leader.role:cluster-leader}") String leaderRole,
            String leaderClientIdentifier) {
        DefaultLockRepository lockRepository = new DefaultLockRepository(dataSource, leaderClientIdentifier);
        lockRepository.setRegion(leaderRole);
        lockRepository.setPrefix("leader_");
        return lockRepository;
    }

    @Bean
    public LockRegistry lockRegistry(DefaultLockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository, Duration.ofSeconds(LOCK_TTL_SECONDS));
    }

    @Bean
    public Candidate leaderCandidate(
            @Value("${leader.role:cluster-leader}") String leaderRole,
            String leaderClientIdentifier) {
        return new Candidate() {
            private final AtomicBoolean running = new AtomicBoolean(false);
            private volatile Thread leaderBackgroundThread;

            @Override
            public String getRole() {
                return leaderRole;
            }

            @Override
            public String getId() {
                return leaderClientIdentifier;
            }

            @Override
            public void onGranted(Context context) throws InterruptedException {
                String databaseLockKey = UUIDConverter.getUUID(leaderRole).toString();
                System.out.println("[LEADER] Granted: " + getId() + " is now the leader | DB: LOCK_KEY=" + databaseLockKey + ", CLIENT_ID=" + getId() + ", REGION=" + leaderRole);
                running.set(true);
                leaderBackgroundThread = new Thread(() -> {
                    while (running.get()) {
                        try {
                            System.out.println("[LEADER] " + getId() + " heart-beat as leader");
                            Thread.sleep(5000);
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }, "leader-" + getId());
                leaderBackgroundThread.start();
            }

            @Override
            public void onRevoked(Context context) {
                String databaseLockKey = UUIDConverter.getUUID(leaderRole).toString();
                System.out.println("[LEADER] Revoked: " + getId() + " is no longer the leader | DB: LOCK_KEY=" + databaseLockKey + ", CLIENT_ID=" + getId());
                running.set(false);
                if (leaderBackgroundThread != null) {
                    leaderBackgroundThread.interrupt();
                }
            }
        };
    }

    @Bean
    public LockRegistryLeaderInitiator leaderInitiator(LockRegistry lockRegistry, Candidate leaderCandidate) {
        LockRegistryLeaderInitiator leaderInitiator = new LockRegistryLeaderInitiator(lockRegistry, leaderCandidate);
        leaderInitiator.setAutoStartup(true);
        return leaderInitiator;
    }
}
