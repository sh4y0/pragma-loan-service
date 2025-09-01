package com.creditya.loanservice.api.dto.response;

import java.math.BigDecimal;

public record UserResponseDTO(
        String userId,
        String name,
        String email,
        BigDecimal baseSalary
) {
}
