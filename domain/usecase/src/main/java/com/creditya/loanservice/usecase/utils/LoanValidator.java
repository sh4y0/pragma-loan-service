package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.LoanAmountOutOfRangeException;
import com.creditya.loanservice.usecase.exception.LoanTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanValidator {

    private final LoanTypeRepository loanTypeRepository;
    private final UseCaseLogger logger;

    public Mono<LoanType> validate(Loan loan, String loanTypeName) {
        return loanTypeRepository.findByName(loanTypeName)
                .doOnNext(type -> logger.trace("Loan type found: {} (ID: {})", type.getName(), type.getIdLoanType()))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Loan type not found: {}", loanTypeName);
                    return Mono.error(new LoanTypeNotFoundException());
                }))
                .filter(type -> isAmountInRange(loan, type, loanTypeName))
                .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()));
    }

    private boolean isAmountInRange(Loan loan, LoanType type, String loanTypeName) {
        boolean inRange = loan.getAmount().compareTo(type.getMinimumAmount()) >= 0 &&
                loan.getAmount().compareTo(type.getMaximumAmount()) <= 0;
        if (!inRange) {
            logger.warn("Loan amount {} is out of range for loan type {}", loan.getAmount(), loanTypeName);
        }
        return inRange;
    }
}