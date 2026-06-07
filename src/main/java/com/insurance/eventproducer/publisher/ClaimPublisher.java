package com.insurance.eventproducer.publisher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClaimPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClaimPublisher.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.topic.hello}")
    private String topic;

    public ClaimPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String message) {
        kafkaTemplate.send(topic, message)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish to {}: {}", topic, message, ex);
                } else {
                    log.info("Published to {} partition {} offset {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}