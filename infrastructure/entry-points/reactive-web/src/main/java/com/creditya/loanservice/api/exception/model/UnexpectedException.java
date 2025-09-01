package com.creditya.loanservice.api.exception.model;


import com.creditya.loanservice.usecase.exception.BaseException;
import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class UnexpectedException extends BaseException {
    private static final ErrorCatalog error = ErrorCatalog.INTERNAL_SERVER_ERROR;

    public UnexpectedException(Throwable cause) {
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors()
        );
        initCause(cause);
    }
}