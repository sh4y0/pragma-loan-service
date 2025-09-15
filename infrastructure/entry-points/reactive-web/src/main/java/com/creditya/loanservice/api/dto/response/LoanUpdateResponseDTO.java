package com.creditya.loanservice.api.dto.response;

import java.util.UUID;

public record LoanUpdateResponseDTO(
        UUID idLoan,
        String status
) {
}
