package com.insurance.eventproducer.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Claim(
        String claimSk,
        String rxNbr,
        String fillNbr,
        String locNbr,
        String pdRvInd,
        String binNbr,
        String pcn,
        String plnId,
        String hrchySk,
        String ndc,
        String ndcSk,
        BigDecimal amount,
        Instant claimCreatedTs
) {
}
