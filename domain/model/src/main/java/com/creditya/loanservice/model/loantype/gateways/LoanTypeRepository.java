package com.creditya.loanservice.model.loantype.gateways;

import com.creditya.loanservice.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {

    Mono<LoanType> findByName(String name);
}
