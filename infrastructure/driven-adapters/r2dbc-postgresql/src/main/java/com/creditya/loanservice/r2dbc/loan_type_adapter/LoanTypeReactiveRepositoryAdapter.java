package com.creditya.loanservice.r2dbc.loan_type_adapter;

import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.r2dbc.entity.LoanTypeEntity;
import com.creditya.loanservice.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class LoanTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
    String,
        LoanTypeReactiveRepository
> implements LoanTypeRepository {
    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {

        super(repository, mapper, d -> mapper.map(d, LoanType.class));
    }

    @Override
    public Mono<LoanType> findByName(String name) {
        return this.repository.findByName(name).map(this::toEntity);
    }

    @Override
    public Flux<LoanType> findByIds(List<UUID> uuids) {
        return this.repository.findAllById(uuids)
                .map(this::toEntity);
    }
}
