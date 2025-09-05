package com.creditya.loanservice.r2dbc.loan_type_adapter;

import com.creditya.loanservice.r2dbc.entity.LoanTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, String>, ReactiveQueryByExampleExecutor<LoanTypeEntity> {
    Mono<LoanTypeEntity> findByName(String name);
    Flux<LoanTypeEntity> findAllById(List<UUID> ids);
}
