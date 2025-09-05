package com.creditya.loanservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(name = "LoanDTO", description = "Request data required for Loan")
public record LoanCreatedRequestDTO(@NotNull(message = "Amount cannot be null")
                      @DecimalMin(value= "0.0", inclusive = true, message = "Amount cannot be less than 0")
                      @DecimalMax(value = "15000000.0", inclusive = true, message = "Amount cannot exceed 15,000,000")
                      @Schema(description = "Requested loan amount", example = "50000.00")
                      BigDecimal amount,

                                    @NotNull(message = "Term cannot be null")
                      @Min(value = 0, message = "Term cannot be less than 0")
                      @Schema(description = "Loan term in months", example = "12")
                      int loanTerm,

                                    @NotBlank(message = "Email cannot be blank")
                      @Pattern(
                              regexp = "^(?!.*\\.\\.)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                              message = "Email format is invalid"
                      )
                      @Schema(description = "User's Email", example = "gutierrezherrada@gmail.com")
                      String email,

                      @NotBlank(message = "DNI cannot be blank")
                      @Pattern(
                            regexp = "^[0-9]{8}[A-Za-z]$",
                            message = "DNI must contain exactly 8 digits followed by a letter"
                      )
                      @Schema(description = "National ID (DNI) of the applicant", example = "12345678Z")
                      String dni,

                                    @NotBlank(message = "Loan Type cannot be blank")
                      @Schema(description = "Type of the loan", example = "PERSONAL")
                      String loanType) {
}
