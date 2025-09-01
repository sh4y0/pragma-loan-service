package com.creditya.loanservice.model.loan.data;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanData {
    private UUID loanId;
    private BigDecimal amount;
    private Integer loanTerm;
    private String email;
    private String dni;
    private String status;
    private String loanType;
}
