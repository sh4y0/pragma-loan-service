package com.creditya.loanservice.usecase.utils;

import com.creditya.loanservice.model.creditanalisys.ActiveLoanDetails;
import com.creditya.loanservice.model.creditanalisys.ClientFinancialProfile;
import com.creditya.loanservice.model.creditanalisys.CreditAnalysis;
import com.creditya.loanservice.model.creditanalisys.NewLoanDetails;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AutomaticValidation {

    private final LoanRepository loanRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final LoanSQSSender loanSQSSender;
    private final UseCaseLogger logger;

    public Mono<LoanData> handle(LoanData loanData, LoanType loanType, UUID userId) {
        boolean isAutomaticValidation = loanType.getAutomaticValidation();

        if (!isAutomaticValidation) {
            logger.trace("Loan type does not have automatic validation for loan {}. Flow complete.", loanData.getIdLoan());
            return Mono.just(loanData);
        }

        logger.trace("Loan type has automatic validation for loan {}. Preparing message for Lambda analysis.", loanData.getIdLoan());
        Mono<List<ActiveLoanDetails>> activeLoansMono = loanRepository.findActiveLoansByUserId(userId).collectList();
        Mono<UserSnapshot> userMono = userSnapshotRepository.findUserById(userId);

        return Mono.zip(activeLoansMono, userMono)
                .flatMap(tuple -> {
                    List<ActiveLoanDetails> activeLoansList = tuple.getT1();
                    UserSnapshot user = tuple.getT2();
                    CreditAnalysis creditAnalysis = buildCreditAnalysisMessage(loanData, loanType, user.getBaseSalary(), activeLoansList, userId);
                    return loanSQSSender.sendCreditAnalysis(creditAnalysis)
                            .doOnSuccess(v -> logger.info("Successfully sent loan {} for automatic analysis.", loanData.getIdLoan()))
                            .thenReturn(loanData);
                });
    }

    private CreditAnalysis buildCreditAnalysisMessage(LoanData loanData,
                                                      LoanType loanType,
                                                      BigDecimal baseSalary,
                                                      List<ActiveLoanDetails> activeLoans,
                                                      UUID userId) {
        NewLoanDetails newLoanDetails = new NewLoanDetails(
                loanData.getAmount(),
                loanData.getLoanTerm(),
                loanType.getInterestRate()
        );

        ClientFinancialProfile profile = new ClientFinancialProfile(
                baseSalary,
                activeLoans
        );

        return  CreditAnalysis.builder()
                .idLoan(loanData.getIdLoan())
                .userId(userId)
                .email(loanData.getEmail())
                .newLoanDetails(newLoanDetails)
                .financialProfile(profile)
                .automaticValidation(true)
                .build();
    }
}