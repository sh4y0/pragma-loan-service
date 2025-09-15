package com.creditya.loanservice.r2dbc.loan_adapter;

import com.creditya.loanservice.model.creditanalisys.ActiveLoanDetails;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import com.creditya.loanservice.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class LoanReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Loan,
        LoanEntity,
        UUID,
        LoanReactiveRepository
> implements LoanRepository {
    public LoanReactiveRepositoryAdapter(LoanReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, d -> mapper.map(d, Loan.class));
    }

    @Override
    public Mono<Loan> createLoan(Loan loan) {
        return this.repository.save(this.toData(loan)).map(this::toEntity);
    }

    @Override
    public Mono<Loan> findLoanById(UUID uuid) {
        return this.repository.findById(uuid).map(this::toEntity);
    }



    @Override
    public Mono<Long> countAllLoans() {
        return this.repository.count();
    }

    @Override
    public Flux<LoanJoinedProjection> findAllLoans() {
        return this.repository.findAllLoans();
    }

    @Override
    public Mono<Long> countLoansByStatusIds(List<UUID> statusIds) {
        return this.repository.countByIdStatusIn(statusIds);
    }

    @Override
    public Flux<LoanJoinedProjection> findLoansWithTypeAndStatus(UUID[] statusIds, int limit, int offset) {
        return this.repository.findLoansWithTypeAndStatus(statusIds,limit,offset);
    }

    @Override
    public Flux<ActiveLoanDetails> findActiveLoansByUserId(UUID uuid) {
        return this.repository.findActiveLoansByUserId(uuid);
    }

}
