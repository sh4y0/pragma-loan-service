package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanNotification;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.loan.responseEvent.LoanStatusUpdateEvent;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SendNotificationUseCase {

    private final LoanSQSSender loanSQSSender;
    private final UseCaseLogger useCaseLogger;

    public Mono<String> sendAutomaticResultNotification(LoanStatusUpdateEvent analysisResult) {
        useCaseLogger.trace("Preparing automatic notification for loan {}", analysisResult.idLoan());
        return loanSQSSender.sendStatusNotificationCredit(analysisResult);
    }

    public Mono<String> sendManualUpdateNotification(Loan updatedLoan, String email, String status) {
        useCaseLogger.trace("Preparing manual notification for loan {}", updatedLoan.getIdLoan());
        LoanNotification notification = LoanNotification.builder()
                .idLoan(updatedLoan.getIdLoan())
                .status(status)
                .email(email)
                .automaticValidation(false)
                .build();
        return loanSQSSender.sendStatusNotification(notification);
    }
}