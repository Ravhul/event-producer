package com.insurance.eventproducer.config;

import com.insurance.eventproducer.model.Claim;
import com.insurance.eventproducer.publisher.ClaimPublisher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

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

    @Bean
    ItemWriter<Claim> claimWriter(ClaimPublisher publisher){
        return chunk -> {
            for(Claim claim: chunk){
                publisher.publish(claim);
            }
        };
    }

    @Bean
    Step publishStep(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<Claim> claimReader,
                     ItemWriter<Claim> claimWriter){
        return new StepBuilder("publishStep",jobRepository)
                .<Claim,Claim>chunk(2,transactionManager)
                .reader(claimReader)
                .writer(claimWriter)
                .build();
    }

    @Bean
    Job publishClaimJob(JobRepository jobRepository, Step publishStep){
        return new JobBuilder("publishClaimJob", jobRepository)
                .start(publishStep)
                .build();
    }
}


