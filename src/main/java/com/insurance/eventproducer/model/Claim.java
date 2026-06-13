package com.insurance.eventproducer.model;

import com.insurance.eventproducer.enums.ClaimStatus;
import com.insurance.eventproducer.enums.ClaimType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Claim(
        String claimId,
        String policyNumber,
        String claimantName,
        BigDecimal claimAmount,
        ClaimType claimType,
        LocalDate incidentDate,
        ClaimStatus status
) {
}
