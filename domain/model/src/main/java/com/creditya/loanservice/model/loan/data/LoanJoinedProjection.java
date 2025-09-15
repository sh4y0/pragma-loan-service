package com.creditya.loanservice.model.loan.data;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(toBuilder = true)
public record LoanJoinedProjection(
        UUID idLoan,
        UUID userId,
        BigDecimal amount,
        Integer loanTerm,
        String email,
        String dni,
        String loanStatusName,
        String loanTypeName,
        BigDecimal interestRate
) {}
