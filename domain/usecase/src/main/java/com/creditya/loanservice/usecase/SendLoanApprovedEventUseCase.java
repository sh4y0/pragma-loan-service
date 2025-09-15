package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanApprovedEvent;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SendLoanApprovedEventUseCase {

    private final LoanSQSSender loanSQSSender;
    private final UseCaseLogger useCaseLogger;

    public Mono<Void> execute(LoanApprovedEvent approvedLoan) {
        useCaseLogger.info("Sending 'Loan Approved' event for loan {}", approvedLoan.idLoan());
        return loanSQSSender.sendLoanApprovedEvent(approvedLoan)
                .then();
    }
}
