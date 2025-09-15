package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

public class LoanCalculator {

    private static final int DEFAULT_DECIMAL_SCALE = 2;
    private static final int EXTRA_PRECISION = 4;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final LoanStatus loanStatus;

    public LoanCalculator(LoanStatus loanStatus) {
        this.loanStatus = Objects.requireNonNull(loanStatus, "loanStatus must not be null");
    }

    public long calculateApprovedLoansCount(LoanJoinedProjection loan) {
        if (loan == null || loan.loanStatusName() == null) {
            return 0;
        }
        return loanStatus.isApproved(loan.loanStatusName()) ? 1 : 0;
    }

    public BigDecimal calculateTotalMonthlyDebt(LoanJoinedProjection loan) {
        Objects.requireNonNull(loan, "loan must not be null");
        Objects.requireNonNull(loan.amount(), "loan amount must not be null");
        Objects.requireNonNull(loan.interestRate(), "interestRate must not be null");

        if (loan.loanTerm() <= 0) {
            throw new IndexOutOfBoundsExceptionPage();
        }

        BigDecimal monthlyRate = loan.interestRate()
                .divide(BigDecimal.valueOf(100), DEFAULT_DECIMAL_SCALE + EXTRA_PRECISION, DEFAULT_ROUNDING_MODE)
                .divide(BigDecimal.valueOf(12), DEFAULT_DECIMAL_SCALE + EXTRA_PRECISION, DEFAULT_ROUNDING_MODE);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return loan.amount()
                    .divide(BigDecimal.valueOf(loan.loanTerm()), DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = monthlyRate.multiply(onePlusRate.pow(loan.loanTerm(), MathContext.DECIMAL64));
        BigDecimal denominator = onePlusRate.pow(loan.loanTerm(), MathContext.DECIMAL64).subtract(BigDecimal.ONE);

        return loan.amount()
                .multiply(numerator)
                .divide(denominator, DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
