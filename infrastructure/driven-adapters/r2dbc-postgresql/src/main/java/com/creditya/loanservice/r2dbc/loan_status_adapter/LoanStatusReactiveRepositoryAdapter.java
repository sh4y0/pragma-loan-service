package com.creditya.loanservice.r2dbc.loan_status_adapter;

import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.r2dbc.entity.LoanStatusEntity;
import com.creditya.loanservice.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class LoanStatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanStatus,
        LoanStatusEntity,
        UUID,
        LoanStatusReactiveRepository
> implements LoanStatusRepository {
    public LoanStatusReactiveRepositoryAdapter(LoanStatusReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, d -> mapper.map(d, LoanStatus.class));
    }

    @Override
    public Mono<LoanStatus> findByName(String name) {
        return this.repository.findByName(name).map(this::toEntity);
    }
}
