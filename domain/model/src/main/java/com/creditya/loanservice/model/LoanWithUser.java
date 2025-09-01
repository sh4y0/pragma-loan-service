package com.creditya.loanservice.model;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanWithUser {
    private Loan loan;
    private UserSnapshot userSnapshot;
}
