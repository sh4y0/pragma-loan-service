package com.creditya.loanservice.usecase.exception;

import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class LoanStatusNotFoundException extends BaseException {
    private static final ErrorCatalog error = ErrorCatalog.LOAN_STATUS_NOT_FOUND;
    public LoanStatusNotFoundException(){
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors()
        );
    }
}