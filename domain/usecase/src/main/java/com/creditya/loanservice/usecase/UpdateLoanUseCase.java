package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanDecision;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.LoanNotFoundException;
import com.creditya.loanservice.usecase.exception.LoanStatusNotFoundException; // Corregido el tipo de excepción
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateLoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final UseCaseLogger useCaseLogger;

    public Mono<Loan> updateLoan(LoanDecision loanDecision) {
        useCaseLogger.trace("Updating loan {} to status {}", loanDecision.getIdLoan(), loanDecision.getStatus());

        Mono<Loan> loanMono = findLoanOrFail(loanDecision.getIdLoan());
        Mono<LoanStatus> statusMono = findLoanStatusOrFail(loanDecision.getStatus());

        return Mono.zip(loanMono, statusMono)
                .flatMap(this::updateAndSaveLoan)
                .doOnSuccess(updatedLoan -> useCaseLogger.trace("Successfully updated loan {}", updatedLoan.getIdLoan()));
    }

    private Mono<Loan> findLoanOrFail(UUID loanId) {
        return loanRepository.findLoanById(loanId)
                .switchIfEmpty(Mono.error(new LoanNotFoundException()));
    }

    private Mono<LoanStatus> findLoanStatusOrFail(String statusName) {
        return loanStatusRepository.findByName(statusName)
                .switchIfEmpty(Mono.error(new LoanStatusNotFoundException()));
    }

    private Mono<Loan> updateAndSaveLoan(Tuple2<Loan, LoanStatus> loanAndStatusTuple) {
        Loan loanToUpdate = loanAndStatusTuple.getT1();
        LoanStatus newStatus = loanAndStatusTuple.getT2();

        loanToUpdate.setIdStatus(newStatus.getIdStatus());
        return loanRepository.createLoan(loanToUpdate);
    }
}