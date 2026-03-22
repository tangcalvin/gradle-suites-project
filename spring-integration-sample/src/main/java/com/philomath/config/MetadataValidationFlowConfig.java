package com.philomath.config;

import com.philomath.entity.FileMetadata;
import com.philomath.service.FixedLengthValidationService;
import com.philomath.service.MetadataPollingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;

/**
 * Polls DB for metadata with status=DOWNLOADED, atomically claims by updating to
 * PENDING_VALIDATION, then validates each file using fixed-length format and updates
 * to VALIDATED or VALIDATION_FAILED.
 */
@Configuration
public class MetadataValidationFlowConfig {

    @Bean
    public IntegrationFlow metadataValidationFlow(
            MetadataPollingService metadataPollingService,
            FixedLengthValidationService fixedLengthValidationService,
            @Value("${validation.poller-fixed-delay:60000}") long pollerFixedDelay,
            @Value("${validation.max-per-poll:5}") int maxPerPoll) {
        return IntegrationFlow
                .from((MessageSource<Object>) () -> {
                    List<FileMetadata> list = metadataPollingService.pollPendingValidation(maxPerPoll);
                    return list.isEmpty() ? null : new GenericMessage<>(list);
                }, poller -> poller
                        .id("metadataValidationPoller")
                        .poller(Pollers.fixedDelay(pollerFixedDelay)
                                .maxMessagesPerPoll(maxPerPoll)))
                .split()
                .<FileMetadata>handle((payload, headers) -> {
                    fixedLengthValidationService.validate(payload);
                    return null;
                })
                .get();
    }
}
