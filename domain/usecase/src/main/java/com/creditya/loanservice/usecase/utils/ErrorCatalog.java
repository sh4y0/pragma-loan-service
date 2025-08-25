package com.creditya.loanservice.usecase.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum ErrorCatalog {

    LOAN_NOT_FOUND(
            "LOAN_NOT_FOUND",
            "Loan Not Found",
            "The specified loan does not exist in the system.",
            404,
            Map.of("loan", "The loan you requested could not be found. Please verify the ID or DNI.")
    ),

    LOAN_STATUS_NOT_FOUND(
            "LOAN_STATUS_NOT_FOUND",
            "Loan Status Not Found",
            "The requested loan status does not exist in the system.",
            404,
            Map.of("status", "The provided loan status is invalid or missing in the database.")
    ),

    LOAN_TYPE_NOT_FOUND(
            "LOAN_TYPE_NOT_FOUND",
            "Loan Type Not Found",
            "The requested loan type could not be found.",
            404,
            Map.of("loanType", "The loan type you provided does not exist in the system.")
    ),

    LOAN_AMOUNT_OUT_OF_RANGE(
            "LOAN_AMOUNT_OUT_OF_RANGE",
            "Loan Amount Out of Range",
            "The requested loan amount is not within the allowed range for the selected loan type.",
            400,
            Map.of(
                    "amount", "The loan amount must be between the minimum and maximum allowed for this loan type."
            )
    ),

    VALIDATION_EXCEPTION(
            "VALIDATION_EXCEPTION",
            "Validation Failed",
            "Some of the provided data is invalid. Please review the fields and try again.",
            400,
            null
    ),

    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "Internal Server Error",
            "Something went wrong on our side. Please try again later or contact support if the issue persists.",
            500,
            Map.of("server", "Unexpected error occurred while processing the loan request")
    );

    private final String code;
    private final String title;
    private final String message;
    private final int status;
    private final Map<String, String> errors;

}
