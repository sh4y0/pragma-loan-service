package com.creditya.loanservice.r2dbc.loan_status_adapter;

import com.creditya.loanservice.r2dbc.entity.LoanStatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanStatusReactiveRepository extends ReactiveCrudRepository<LoanStatusEntity, UUID>, ReactiveQueryByExampleExecutor<LoanStatusEntity> {
    Mono<LoanStatusEntity> findByName(String name);
    Flux<LoanStatusEntity> findByNameIn(List<String> statusNames);
}
