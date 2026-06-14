package com.insurance.eventproducer.config;

import com.insurance.eventproducer.model.Claim;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Configuration
public class BatchConfig {

    @Bean
    public ItemReader<Claim> claimReader(){
        List<Claim> claims = List.of(
                // Sold + its reversal: SAME rxNbr-fillNbr-locNbr => same key => same partition (ordered together)
                new Claim("CSK-1001", "RX1001", "01", "500", "1",
                        "610279", "CLAIMCR", "PLN001", "HRCHY100",
                        "00071015523", "NDCSK01", new BigDecimal("125.50"), Instant.parse("2026-06-10T09:30:00Z")),
                new Claim("CSK-1002", "RX1001", "01", "500", "-1",
                        "610279", "CLAIMCR", "PLN001", "HRCHY100",
                        "00071015523", "NDCSK01", new BigDecimal("125.50"), Instant.parse("2026-06-12T14:15:00Z")),
                // Independent sold claims (distinct natural keys)
                new Claim("CSK-1003", "RX1002", "01", "500", "1",
                        "610279", "CLAIMCR", "PLN002", "HRCHY101",
                        "49702020118", "NDCSK02", new BigDecimal("89.99"), Instant.parse("2026-06-11T11:00:00Z")),
                new Claim("CSK-1004", "RX1003", "02", "700", "1",
                        "017856", "PCS", "PLN003", "HRCHY102",
                        "00093721910", "NDCSK03", new BigDecimal("432.00"), Instant.parse("2026-06-13T16:45:00Z"))
        );
        return new ListItemReader<>(claims);
    }
}
