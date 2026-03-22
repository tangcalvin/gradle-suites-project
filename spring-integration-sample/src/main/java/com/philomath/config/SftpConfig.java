package com.philomath.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;

/**
 * SFTP inbound adapter: leader-only, single connection, sequential download (max 1 file per poll).
 * Started/stopped by {@link LeaderSftpAdapterController} based on leadership.
 */
@Configuration
public class SftpConfig {

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory(
            @Value("${sftp.host:localhost}") String host,
            @Value("${sftp.port:22}") int port,
            @Value("${sftp.username:}") String username,
            @Value("${sftp.password:}") String password,
            @Value("${sftp.private-key:}") String privateKeyPath,
            @Value("${sftp.allow-unknown-keys:true}") boolean allowUnknownKeys) {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(username);
        if (password != null && !password.isBlank()) {
            factory.setPassword(password);
        }
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            factory.setPrivateKey(new FileSystemResource(privateKeyPath));
        }
        factory.setAllowUnknownKeys(allowUnknownKeys);
        return factory;
    }

    @Bean
    public CachingSessionFactory<SftpClient.DirEntry> cachedSftpSessionFactory(DefaultSftpSessionFactory sftpSessionFactory) {
        return new CachingSessionFactory<>(sftpSessionFactory, 1);
    }

    @Bean
    public IntegrationFlow sftpInboundFlow(
            CachingSessionFactory<SftpClient.DirEntry> cachedSftpSessionFactory,
            @Value("${sftp.remote-directory:/}") String remoteDirectory,
            @Value("${sftp.local-directory:./sftp-staging}") File localDirectory,
            @Value("${sftp.filename-pattern:*.*}") String filenamePattern,
            @Value("${sftp.poller-fixed-delay:5000}") long pollerFixedDelay,
            MessageChannel sftpPollerErrorChannel,
            MessageChannel sftpFilesChannel) {
        return IntegrationFlow
                .from(Sftp.inboundAdapter(cachedSftpSessionFactory)
                                .preserveTimestamp(true)
                                .remoteDirectory(remoteDirectory)
                                .filter(new SftpSimplePatternFileListFilter(filenamePattern))
                                .localDirectory(localDirectory)
                                .autoCreateLocalDirectory(true)
                                .maxFetchSize(1),
                        poller -> poller
                                .id("sftpInboundAdapter")
                                .autoStartup(false)
                                .poller(Pollers.fixedDelay(pollerFixedDelay)
                                        .errorChannel(sftpPollerErrorChannel)))
                .channel(sftpFilesChannel)
                .get();
    }

    /** DirectChannel: sequential (single-threaded). */
    @Bean
    public DirectChannel sftpPollerErrorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow sftpPollerErrorFlow(MessageChannel sftpPollerErrorChannel) {
        return IntegrationFlow.from(sftpPollerErrorChannel)
                .handle(new LoggingHandler(LoggingHandler.Level.ERROR))
                .get();
    }

    /** ExecutorChannel: parallel processing (multiple workers). */
    @Bean
    public ExecutorChannel sftpFilesChannel(java.util.concurrent.Executor sftpFileProcessorExecutor) {
        return new ExecutorChannel(sftpFileProcessorExecutor);
    }

    @Bean
    public java.util.concurrent.Executor sftpFileProcessorExecutor(
            @Value("${sftp.processor-parallelism:4}") int parallelism) {
        return java.util.concurrent.Executors.newFixedThreadPool(
                Math.max(1, parallelism),
                runnable -> {
                    Thread thread = new Thread(runnable, "sftp-file-processor");
                    thread.setDaemon(false);
                    return thread;
                });
    }
}
