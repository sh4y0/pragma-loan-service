package com.creditya.loanservice.model.creditanalisys;

import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record NewLoanDetails(
        BigDecimal amount,
        Integer loanTerm,
        BigDecimal interestRate
) {
}
