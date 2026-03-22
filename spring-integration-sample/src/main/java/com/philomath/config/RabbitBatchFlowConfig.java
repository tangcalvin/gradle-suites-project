package com.philomath.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * RabbitMQ batch inbound flow: receives up to batchSize messages, or partial batch
 * after receiveTimeout ms. Groups by "group" attribute, splits, and dispatches to
 * downstream ExecutorChannel for parallel processing by multiple workers.
 *
 * <p>Channel type visibility: Explicit channel beans below return {@link ExecutorChannel}
 * or {@link org.springframework.integration.channel.DirectChannel}. When no
 * {@code .channel(...)} is specified between steps, Spring Integration uses an
 * implicit DirectChannel (sequential).
 */
@Slf4j
@Configuration
public class RabbitBatchFlowConfig {

    /** Use JSON instead of Java Serialization for message payloads (avoids deserialization security block). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Ensure RabbitTemplate uses JSON converter when sending (content-type: application/json).
     * If you see "application/x-java-serialized-object" warnings, purge the queue to remove
     * old Java-serialized messages; new messages sent via REST will be JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public Queue batchQueue(@Value("${rabbitmq.queue:test.batch.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean("groupProcessorExecutor")
    public Executor groupProcessorExecutor(
            @Value("${rabbitmq.group-processor-parallelism:4}") int parallelism) {
        return Executors.newFixedThreadPool(
                Math.max(1, parallelism),
                runnable -> {
                    Thread t = new Thread(runnable, "rabbit-group-processor");
                    t.setDaemon(false);
                    return t;
                });
    }

    /** ExecutorChannel: parallel processing (multiple workers). */
    @Bean("groupProcessorChannel")
    public ExecutorChannel groupProcessorChannel(Executor groupProcessorExecutor) {
        return new ExecutorChannel(groupProcessorExecutor);
    }

    @Bean
    public IntegrationFlow rabbitBatchFlow(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter,
            @Value("${rabbitmq.queue:test.batch.queue}") String queueName,
            @Value("${rabbitmq.batch-size:100}") int batchSize,
            @Value("${rabbitmq.receive-timeout-ms:3000}") long receiveTimeoutMs,
            @Qualifier("groupProcessorChannel") MessageChannel groupProcessorChannel) {
        return IntegrationFlow
                .from(Amqp.inboundAdapter(connectionFactory, queueName)
                        .messageConverter(jsonMessageConverter)
                        .configureContainer(c -> c
                                .batchSize(batchSize)
                                .consumerBatchEnabled(true)
                                .receiveTimeout(receiveTimeoutMs))
                        .batchMode(AmqpInboundChannelAdapter.BatchMode.EXTRACT_PAYLOADS))
                .<List<?>>handle((list, headers) -> {
                    int size = list != null ? list.size() : 0;
                    log.info("[RabbitBatch] Received batch of {} messages from queue", size);
                    if (list == null || list.isEmpty()) {
                        log.debug("[RabbitBatch] Empty batch, skipping grouping");
                        return List.<List<?>>of();
                    }
                    // Group by "group" attribute (fallback "unknown" if missing)
                    @SuppressWarnings("unchecked")
                    Map<Object, List<?>> grouped = (Map<Object, List<?>>) (Map<?, ?>) list.stream()
                            .filter(p -> p != null)
                            .collect(Collectors.groupingBy((Object p) -> {
                                if (p instanceof Map) {
                                    Object g = ((Map<?, ?>) p).get("group");
                                    return g != null ? g : "unknown";
                                }
                                return "unknown";
                            }));
                    List<List<?>> groups = new ArrayList<>(grouped.values());
                    String groupSummary = grouped.entrySet().stream()
                            .map(e -> String.format("%s=%d", e.getKey(), e.getValue().size()))
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    log.info("[RabbitBatch] Grouped {} messages into {} groups: [{}]", list.size(), groups.size(), groupSummary);
                    return groups;
                })
                .split()
                .channel(groupProcessorChannel)
                .get();
    }

    /**
     * Downstream flow: multiple workers pick up groups from the ExecutorChannel
     * and process them in parallel. Each group is handled on a separate thread.
     */
    @Bean
    public IntegrationFlow groupProcessorFlow(
            @Qualifier("groupProcessorChannel") MessageChannel groupProcessorChannel) {
        return IntegrationFlow.from(groupProcessorChannel)
                .<List<?>>handle((payload, headers) -> {
                    int size = payload != null ? payload.size() : 0;
                    Object groupId = payload != null && size > 0 && payload.get(0) instanceof Map
                            ? ((Map<?, ?>) payload.get(0)).get("group")
                            : "?";
                    log.info("[RabbitBatch] [{}] Processing group (group={}): {} messages",
                            Thread.currentThread().getName(), groupId, size);
                    return null;
                })
                .get();
    }
}
