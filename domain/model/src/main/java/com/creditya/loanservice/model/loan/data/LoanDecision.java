package com.creditya.loanservice.model.loan.data;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanDecision {
    private UUID idLoan;
    private String status;
    private boolean automaticValidation;
}