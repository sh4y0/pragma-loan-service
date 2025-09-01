package com.creditya.loanservice.api.facade;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.usecase.LoanUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("secureCreateLoan")
@RequiredArgsConstructor
public class SecureLoanFacade {
    private final LoanUseCase loanUseCase;

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public Mono<LoanData> createLoan(Loan loan, String loanTypeName) {
        return this.loanUseCase.createLoan(loan, loanTypeName);
    }
}
