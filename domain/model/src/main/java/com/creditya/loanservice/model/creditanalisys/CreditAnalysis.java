package com.creditya.loanservice.model.creditanalisys;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record CreditAnalysis(
        UUID idLoan,
        UUID userId,
        String email,
        NewLoanDetails newLoanDetails,
        String status,
        ClientFinancialProfile financialProfile,
        boolean automaticValidation
) {
}
