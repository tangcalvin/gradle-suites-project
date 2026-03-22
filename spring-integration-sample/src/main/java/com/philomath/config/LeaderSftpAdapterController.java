package com.philomath.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * Starts SFTP inbound adapter when this instance becomes leader, stops it when leadership is revoked.
 */
@Slf4j
@Component
@DependsOn("sftpInboundFlow")
public class LeaderSftpAdapterController implements ApplicationListener<org.springframework.integration.leader.event.AbstractLeaderEvent> {

    private final SourcePollingChannelAdapter sftpInboundAdapter;

    public LeaderSftpAdapterController(ApplicationContext context) {
        // SI may register adapter as "sftpInboundAdapter", "sftpInboundFlow.sftpInboundAdapter", or similar
        String[] possibleNames = {"sftpInboundAdapter", "sftpInboundFlow.sftpInboundAdapter", "sftpInboundFlow.sourcePollingChannelAdapter"};
        this.sftpInboundAdapter = Arrays.stream(possibleNames)
                .filter(context::containsBean)
                .map(name -> context.getBean(name, SourcePollingChannelAdapter.class))
                .findFirst()
                .orElseGet(() -> {
                    Map<String, SourcePollingChannelAdapter> adapters = context.getBeansOfType(SourcePollingChannelAdapter.class);
                    if (adapters.isEmpty()) {
                        throw new IllegalStateException("SFTP inbound adapter bean not found. Checked: " + Arrays.toString(possibleNames));
                    }
                    // Prefer bean whose name contains "sftp"
                    return adapters.entrySet().stream()
                            .filter(e -> e.getKey().toLowerCase().contains("sftp"))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(adapters.values().iterator().next());
                });
    }

    @Override
    public void onApplicationEvent(org.springframework.integration.leader.event.AbstractLeaderEvent event) {
        if (event instanceof OnGrantedEvent) {
            log.info("Leadership granted - starting SFTP inbound adapter");
            sftpInboundAdapter.start();
        } else if (event instanceof OnRevokedEvent) {
            log.info("Leadership revoked - stopping SFTP inbound adapter");
            sftpInboundAdapter.stop();
        }
    }
}
