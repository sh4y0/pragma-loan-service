package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.LoanAmountOutOfRangeException;
import com.creditya.loanservice.usecase.exception.LoanNotFoundException;
import com.creditya.loanservice.usecase.exception.LoanStatusNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static com.creditya.loanservice.model.loanstatus.Status.PENDING;

    @RequiredArgsConstructor
    public class LoanUseCase {

        private final LoanRepository loanRepository;
        private final LoanTypeRepository loanTypeRepository;
        private final LoanStatusRepository loanStatusRepository;
        private final UseCaseLogger logger;
        private final TransactionalGateway transactionalGateway;

        public Mono<Loan> createLoan(Loan loan, String loanTypeName) {
            logger.trace("Starting loan creation flow for DNI: {}", loan.getDni());

            return transactionalGateway.executeInTransaction(
                            loanTypeRepository.findByName(loanTypeName)
                                    .doOnNext(type -> logger.trace("Loan type found: {} (ID: {})", type.getName(), type.getIdLoanType()))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        logger.warn("Loan type not found: {}", loanTypeName);
                                        return Mono.error(new LoanNotFoundException());
                                    }))
                                    .filter(type -> {
                                        boolean inRange = loan.getAmount().compareTo(type.getMinimumAmount()) >= 0 &&
                                                loan.getAmount().compareTo(type.getMaximumAmount()) <= 0;
                                        if (!inRange) {
                                            logger.warn("Loan amount {} is out of range for loan type {}",
                                                    loan.getAmount(), loanTypeName);
                                        }
                                        return inRange;
                                    })
                                    .switchIfEmpty(Mono.error(new LoanAmountOutOfRangeException()))
                                    .flatMap(type -> {
                                        loan.setIdLoanType(type.getIdLoanType());
                                        logger.trace("Assigned loan type ID {} to loan application for DNI: {}", type.getIdLoanType(), loan.getDni());

                                        return loanStatusRepository.findByName(PENDING.getName())
                                                .doOnNext(status -> logger.trace("Initial loan status found: {} (ID: {})",
                                                        PENDING.getName(), status.getIdStatus()))
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    logger.error("Initial loan status '{}' not found", PENDING.getName());
                                                    return Mono.error(new LoanStatusNotFoundException());
                                                }))
                                                .flatMap(status -> {
                                                    loan.setIdStatus(status.getIdStatus());
                                                    logger.trace("Assigned status ID {} to loan application for DNI: {}", status.getIdStatus(), loan.getDni());
                                                    return loanRepository.createLoan(loan)
                                                            .doOnSuccess(app -> logger.info("Loan application created successfully for DNI: {}", app.getDni()));
                                                });
                                    })
                    )
                    .doOnError(err -> logger.error("Error creating loan application for DNI {}: {}",
                            loan.getDni(), err.getMessage()));
        }
    }

