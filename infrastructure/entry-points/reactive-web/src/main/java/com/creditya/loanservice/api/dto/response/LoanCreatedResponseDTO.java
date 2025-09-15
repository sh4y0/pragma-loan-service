package com.creditya.loanservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record LoanCreatedResponseDTO(
        @Schema(description = "Loan UUID", example = "111111-22222-33333-44444")
        UUID idLoan,

        @Schema(description = "Requested loan amount", example = "50000.00")
        BigDecimal amount,

        @Schema(description = "Loan term in months", example = "12")
        int loanTerm,

        @Schema(description = "User's Email", example = "gutierrezherrada@gmail.com")
        String email,

        @Schema(description = "National ID (DNI) of the applicant", example = "12345678")
        String dni,

        @Schema(description = "Type of the loan", example = "PERSONAL")
        String loanType
) {
}
