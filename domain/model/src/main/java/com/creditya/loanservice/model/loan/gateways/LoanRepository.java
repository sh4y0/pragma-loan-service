package com.creditya.loanservice.model.loan.gateways;

import com.creditya.loanservice.model.loan.Loan;
import reactor.core.publisher.Mono;

public interface LoanRepository {
    Mono<Loan> createLoan(Loan loan);
}
