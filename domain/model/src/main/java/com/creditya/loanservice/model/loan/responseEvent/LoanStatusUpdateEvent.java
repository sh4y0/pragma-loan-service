package com.creditya.loanservice.model.loan.responseEvent;

import java.util.UUID;

public record LoanStatusUpdateEvent(
        UUID idLoan,
        UUID userId,
        String email,
        String status,
        CalculationDetails calculationDetails,
        boolean automaticValidation

) {}
