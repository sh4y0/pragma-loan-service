package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanApprovedEvent;
import com.creditya.loanservice.model.loan.data.LoanDecision;
import com.creditya.loanservice.model.loan.responseEvent.LoanStatusUpdateEvent;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class LoanDecisionProcessorUseCase {

    private final UpdateLoanUseCase updateLoanUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final UseCaseLogger useCaseLogger;
    private final SendLoanApprovedEventUseCase sendLoanApprovedEventUseCase;

    private static final String APPROVED_STATUS = "Approved";

    public Mono<LoanDecision> processAutomaticAnalysisResult(LoanStatusUpdateEvent analysisResult) {
        useCaseLogger.info("Processing automatic analysis result for loan {}", analysisResult.idLoan());

        LoanDecision decisionToUpdate = new LoanDecision(
                analysisResult.idLoan(),
                analysisResult.status(),
                true
        );

        return updateLoanUseCase.updateLoan(decisionToUpdate)
                .flatMap(updatedLoan -> handleApprovalEvent(updatedLoan, decisionToUpdate))
                .flatMap(decisionAfterEvent ->
                        sendNotificationUseCase.sendAutomaticResultNotification(analysisResult)
                                .thenReturn(decisionAfterEvent)
                );
    }

    public Mono<LoanDecision> processManualUpdate(LoanDecision manualUpdateRequest) {
        useCaseLogger.info("Processing manual update for loan {}", manualUpdateRequest.getIdLoan());

        return updateLoanUseCase.updateLoan(manualUpdateRequest)
                .flatMap(updatedLoan -> {
                    Mono<LoanDecision> decisionMono = handleApprovalEvent(updatedLoan, manualUpdateRequest);
                    return Mono.zip(Mono.just(updatedLoan), decisionMono);
                })
                .flatMap(tuple -> {
                    Loan updatedLoan = tuple.getT1();
                    LoanDecision decisionAfterEvent = tuple.getT2();

                    return sendNotificationUseCase.sendManualUpdateNotification(
                                    updatedLoan, updatedLoan.getEmail(), manualUpdateRequest.getStatus())
                            .thenReturn(decisionAfterEvent);
                });
    }

    private Mono<LoanDecision> handleApprovalEvent(Loan updatedLoan, LoanDecision decision) {
        useCaseLogger.info("Handling approval event flow for loan {}", updatedLoan.getIdLoan());

        if (APPROVED_STATUS.equals(decision.getStatus())) {
            useCaseLogger.info("Loan {} approved, sending LoanApprovedEvent", updatedLoan.getIdLoan());

            LoanApprovedEvent event = LoanApprovedEvent.builder()
                    .idLoan(updatedLoan.getIdLoan())
                    .status(decision.getStatus())
                    .amountApproved(updatedLoan.getAmount())
                    .approvedAt(LocalDateTime.now().toString())
                    .build();

            return sendLoanApprovedEventUseCase.execute(event)
                    .doOnSuccess(e -> useCaseLogger.info("LoanApprovedEvent sent for loan {}", updatedLoan.getIdLoan()))
                    .thenReturn(decision);
        }

        useCaseLogger.info("Loan {} not approved, skipping LoanApprovedEvent", updatedLoan.getIdLoan());
        return Mono.just(decision);
    }
}