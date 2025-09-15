package com.creditya.loanservice.model.loan.responseEvent;

import java.math.BigDecimal;

public record PaymentInstallment(
        int month,
        BigDecimal payment,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal remainingBalance
) {
}
