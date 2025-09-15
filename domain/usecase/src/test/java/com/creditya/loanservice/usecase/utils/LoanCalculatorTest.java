package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanCalculatorTest {

    private LoanStatus loanStatus;
    private LoanCalculator calculator;

    @BeforeEach
    void setUp() {
        loanStatus = mock(LoanStatus.class);
        calculator = new LoanCalculator(loanStatus);
    }

    private LoanJoinedProjection buildLoan(BigDecimal amount, BigDecimal interestRate, int term, String statusName) {
        return new LoanJoinedProjection(
                null, null, amount, term,
                "test@example.com", "12345678",
                statusName, "PERSONAL", interestRate
        );
    }

    @Test
    void calculateApprovedLoansCount_nullLoan_returns0() {
        assertEquals(0, calculator.calculateApprovedLoansCount(null));
    }

    @Test
    void calculateApprovedLoansCount_nullStatus_returns0() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), BigDecimal.valueOf(5), 12, null);
        assertEquals(0, calculator.calculateApprovedLoansCount(loan));
    }

    @Test
    void calculateApprovedLoansCount_approved_returns1() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), BigDecimal.valueOf(5), 12, "APPROVED");
        when(loanStatus.isApproved("APPROVED")).thenReturn(true);
        assertEquals(1, calculator.calculateApprovedLoansCount(loan));
    }

    @Test
    void calculateApprovedLoansCount_notApproved_returns0() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), BigDecimal.valueOf(5), 12, "PENDING");
        when(loanStatus.isApproved("PENDING")).thenReturn(false);
        assertEquals(0, calculator.calculateApprovedLoansCount(loan));
    }

    @Test
    void calculateTotalMonthlyDebt_nullLoan_throws() {
        assertThrows(NullPointerException.class, () -> calculator.calculateTotalMonthlyDebt(null));
    }

    @Test
    void calculateTotalMonthlyDebt_nullAmount_throws() {
        LoanJoinedProjection loan = buildLoan(null, BigDecimal.valueOf(5), 12, "APPROVED");
        assertThrows(NullPointerException.class, () -> calculator.calculateTotalMonthlyDebt(loan));
    }

    @Test
    void calculateTotalMonthlyDebt_nullInterestRate_throws() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), null, 12, "APPROVED");
        assertThrows(NullPointerException.class, () -> calculator.calculateTotalMonthlyDebt(loan));
    }

    @Test
    void calculateTotalMonthlyDebt_termZero_throws() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), BigDecimal.valueOf(5), 0, "APPROVED");
        assertThrows(IndexOutOfBoundsExceptionPage.class, () -> calculator.calculateTotalMonthlyDebt(loan));
    }

    @Test
    void calculateTotalMonthlyDebt_zeroInterest_returnsSimpleDivision() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1200), BigDecimal.ZERO, 12, "APPROVED");
        BigDecimal result = calculator.calculateTotalMonthlyDebt(loan);
        assertTrue(result.compareTo(BigDecimal.valueOf(100.00)) == 0);
    }

    @Test
    void calculateTotalMonthlyDebt_positiveInterest_returnsCorrectValue() {
        LoanJoinedProjection loan = buildLoan(BigDecimal.valueOf(1000), BigDecimal.valueOf(12), 12, "APPROVED");
        BigDecimal result = calculator.calculateTotalMonthlyDebt(loan);
        assertEquals(BigDecimal.valueOf(88.85), result.setScale(2, BigDecimal.ROUND_HALF_UP));
    }
}
