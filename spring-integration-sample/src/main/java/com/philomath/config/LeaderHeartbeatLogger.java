package com.philomath.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Logs a heartbeat for non-leader instances to indicate they are still active.
 */
@Slf4j
@Component
public class LeaderHeartbeatLogger {

    private final LockRegistryLeaderInitiator leaderInitiator;
    private final String clientId;

    public LeaderHeartbeatLogger(
            LockRegistryLeaderInitiator leaderInitiator,
            @Qualifier("leaderClientIdentifier") String leaderClientIdentifier) {
        this.leaderInitiator = leaderInitiator;
        this.clientId = leaderClientIdentifier;
    }

    @Scheduled(fixedDelay = 5000)
    public void logNonLeaderHeartbeat() {
        if (leaderInitiator.getContext() == null || !leaderInitiator.getContext().isLeader()) {
            log.info("Non-leader but still active: {}", clientId);
        }
    }
}
