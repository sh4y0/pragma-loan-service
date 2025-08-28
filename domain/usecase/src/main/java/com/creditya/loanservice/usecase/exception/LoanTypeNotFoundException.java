package com.creditya.loanservice.usecase.exception;

import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class LoanTypeNotFoundException extends BaseException{
    private static final ErrorCatalog error = ErrorCatalog.LOAN_TYPE_NOT_FOUND;

    public LoanTypeNotFoundException() {
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors()
        );
    }
}
