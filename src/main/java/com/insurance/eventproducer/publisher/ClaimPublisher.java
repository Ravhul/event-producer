package com.insurance.eventproducer.publisher;


import com.insurance.eventproducer.model.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.insurance.eventproducer.constants.ClaimsConstants.RAW_CLAIM_TOPIC;

@Service
public class ClaimPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClaimPublisher.class);
    private final KafkaTemplate<String, Claim> kafkaTemplate;

    private static final String topic = RAW_CLAIM_TOPIC;

    public ClaimPublisher(KafkaTemplate<String, Claim> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Claim message) {
        String claimKey = message.rxNbr() + "-" + message.fillNbr() + "-" + message.locNbr();
        kafkaTemplate.send(topic, claimKey ,message)
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