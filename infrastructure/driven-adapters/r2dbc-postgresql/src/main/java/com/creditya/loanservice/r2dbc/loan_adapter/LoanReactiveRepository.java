package com.creditya.loanservice.r2dbc.loan_adapter;

import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, UUID>, ReactiveQueryByExampleExecutor<LoanEntity> {

}
