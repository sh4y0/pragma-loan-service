package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LoanCalculator {

    private static final int DEFAULT_DECIMAL_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final LoanStatus loanStatus;

    public LoanCalculator(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
    }

    public BigDecimal calculateTotalMonthlyDebt(
            UserSnapshot user,
            List<Loan> allLoans,
            Map<UUID, LoanType> loanTypeMap,
            Map<UUID, com.creditya.loanservice.model.loanstatus.LoanStatus> loanStatusMap
    ) {
        if (user == null) return BigDecimal.ZERO;

        return allLoans.stream()
                .filter(loan -> user.getUserId().equals(loan.getUserId()))
                .filter(loan -> {
                    com.creditya.loanservice.model.loanstatus.LoanStatus status = loanStatusMap.get(loan.getIdStatus());
                    return status != null && (loanStatus.isApproved(status.getName())
                            || loanStatus.isPending(status.getName()));
                })
                .map(loan -> {
                    LoanType type = loanTypeMap.get(loan.getIdLoanType());
                    return calculateMonthlyPayment(loan, type != null ? type.getInterestRate() : BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long calculateApprovedLoansCount(UserSnapshot user, List<Loan> allLoans, Map<UUID, com.creditya.loanservice.model.loanstatus.LoanStatus> loanStatusMap) {
        if (user == null) return 0;

        return allLoans.stream()
                .filter(loan -> user.getUserId().equals(loan.getUserId()))
                .map(loan -> loanStatusMap.get(loan.getIdStatus()))
                .filter(Objects::nonNull)
                .filter(status -> loanStatus.isApproved(status.getName()))
                .count();
    }

    public BigDecimal calculateMonthlyPayment(Loan loan, BigDecimal annualRate) {
        if (loan.getLoanTerm() <= 0) throw new IllegalArgumentException("Loan term must be positive");

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), DEFAULT_DECIMAL_SCALE + 4, DEFAULT_ROUNDING_MODE)
                .divide(BigDecimal.valueOf(12), DEFAULT_DECIMAL_SCALE + 4, DEFAULT_ROUNDING_MODE);

        BigDecimal onePlusRatePow = BigDecimal.ONE.add(monthlyRate)
                .pow(-loan.getLoanTerm(), MathContext.DECIMAL64);

        return loan.getAmount()
                .multiply(monthlyRate)
                .divide(BigDecimal.ONE.subtract(onePlusRatePow), DEFAULT_DECIMAL_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
