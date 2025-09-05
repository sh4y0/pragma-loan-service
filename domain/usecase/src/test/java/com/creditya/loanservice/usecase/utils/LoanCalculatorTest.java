package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanCalculatorTest {

    private LoanStatus loanStatus;
    private LoanCalculator loanCalculator;

    @BeforeEach
    void setUp() {
        loanStatus = mock(LoanStatus.class);
        loanCalculator = new LoanCalculator(loanStatus);
    }

    @Test
    void calculateMonthlyPayment_shouldThrowIfLoanTermIsZero() {
        Loan loan = new Loan();
        loan.setLoanTerm(0);
        loan.setAmount(BigDecimal.valueOf(1000));

        assertThrows(IllegalArgumentException.class,
                () -> loanCalculator.calculateMonthlyPayment(loan, BigDecimal.valueOf(10)));
    }

    @Test
    void calculateMonthlyPayment_shouldReturnPositivePayment() {
        Loan loan = new Loan();
        loan.setLoanTerm(12);
        loan.setAmount(BigDecimal.valueOf(1200));

        // cuando annualRate = 12%
        BigDecimal result = loanCalculator.calculateMonthlyPayment(loan, BigDecimal.valueOf(12));

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateTotalMonthlyDebt_shouldReturnZeroIfUserIsNull() {
        BigDecimal result = loanCalculator.calculateTotalMonthlyDebt(
                null, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()
        );
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateTotalMonthlyDebt_shouldSumApprovedAndPendingLoans() {
        UUID userId = UUID.randomUUID();
        UUID loanTypeId = UUID.randomUUID();
        UUID loanStatusId = UUID.randomUUID();

        UserSnapshot user = new UserSnapshot();
        user.setUserId(userId);

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setLoanTerm(12);
        loan.setAmount(BigDecimal.valueOf(1200));
        loan.setIdLoanType(loanTypeId);
        loan.setIdStatus(loanStatusId);

        LoanType loanType = new LoanType();
        loanType.setInterestRate(BigDecimal.valueOf(12));

        com.creditya.loanservice.model.loanstatus.LoanStatus status =
                new com.creditya.loanservice.model.loanstatus.LoanStatus();
        status.setName("APPROVED");

        when(loanStatus.isApproved("APPROVED")).thenReturn(true);

        BigDecimal result = loanCalculator.calculateTotalMonthlyDebt(
                user,
                List.of(loan),
                Map.of(loanTypeId, loanType),
                Map.of(loanStatusId, status)
        );

        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateApprovedLoansCount_shouldReturnNumberOfApprovedLoans() {
        UUID userId = UUID.randomUUID();
        UUID loanStatusId = UUID.randomUUID();

        UserSnapshot user = new UserSnapshot();
        user.setUserId(userId);

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setIdStatus(loanStatusId);

        com.creditya.loanservice.model.loanstatus.LoanStatus status =
                new com.creditya.loanservice.model.loanstatus.LoanStatus();
        status.setName("APPROVED");

        when(loanStatus.isApproved("APPROVED")).thenReturn(true);

        long result = loanCalculator.calculateApprovedLoansCount(
                user,
                List.of(loan),
                Map.of(loanStatusId, status)
        );

        assertEquals(1, result);
    }
}
