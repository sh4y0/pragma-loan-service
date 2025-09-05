package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPageSize;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GetPaginationLoanUseCaseTest {

    private LoanRepository loanRepository;
    private LoanStatusRepository loanStatusRepository;
    private UserSnapshotRepository userSnapshotRepository;
    private LoanTypeRepository loanTypeRepository;
    private LoanCalculator loanCalculator;
    private UseCaseLogger logger;

    private GetPaginationLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        loanStatusRepository = mock(LoanStatusRepository.class);
        userSnapshotRepository = mock(UserSnapshotRepository.class);
        loanTypeRepository = mock(LoanTypeRepository.class);
        loanCalculator = mock(LoanCalculator.class);
        logger = mock(UseCaseLogger.class);

        useCase = new GetPaginationLoanUseCase(
                loanRepository,
                loanStatusRepository,
                userSnapshotRepository,
                loanTypeRepository,
                loanCalculator,
                logger
        );
    }

    private Loan buildLoan(UUID userId, UUID loanTypeId, UUID statusId, BigDecimal amount) {
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setUserId(userId);
        loan.setIdLoanType(loanTypeId);
        loan.setIdStatus(statusId);
        loan.setAmount(amount);
        return loan;
    }

    private UserSnapshot buildUser(UUID id) {
        UserSnapshot user = new UserSnapshot();
        user.setUserId(id);
        user.setName("User " + id.toString());
        return user;
    }

    private LoanType buildLoanType(UUID id, BigDecimal rate) {
        LoanType type = new LoanType();
        type.setIdLoanType(id);
        type.setName("Type " + id.toString());
        type.setInterestRate(rate);
        return type;
    }

    private LoanStatus buildLoanStatus(UUID id, String name) {
        LoanStatus status = new LoanStatus();
        status.setIdStatus(id);
        status.setName(name);
        return status;
    }

    @Test
    void execute_success_withFilterStatuses() {
        UUID statusId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loanTypeId = UUID.randomUUID();

        Loan loan = buildLoan(userId, loanTypeId, statusId, BigDecimal.valueOf(1000));
        UserSnapshot user = buildUser(userId);
        LoanType type = buildLoanType(loanTypeId, BigDecimal.valueOf(5.0));
        LoanStatus status = buildLoanStatus(statusId, "UNDER_REVIEW");

        List<String> filters = List.of("UNDER_REVIEW");

        when(loanStatusRepository.findIdsByNames(filters)).thenReturn(Flux.just(statusId));
        when(loanRepository.countLoansByStatusIds(anyList())).thenReturn(Mono.just(1L));
        when(loanRepository.findLoansByStatusIds(anyList())).thenReturn(Flux.just(loan));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.just(user));
        when(loanTypeRepository.findByIds(anyList())).thenReturn(Flux.just(type));
        when(loanStatusRepository.findByIds(anyList())).thenReturn(Flux.just(status));
        when(loanCalculator.calculateTotalMonthlyDebt(any(), anyList(), anyMap(), anyMap())).thenReturn(BigDecimal.valueOf(2000));
        when(loanCalculator.calculateApprovedLoansCount(any(), anyList(), anyMap())).thenReturn(1L);

        StepVerifier.create(useCase.execute(0, 10, filters))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().getFirst().getLoanStatusName().equals("UNDER_REVIEW")
                        && page.getContent().getFirst().getLoanTypeName().equals(type.getName()))
                .verifyComplete();

        verify(logger, times(1)).trace(eq("Resolved {} status IDs from {} filter statuses"), eq(1), eq(1));
        verifyNoMoreInteractions(logger);

        verify(loanStatusRepository, times(1)).findIdsByNames(filters);
        verify(loanRepository, times(1)).countLoansByStatusIds(anyList());
        verify(loanRepository, times(1)).findLoansByStatusIds(anyList());
        verify(userSnapshotRepository, times(1)).findUsersByIds(anyList());
        verify(loanTypeRepository, times(1)).findByIds(anyList());
        verify(loanStatusRepository, times(1)).findByIds(anyList());
    }

    @Test
    void execute_success_withoutFilterStatuses() {
        UUID statusId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID loanTypeId = UUID.randomUUID();

        Loan loan = buildLoan(userId, loanTypeId, statusId, BigDecimal.valueOf(1000));
        UserSnapshot user = buildUser(userId);
        LoanType type = buildLoanType(loanTypeId, BigDecimal.valueOf(5.0));
        LoanStatus status = buildLoanStatus(statusId, "PENDING");

        when(loanRepository.countAllLoans()).thenReturn(Mono.just(1L));
        when(loanRepository.findAllLoans()).thenReturn(Flux.just(loan));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.just(user));
        when(loanTypeRepository.findByIds(anyList())).thenReturn(Flux.just(type));
        when(loanStatusRepository.findByIds(anyList())).thenReturn(Flux.just(status));
        when(loanCalculator.calculateTotalMonthlyDebt(any(), anyList(), anyMap(), anyMap())).thenReturn(BigDecimal.valueOf(2000));
        when(loanCalculator.calculateApprovedLoansCount(any(), anyList(), anyMap())).thenReturn(1L);

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().get(0).getLoanStatusName().equals("PENDING"))
                .verifyComplete();

        verify(loanRepository, times(1)).countAllLoans();
        verify(loanRepository, times(1)).findAllLoans();
    }

    @Test
    void execute_emptyLoans() {
        when(loanRepository.countAllLoans()).thenReturn(Mono.just(0L));
        when(loanRepository.findAllLoans()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().isEmpty() && page.getTotalElements() == 0)
                .verifyComplete();
    }

    @Test
    void execute_invalidPagination_shouldThrow() {
        StepVerifier.create(Mono.fromCallable(() -> useCase.execute(-1, 10, null)))
                .expectError(IndexOutOfBoundsExceptionPage.class)
                .verify();

        StepVerifier.create(Mono.fromCallable(() -> useCase.execute(0, 0, null)))
                .expectError(IndexOutOfBoundsExceptionPageSize.class)
                .verify();
    }

    @Test
    void execute_enrichmentHandlesMissingDataGracefully() {
        UUID userId = UUID.randomUUID();
        UUID loanTypeId = UUID.randomUUID();
        UUID statusId = UUID.randomUUID();

        Loan loan = buildLoan(userId, loanTypeId, statusId, BigDecimal.valueOf(1000));

        when(loanRepository.countAllLoans()).thenReturn(Mono.just(1L));
        when(loanRepository.findAllLoans()).thenReturn(Flux.just(loan));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.empty());
        when(loanTypeRepository.findByIds(anyList())).thenReturn(Flux.empty());
        when(loanStatusRepository.findByIds(anyList())).thenReturn(Flux.empty());
        when(loanCalculator.calculateTotalMonthlyDebt(any(), anyList(), anyMap(), anyMap())).thenReturn(BigDecimal.ZERO);
        when(loanCalculator.calculateApprovedLoansCount(any(), anyList(), anyMap())).thenReturn(0L);

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().getFirst().getUserSnapshot() == null
                        && page.getContent().getFirst().getLoanTypeName() == null
                        && page.getContent().getFirst().getLoanStatusName() == null)
                .verifyComplete();
    }

    @Test
    void execute_logsErrorOnFailure() {
        RuntimeException ex = new RuntimeException("Repository failed");
        when(loanStatusRepository.findIdsByNames(anyList())).thenReturn(Flux.error(ex));

        StepVerifier.create(useCase.execute(0, 10, List.of("UNDER_REVIEW")))
                .expectError(RuntimeException.class)
                .verify();

        verify(logger, times(1)).trace(eq("Error executing GetLoanUnderReviewUseCase"), eq(ex));
    }
}
