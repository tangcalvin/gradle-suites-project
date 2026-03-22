package com.philomath.config;

import com.philomath.service.FileProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

/**
 * Flow: sftpFilesChannel -> FileProcessorService (writes to NFS + DB).
 * Runs in parallel via ExecutorChannel. Errors handled inside the service (log + DB record).
 */
@Configuration
public class FileProcessorFlowConfig {

    @Bean
    public IntegrationFlow fileProcessorFlow(
            MessageChannel sftpFilesChannel,
            FileProcessorService fileProcessorService) {
        return IntegrationFlow.from(sftpFilesChannel)
                .handle(fileProcessorService::processFile)
                .get();
    }
}
