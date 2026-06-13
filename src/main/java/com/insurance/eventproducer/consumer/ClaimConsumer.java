package com.insurance.eventproducer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import static com.insurance.eventproducer.constants.ClaimsConstants.RAW_CLAIM_TOPIC;

@Component
public class ClaimConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimConsumer.class);
    private static final String topic = RAW_CLAIM_TOPIC;

    @KafkaListener(topics = topic, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message){
        log.info("Received: {}",message);
    }
}
