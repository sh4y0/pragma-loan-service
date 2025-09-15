package com.creditya.loanservice.model.loan.gateways;

import com.creditya.loanservice.model.creditanalisys.ActiveLoanDetails;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanRepository {
    Mono<Loan> createLoan(Loan loan);
    Mono<Loan> findLoanById(UUID uuid);
    Flux<LoanJoinedProjection> findAllLoans();
    Mono<Long> countAllLoans();
    Mono<Long> countLoansByStatusIds(List<UUID> statusIds);
    Flux<LoanJoinedProjection> findLoansWithTypeAndStatus(UUID[] statusIds, int limit, int offset);
    Flux<ActiveLoanDetails> findActiveLoansByUserId(UUID uuid);
}
