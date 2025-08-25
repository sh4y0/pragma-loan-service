package com.creditya.loanservice.model.loan;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Loan {
    private UUID loanId;
    private BigDecimal amount;
    private Integer loanTerm;
    private String email;
    private String dni;
    private UUID idStatus;
    private UUID idLoanType;

}
