package com.creditya.loanservice.api.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LoanUpdateRequestDTO(
        UUID idLoan,
        String status
) {
}
