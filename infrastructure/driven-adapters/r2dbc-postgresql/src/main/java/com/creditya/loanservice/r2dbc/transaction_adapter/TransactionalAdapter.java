package com.creditya.loanservice.r2dbc.transaction_adapter;

import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionalAdapter implements TransactionalGateway {

    private final TransactionalOperator transactionalOperator;

    @Override
    public <T> Mono<T> executeInTransaction(Mono<T> action) {
        return transactionalOperator.transactional(action);
    }
}