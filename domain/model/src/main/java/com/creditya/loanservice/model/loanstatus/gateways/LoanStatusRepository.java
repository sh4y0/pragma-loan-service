package com.creditya.loanservice.model.loanstatus.gateways;

import com.creditya.loanservice.model.loanstatus.LoanStatus;
import reactor.core.publisher.Mono;

public interface LoanStatusRepository {

    Mono<LoanStatus> findByName(String status);
}
