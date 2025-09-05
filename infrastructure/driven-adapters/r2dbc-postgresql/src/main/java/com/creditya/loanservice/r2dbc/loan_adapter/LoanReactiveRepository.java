package com.creditya.loanservice.r2dbc.loan_adapter;

import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import lombok.NonNull;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, UUID>, ReactiveQueryByExampleExecutor<LoanEntity> {

    Flux<LoanEntity> findAllByIdStatusIn(List<UUID> statusIds);
    Mono<Long> countByIdStatusIn(List<UUID> statusIds);

    @NonNull
    Mono<Long>  count();
}
