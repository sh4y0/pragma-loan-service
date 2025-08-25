package com.creditya.loanservice.usecase.exception;


import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class LoanAmountOutOfRangeException extends BaseException {
    private static final ErrorCatalog error = ErrorCatalog.LOAN_AMOUNT_OUT_OF_RANGE;
    public LoanAmountOutOfRangeException() {
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors()
        );
    }
}