package com.creditya.loanservice.usecase.exception;

import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class IndexOutOfBoundsExceptionPage extends BaseException {
    private static final ErrorCatalog error = ErrorCatalog.PAGE_INDEX_NEGATIVE;

    public IndexOutOfBoundsExceptionPage() {
        super(  error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                error.getErrors());
    }
}
