package com.philomath.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * REST endpoint to simulate sending messages to the batch queue for testing.
 * POST /api/test/send-messages?count=10000
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class RabbitTestController {

    private final RabbitTemplate rabbitTemplate;
    private final String queueName;
    private final Random random = new Random();

    public RabbitTestController(RabbitTemplate rabbitTemplate,
                                @Value("${rabbitmq.queue:test.batch.queue}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @PostMapping("/send-messages")
    public ResponseEntity<Map<String, Object>> sendMessages(
            @RequestParam(defaultValue = "10000") int count) {
        if (count <= 0 || count > 1_000_000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "count must be between 1 and 1000000"));
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", i + 1);
            payload.put("attribute", "A"); // or "B" for i % 2 == 1 to test grouping
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("group", 1 + random.nextInt(10)); // random group 1-10 for split grouping
            rabbitTemplate.convertAndSend(queueName, payload);
        }
        long elapsed = System.currentTimeMillis() - start;

        Map<String, Object> result = Map.of(
                "sent", count,
                "elapsedMs", elapsed,
                "queue", queueName);
        log.info("Sent {} messages in {} ms", count, elapsed);
        return ResponseEntity.ok(result);
    }
}
