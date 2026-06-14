package com.insurance.eventproducer.controller;

import com.insurance.eventproducer.model.Claim;
import com.insurance.eventproducer.publisher.ClaimPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimPublisher publisher;

    public ClaimController(ClaimPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/v1/publish")
    public ResponseEntity<String> publish(@RequestBody Claim message) {
        publisher.publish(message);
        return ResponseEntity.ok("Message published");
    }
}