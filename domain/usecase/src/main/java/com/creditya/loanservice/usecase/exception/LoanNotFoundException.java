package com.creditya.loanservice.usecase.exception;

import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class LoanNotFoundException extends BaseException {
    private static final ErrorCatalog error = ErrorCatalog.LOAN_NOT_FOUND;
    public LoanNotFoundException(){
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors()
        );
    }
}
