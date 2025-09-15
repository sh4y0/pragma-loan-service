package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.creditanalisys.ActiveLoanDetails;
import com.creditya.loanservice.model.creditanalisys.CreditAnalysis;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.LoanAmountOutOfRangeException;
import com.creditya.loanservice.usecase.exception.LoanStatusNotFoundException;
import com.creditya.loanservice.usecase.exception.LoanTypeNotFoundException;
import com.creditya.loanservice.usecase.exception.UnauthorizedLoanApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.when;

class CreateLoanUseCaseTest {

    @Mock private LoanRepository loanRepository;
    @Mock private LoanTypeRepository loanTypeRepository;
    @Mock private LoanStatusRepository loanStatusRepository;
    @Mock private UserSnapshotRepository userSnapshotRepository;
    @Mock private LoanSQSSender loanSQSSender;
    @Mock private TransactionalGateway transactionalGateway;
    @Mock private UseCaseLogger logger;

    @InjectMocks
    private CreateLoanUseCase createLoanUseCase;

    private final UUID userId = UUID.randomUUID();
    private final String dni = "12345678";

    private Loan loan;
    private LoanType loanType;
    private LoanStatus loanStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loan = Loan.builder()
                .dni(dni)
                .amount(BigDecimal.valueOf(1000))
                .loanTerm(12)
                .email("test@email.com")
                .build();

        loanType = LoanType.builder()
                .idLoanType(UUID.randomUUID())
                .name("personal")
                .minimumAmount(BigDecimal.valueOf(500))
                .maximumAmount(BigDecimal.valueOf(2000))
                .interestRate(BigDecimal.valueOf(0.1))
                .automaticValidation(false)
                .build();

        loanStatus = LoanStatus.builder()
                .idStatus(UUID.randomUUID())
                .name("PENDING")
                .build();

        when(transactionalGateway.executeInTransaction(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createLoan_success_withoutAutomaticValidation() {
        when(loanTypeRepository.findByName("personal")).thenReturn(Mono.just(loanType));
        when(loanStatusRepository.findByName("PENDING")).thenReturn(Mono.just(loanStatus));
        when(loanRepository.createLoan(any())).thenReturn(Mono.just(loan));

        LoanData result = createLoanUseCase.createLoan(loan, "personal")
                .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                .block();

        assertNotNull(result);
        assertEquals(loan.getDni(), result.getDni());
        assertEquals("personal", result.getLoanType());
        assertEquals("PENDING", result.getStatus());

        verify(loanRepository).createLoan(any());
        verifyNoInteractions(loanSQSSender);
    }

    @Test
    void createLoan_success_withAutomaticValidation() {
        // given
        loanType.setAutomaticValidation(true);

        when(loanTypeRepository.findByName("personal")).thenReturn(Mono.just(loanType));
        when(loanStatusRepository.findByName("PENDING")).thenReturn(Mono.just(loanStatus));
        when(loanRepository.createLoan(any())).thenReturn(Mono.just(loan));
        when(loanRepository.findActiveLoansByUserId(userId)).thenReturn(Flux.just(new ActiveLoanDetails()));
        when(userSnapshotRepository.findUserById(userId)).thenReturn(Mono.just(new UserSnapshot(userId, BigDecimal.valueOf(3000))));
        when(loanSQSSender.sendCreditAnalysis(any(CreditAnalysis.class))).thenReturn(Mono.empty());

        // when
        LoanData result = createLoanUseCase.createLoan(loan, "personal")
                .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                .block();

        // then
        assertNotNull(result);
        verify(loanSQSSender).sendCreditAnalysis(any(CreditAnalysis.class));
    }

    @Test
    void createLoan_fail_unauthorizedDni() {
        // given
        String otherDni = "99999999";

        Loan invalidLoan = Loan.builder()
                .dni(otherDni)
                .amount(BigDecimal.valueOf(1000))
                .build();

        // when / then
        assertThrows(UnauthorizedLoanApplicationException.class, () ->
                createLoanUseCase.createLoan(invalidLoan, "personal")
                        .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                        .block()
        );
    }

    @Test
    void createLoan_fail_loanTypeNotFound() {
        when(loanTypeRepository.findByName("personal")).thenReturn(Mono.empty());

        assertThrows(LoanTypeNotFoundException.class, () ->
                createLoanUseCase.createLoan(loan, "personal")
                        .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                        .block()
        );
    }

    @Test
    void createLoan_fail_amountOutOfRange() {
        loan.setAmount(BigDecimal.valueOf(10000)); // fuera de rango

        when(loanTypeRepository.findByName("personal")).thenReturn(Mono.just(loanType));

        assertThrows(LoanAmountOutOfRangeException.class, () ->
                createLoanUseCase.createLoan(loan, "personal")
                        .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                        .block()
        );
    }

    @Test
    void createLoan_fail_statusNotFound() {
        when(loanTypeRepository.findByName("personal")).thenReturn(Mono.just(loanType));
        when(loanStatusRepository.findByName("PENDING")).thenReturn(Mono.empty());

        assertThrows(LoanStatusNotFoundException.class, () ->
                createLoanUseCase.createLoan(loan, "personal")
                        .contextWrite(Context.of("userId", userId.toString(), "userDni", dni))
                        .block()
        );
    }
}
