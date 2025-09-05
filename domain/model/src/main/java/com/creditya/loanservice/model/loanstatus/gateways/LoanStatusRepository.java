package com.creditya.loanservice.model.loanstatus.gateways;

import com.creditya.loanservice.model.loanstatus.LoanStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanStatusRepository {

    Mono<LoanStatus> findByName(String status);
    Flux<UUID> findIdsByNames(List<String> statusNames);
    Flux<LoanStatus> findByIds(List<UUID> uuids);
}
