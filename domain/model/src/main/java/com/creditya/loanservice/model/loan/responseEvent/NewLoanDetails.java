package com.creditya.loanservice.model.loan.responseEvent;

import java.math.BigDecimal;

public record NewLoanDetails(
        BigDecimal amount,
        Integer loanTerm,
        BigDecimal interestRate
) {
}
