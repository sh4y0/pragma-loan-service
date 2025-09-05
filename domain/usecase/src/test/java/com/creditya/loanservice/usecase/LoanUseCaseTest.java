package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoanUseCaseTest {

    private LoanRepository loanRepository;
    private LoanTypeRepository loanTypeRepository;
    private LoanStatusRepository loanStatusRepository;
    private TransactionalGateway transactionalGateway;

    private LoanUseCase loanUseCase;

    private final UUID MOCK_USER_ID = UUID.randomUUID();
    private final String MOCK_DNI = "12345678A";

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        loanTypeRepository = mock(LoanTypeRepository.class);
        loanStatusRepository = mock(LoanStatusRepository.class);
        UseCaseLogger logger = mock(UseCaseLogger.class);
        transactionalGateway = mock(TransactionalGateway.class);

        loanUseCase = new LoanUseCase(
                loanRepository,
                loanTypeRepository,
                loanStatusRepository,
                logger,
                transactionalGateway
        );
    }

    private Loan buildLoan(String dni, BigDecimal amount) {
        Loan loan = new Loan();
        loan.setDni(dni);
        loan.setAmount(amount);
        loan.setEmail("test@test.com");
        loan.setLoanTerm(12);
        return loan;
    }

    private LoanType buildLoanType(BigDecimal min, BigDecimal max) {
        LoanType type = new LoanType();
        type.setIdLoanType(UUID.randomUUID());
        type.setName("PERSONAL");
        type.setMinimumAmount(min);
        type.setMaximumAmount(max);
        return type;
    }

    private LoanStatus buildLoanStatus(String name) {
        LoanStatus status = new LoanStatus();
        status.setIdStatus(UUID.randomUUID());
        status.setName(name);
        return status;
    }

    @Test
    void createLoan_success() {
        Loan loan = buildLoan(MOCK_DNI, BigDecimal.valueOf(5000));
        LoanType type = buildLoanType(BigDecimal.valueOf(1000), BigDecimal.valueOf(10000));
        LoanStatus status = buildLoanStatus("PENDING");

        Loan loanSaved = new Loan();
        loanSaved.setLoanId(UUID.randomUUID());
        loanSaved.setAmount(loan.getAmount());
        loanSaved.setLoanTerm(loan.getLoanTerm());
        loanSaved.setEmail(loan.getEmail());
        loanSaved.setDni(loan.getDni());

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(type));
        when(loanStatusRepository.findByName(anyString())).thenReturn(Mono.just(status));
        when(loanRepository.createLoan(any(Loan.class))).thenReturn(Mono.just(loanSaved));
        when(transactionalGateway.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        loanUseCase.createLoan(loan, "PERSONAL")
                                .contextWrite(ctx -> ctx.put("userId", MOCK_USER_ID.toString()).put("userDni", MOCK_DNI))
                )
                .expectNextMatches(loanData ->
                        loanData.getDni().equals(MOCK_DNI) &&
                                loanData.getLoanType().equals("PERSONAL") &&
                                loanData.getStatus().equals("PENDING")
                )
                .verifyComplete();
    }

    @Test
    void createLoan_unauthorizedUser() {
        Loan loan = buildLoan("11111111B", BigDecimal.valueOf(5000));

        StepVerifier.create(
                        loanUseCase.createLoan(loan, "PERSONAL")
                                .contextWrite(ctx -> ctx.put("userId", MOCK_USER_ID.toString()).put("userDni", MOCK_DNI))
                )
                .expectError(UnauthorizedLoanApplicationException.class)
                .verify();
    }

    @Test
    void createLoan_loanTypeNotFound() {
        Loan loan = buildLoan(MOCK_DNI, BigDecimal.valueOf(5000));

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.empty());
        when(transactionalGateway.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        loanUseCase.createLoan(loan, "UNKNOWN")
                                .contextWrite(ctx -> ctx.put("userId", MOCK_USER_ID.toString()).put("userDni", MOCK_DNI))
                )
                .expectError(LoanTypeNotFoundException.class)
                .verify();
    }

    @Test
    void createLoan_amountOutOfRange() {
        Loan loan = buildLoan(MOCK_DNI, BigDecimal.valueOf(20000));
        LoanType type = buildLoanType(BigDecimal.valueOf(1000), BigDecimal.valueOf(10000));

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(type));
        when(transactionalGateway.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        loanUseCase.createLoan(loan, "PERSONAL")
                                .contextWrite(ctx -> ctx.put("userId", MOCK_USER_ID.toString()).put("userDni", MOCK_DNI))
                )
                .expectError(LoanAmountOutOfRangeException.class)
                .verify();
    }

    @Test
    void createLoan_statusNotFound() {
        Loan loan = buildLoan(MOCK_DNI, BigDecimal.valueOf(5000));
        LoanType type = buildLoanType(BigDecimal.valueOf(1000), BigDecimal.valueOf(10000));

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(type));
        when(loanStatusRepository.findByName(anyString())).thenReturn(Mono.empty());
        when(transactionalGateway.executeInTransaction(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        loanUseCase.createLoan(loan, "PERSONAL")
                                .contextWrite(ctx -> ctx.put("userId", MOCK_USER_ID.toString()).put("userDni", MOCK_DNI))
                )
                .expectError(LoanStatusNotFoundException.class)
                .verify();
    }
}
