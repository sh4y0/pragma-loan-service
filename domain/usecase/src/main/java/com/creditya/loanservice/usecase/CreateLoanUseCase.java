package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.LoanStatusNotFoundException;
import com.creditya.loanservice.usecase.exception.UnauthorizedLoanApplicationException;
import com.creditya.loanservice.usecase.utils.AutomaticValidation;
import com.creditya.loanservice.usecase.utils.LoanValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.creditya.loanservice.model.loanstatus.Status.PENDING;

@RequiredArgsConstructor
public class CreateLoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final LoanValidator loanValidator;
    private final AutomaticValidation automaticValidation;
    private final TransactionalGateway transactionalGateway;
    private final UseCaseLogger logger;

    public Mono<LoanData> createLoan(Loan loan, String loanTypeName) {
        return Mono.deferContextual(context -> {
                    String userId = context.get("userId");
                    String dni = context.get("userDni");

                    authorize(loan.getDni(), dni);
                    logger.trace("Starting loan creation flow for DNI: {} (userId: {})", loan.getDni(), userId);

                    return transactionalGateway.executeInTransaction(
                            loanValidator.validate(loan, loanTypeName)
                                    .flatMap(loanType -> createAndProcessLoan(loan, loanType, UUID.fromString(userId)))
                    );
                })
                .doOnError(err -> logger.error("Error creating loan application for DNI {}: {}", loan.getDni(), err.getMessage()));
    }

    private void authorize(String loanDni, String userDni) {
        if (!loanDni.equals(userDni)) {
            throw new UnauthorizedLoanApplicationException();
        }
    }

    private Mono<LoanData> createAndProcessLoan(Loan loan, LoanType loanType, UUID userId) {
        return findPendingStatus()
                .flatMap(status -> {
                    enrichLoan(loan, loanType, status, userId);
                    return loanRepository.createLoan(loan)
                            .doOnSuccess(app -> logger.info("Loan application created successfully for DNI: {}", app.getDni()))
                            .map(savedLoan -> buildLoanData(savedLoan, loanType, status))
                            .flatMap(loanData -> automaticValidation.handle(loanData, loanType, userId));
                });
    }

    private Mono<LoanStatus> findPendingStatus() {
        return loanStatusRepository.findByName(PENDING.getName())
                .doOnNext(status -> logger.trace("Initial loan status found: {} (ID: {})", PENDING.getName(), status.getIdStatus()))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.error("Initial loan status '{}' not found", PENDING.getName());
                    return Mono.error(new LoanStatusNotFoundException());
                }));
    }

    private void enrichLoan(Loan loan, LoanType loanType, LoanStatus status, UUID userId) {
        loan.setIdLoanType(loanType.getIdLoanType());
        loan.setIdStatus(status.getIdStatus());
        loan.setUserId(userId);
        logger.trace("Enriched loan application for DNI: {} with LoanTypeID: {}, StatusID: {}, UserID: {}",
                loan.getDni(), loanType.getIdLoanType(), status.getIdStatus(), userId);
    }

    private LoanData buildLoanData(Loan loan, LoanType loanType, LoanStatus status) {
        return LoanData.builder()
                .idLoan(loan.getIdLoan())
                .amount(loan.getAmount())
                .loanTerm(loan.getLoanTerm())
                .email(loan.getEmail())
                .dni(loan.getDni())
                .status(status.getName())
                .loanType(loanType.getName())
                .build();
    }
}