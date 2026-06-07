package com.insurance.eventproducer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClaimConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimConsumer.class);

    @KafkaListener(topics = "${app.topic.hello}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message){
        log.info("Received: {}",message);
    }
}
