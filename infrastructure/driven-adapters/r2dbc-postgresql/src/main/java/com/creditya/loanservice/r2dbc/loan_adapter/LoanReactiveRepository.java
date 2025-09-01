package com.creditya.loanservice.r2dbc.loan_adapter;

import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, UUID>, ReactiveQueryByExampleExecutor<LoanEntity> {
    @Query("""
        SELECT * FROM loan
        WHERE id_status IN (:statusIds)
    """)
    Flux<LoanEntity> findAllByIdStatusIn(List<UUID> statusIds);

    @Query("""
        SELECT COUNT(*) FROM loan
        WHERE id_status IN (:statusIds)
    """)
    Mono<Long> countByIdStatusIn(List<UUID> statusIds);
}
