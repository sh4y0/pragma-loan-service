package com.creditya.loanservice.model.loan.data;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record LoanApprovedEvent(
        UUID idLoan,
        String status,
        BigDecimal amountApproved,
        String approvedAt
) {
}
