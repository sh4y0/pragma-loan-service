package com.creditya.loanservice.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record LoanResponseDTO(
        UUID idLoan,
        String name,
        String email,
        String dni,
        BigDecimal amount,
        Integer loanTerm,
        BigDecimal baseSalary,
        String loanType,
        String loanStatus,

        BigDecimal interestRate,
        BigDecimal totalMontlyDebt,
        Long approvedLoans
) {}

