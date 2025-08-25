package com.creditya.loanservice.api.exception.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String tittle;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Object errors;
}