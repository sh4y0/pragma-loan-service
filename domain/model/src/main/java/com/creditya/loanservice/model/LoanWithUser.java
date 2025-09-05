package com.creditya.loanservice.model;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanWithUser {
    private Loan loan;
    private UserSnapshot userSnapshot;
    private String loanTypeName;
    private String loanStatusName;
    private BigDecimal interestRate;
    private BigDecimal totalMontlyDebt;
    private Long approvedLoan;
}
