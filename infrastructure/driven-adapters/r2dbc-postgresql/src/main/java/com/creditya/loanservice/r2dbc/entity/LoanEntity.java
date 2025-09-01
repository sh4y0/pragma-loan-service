package com.creditya.loanservice.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("loan")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoanEntity {
    @Id
    @Column("id_loan")
    private UUID idLoan;
    private BigDecimal amount;
    private Integer loanTerm;
    private String email;
    private String dni;
    @Column("id_status")
    private UUID idStatus;
    @Column("id_loan_type")
    private UUID idLoanType;
    @Column("user_id")
    private UUID userId;
}
