package com.creditya.loanservice.api.exception.model;

import com.creditya.loanservice.usecase.exception.BaseException;
import com.creditya.loanservice.usecase.utils.ErrorCatalog;
import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends BaseException {
    private final static ErrorCatalog error = ErrorCatalog.VALIDATION_EXCEPTION;

    public ValidationException(Map<String, String> errors) {
        super(
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getStatus(),
                errors
        );
    }
}