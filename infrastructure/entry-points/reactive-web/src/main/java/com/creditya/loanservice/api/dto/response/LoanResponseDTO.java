package com.creditya.loanservice.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LoanResponseDTO(
        String name,
        String email,
        BigDecimal amount,
        Integer loanTerm,
        BigDecimal baseSalary,
        String loanType,
        String loanStatus,

        BigDecimal interestRate,
        BigDecimal totalMontlyDebt,
        Integer approvedLoans
) {}

