package com.creditya.loanservice.model.loan.gateways;

import com.creditya.loanservice.model.loan.Loan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanRepository {
    Mono<Loan> createLoan(Loan loan);
    Flux<Loan> findAllLoans();
    Flux<Loan> findLoansByStatusIds(List<UUID> statusIds);
    Mono<Long> countAllLoans();
    Mono<Long> countLoansByStatusIds(List<UUID> statusIds);
}
