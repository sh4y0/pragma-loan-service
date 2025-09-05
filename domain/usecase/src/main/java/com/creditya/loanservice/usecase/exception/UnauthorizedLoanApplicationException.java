package com.creditya.loanservice.usecase.exception;


import com.creditya.loanservice.usecase.utils.ErrorCatalog;

public class UnauthorizedLoanApplicationException extends BaseException {
    public UnauthorizedLoanApplicationException() {
        super(
                ErrorCatalog.DNI_MISMATCH.getCode(),
                ErrorCatalog.DNI_MISMATCH.getTitle(),
                ErrorCatalog.DNI_MISMATCH.getMessage(),
                ErrorCatalog.DNI_MISMATCH.getStatus(),
                ErrorCatalog.DNI_MISMATCH.getErrors()
        );
    }
}