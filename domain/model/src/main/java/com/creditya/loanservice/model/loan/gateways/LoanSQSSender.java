package com.creditya.loanservice.model.loan.gateways;

import com.creditya.loanservice.model.creditanalisys.CreditAnalysis;
import com.creditya.loanservice.model.loan.data.LoanApprovedEvent;
import com.creditya.loanservice.model.loan.data.LoanNotification;
import com.creditya.loanservice.model.loan.responseEvent.LoanStatusUpdateEvent;
import reactor.core.publisher.Mono;

public interface LoanSQSSender {
    Mono<String> sendStatusNotificationCredit(LoanStatusUpdateEvent creditAnalysis);
    Mono<String> sendStatusNotification(LoanNotification loanNotification);
    Mono<String> sendCreditAnalysis(CreditAnalysis creditAnalysis);
    Mono<String> sendLoanApprovedEvent(LoanApprovedEvent approvedLoan);
}
