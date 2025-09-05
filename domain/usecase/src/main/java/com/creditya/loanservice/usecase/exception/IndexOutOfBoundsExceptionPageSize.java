package com.creditya.loanservice.usecase.exception;

import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class IndexOutOfBoundsExceptionPageSize extends BaseException{
    private static final ErrorCatalog error = ErrorCatalog.PAGE_SIZE_INVALID;

    public IndexOutOfBoundsExceptionPageSize() {
        super(  error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors());
    }
}
