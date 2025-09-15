package com.creditya.loanservice.model.loan.data;

import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanWithUser {
    private UUID idLoan;
    private BigDecimal amount;
    private Integer loanTerm;
    private String email;
    private String dni;
    private UserSnapshot userSnapshot;
    private String loanTypeName;
    private String loanStatusName;
    private BigDecimal interestRate;
    private BigDecimal totalMonthlyDebt;
    private Long approvedLoan;
}
