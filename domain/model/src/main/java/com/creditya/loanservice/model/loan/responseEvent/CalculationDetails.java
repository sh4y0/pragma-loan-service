package com.creditya.loanservice.model.loan.responseEvent;

import java.math.BigDecimal;
import java.util.List;

public record CalculationDetails(
        NewLoanDetails newLoanDetails,
        BigDecimal availableCapacity,
        BigDecimal newLoanMonthlyPayment,
        List<PaymentInstallment> paymentSchedule
) {
}
