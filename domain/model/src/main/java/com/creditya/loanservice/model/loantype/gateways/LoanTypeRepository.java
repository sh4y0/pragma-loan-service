package com.creditya.loanservice.model.loantype.gateways;

import com.creditya.loanservice.model.loantype.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanTypeRepository {
    Mono<LoanType> findByName(String name);
    Flux<LoanType> findByIds(List<UUID> uuids);
}
