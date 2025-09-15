package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPageSize;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPaginationCreateLoanUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanStatusRepository loanStatusRepository;

    @Mock
    private UserSnapshotRepository userSnapshotRepository;

    @Mock
    private LoanCalculator loanCalculator;

    @Mock
    private UseCaseLogger logger;

    @InjectMocks
    private GetPaginationLoanUseCase useCase;

    private LoanJoinedProjection buildLoan(UUID loanId, UUID userId, BigDecimal amount) {
        return new LoanJoinedProjection(
                loanId,
                userId,
                amount,
                12,
                "test@example.com",
                "12345678",
                "PENDING",
                "PERSONAL",
                BigDecimal.valueOf(5.0)
        );
    }

    private UserSnapshot buildUser(UUID userId) {
        UserSnapshot user = new UserSnapshot();
        user.setUserId(userId);
        user.setName("User " + userId);
        return user;
    }

    @Test
    void execute_success_withFilterStatuses() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LoanJoinedProjection loan = buildLoan(loanId,userId, BigDecimal.valueOf(1000));
        List<String> filters = List.of("APPROVED");
        List<UUID> statusIds = List.of(UUID.randomUUID());

        when(loanStatusRepository.findIdsByNames(filters)).thenReturn(Flux.fromIterable(statusIds));
        when(loanRepository.findLoansWithTypeAndStatus(any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(loan));
        when(loanRepository.countLoansByStatusIds(anyList())).thenReturn(Mono.just(1L));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.just(buildUser(userId)));
        when(loanCalculator.calculateTotalMonthlyDebt(any())).thenReturn(BigDecimal.valueOf(2000));
        when(loanCalculator.calculateApprovedLoansCount(any())).thenReturn(1L);

        StepVerifier.create(useCase.execute(0, 10, filters))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().getFirst().getIdLoan().equals(loanId)
                        && page.getContent().getFirst().getUserSnapshot() != null)
                .verifyComplete();
    }

    @Test
    void execute_success_withoutFilterStatuses() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LoanJoinedProjection loan = buildLoan(userId, loanId, BigDecimal.valueOf(1000));

        when(loanRepository.findLoansWithTypeAndStatus(any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(loan));
        when(loanRepository.countAllLoans()).thenReturn(Mono.just(1L));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.just(buildUser(userId)));
        when(loanCalculator.calculateTotalMonthlyDebt(any())).thenReturn(BigDecimal.valueOf(2000));
        when(loanCalculator.calculateApprovedLoansCount(any())).thenReturn(1L);

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().getFirst().getLoanStatusName().equals("PENDING"))
                .verifyComplete();
    }

    @Test
    void execute_emptyLoans() {
        when(loanRepository.findLoansWithTypeAndStatus(any(), anyInt(), anyInt()))
                .thenReturn(Flux.empty());
        when(loanRepository.countAllLoans()).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().isEmpty() && page.getTotalElements() == 0)
                .verifyComplete();
    }

    @Test
    void execute_invalidPagination_shouldThrowReactive() {
        StepVerifier.create(Mono.defer(() -> useCase.execute(-1, 10, Collections.emptyList())))
                .expectErrorMatches(IndexOutOfBoundsExceptionPage.class::isInstance)
                .verify();

        StepVerifier.create(Mono.defer(() -> useCase.execute(0, 0, Collections.emptyList())))
                .expectErrorMatches(IndexOutOfBoundsExceptionPageSize.class::isInstance)
                .verify();
    }

    @Test
    void execute_enrichmentHandlesMissingUserData() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LoanJoinedProjection loan = buildLoan(userId, loanId, BigDecimal.valueOf(1000));

        when(loanRepository.findLoansWithTypeAndStatus(any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(loan));
        when(loanRepository.countAllLoans()).thenReturn(Mono.just(1L));
        when(userSnapshotRepository.findUsersByIds(anyList())).thenReturn(Flux.empty());
        when(loanCalculator.calculateTotalMonthlyDebt(any())).thenReturn(BigDecimal.ZERO);
        when(loanCalculator.calculateApprovedLoansCount(any())).thenReturn(0L);

        StepVerifier.create(useCase.execute(0, 10, Collections.emptyList()))
                .expectNextMatches(page -> page.getContent().size() == 1
                        && page.getContent().getFirst().getUserSnapshot() == null)
                .verifyComplete();
    }

    @Test
    void execute_logsErrorOnRepositoryFailure() {
        RuntimeException ex = new RuntimeException("Repository failed");
        when(loanStatusRepository.findIdsByNames(anyList())).thenReturn(Flux.error(ex));

        StepVerifier.create(useCase.execute(0, 10, List.of("APPROVED")))
                .expectError(RuntimeException.class)
                .verify();
    }
}
