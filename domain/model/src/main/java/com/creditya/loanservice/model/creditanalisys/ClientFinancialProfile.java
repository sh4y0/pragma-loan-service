package com.creditya.loanservice.model.creditanalisys;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder(toBuilder = true)
public record ClientFinancialProfile(
        BigDecimal totalRevenues,
        List<ActiveLoanDetails> activeLoans
) {
}
