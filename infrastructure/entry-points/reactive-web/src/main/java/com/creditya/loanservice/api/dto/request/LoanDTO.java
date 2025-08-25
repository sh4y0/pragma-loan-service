package com.creditya.loanservice.api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanDTO(@NotNull(message = "Amount cannot be null")
                      @DecimalMin(value= "0.0", inclusive = true, message = "Amount cannot be less than 0")
                      @DecimalMax(value = "15000000.0", inclusive = true, message = "Amount cannot exceed 15,000,000")
                      BigDecimal amount,

                      @NotNull(message = "Term cannot be null")
                      @Min(value = 0, message = "Term cannot be less than 0")
                      int loanTerm,

                      @NotBlank(message = "Email cannot be blank")
                      @Email(message = "A valid email address is required")
                      String email,

                      @NotBlank(message = "DNI cannot be blank")
                      @Pattern(regexp = "\\d{8}", message = "DNI must contain exactly 8 digits")
                      String dni,

                      @NotBlank(message = "Loan Type cannot be blank")
                      String loanType) {
}
