package com.creditya.loanservice.model.creditanalisys;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(toBuilder = true)
public record ActiveLoanDetails(
        UUID idLoan,
        BigDecimal amount,
        Integer loanTerm,
        BigDecimal interestRate
) {
}
