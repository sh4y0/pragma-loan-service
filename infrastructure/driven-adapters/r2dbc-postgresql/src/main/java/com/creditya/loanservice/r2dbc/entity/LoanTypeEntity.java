package com.creditya.loanservice.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("loan_type")
public class LoanTypeEntity {
    @Id
    @Column("id_loan_type")
    private UUID idLoanType;
    private String name;
    @Column("minimum_amount")
    private BigDecimal minimumAmount;

    @Column("maximum_amount")
    private BigDecimal maximumAmount;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("automatic_validation")
    private Boolean automaticValidation;
}
