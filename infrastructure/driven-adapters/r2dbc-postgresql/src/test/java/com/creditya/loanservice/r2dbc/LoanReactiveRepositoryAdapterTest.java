package com.creditya.loanservice.r2dbc;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import com.creditya.loanservice.r2dbc.loan_adapter.LoanReactiveRepository;
import com.creditya.loanservice.r2dbc.loan_adapter.LoanReactiveRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanReactiveRepositoryAdapterTest {

    @Mock
    private LoanReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private LoanReactiveRepositoryAdapter adapter;

    @Test
    void createLoan_delegatesToRepositoryAndMaps() {
        adapter = new LoanReactiveRepositoryAdapter(repository, mapper);

        Loan loan = new Loan();
        LoanEntity entity = new LoanEntity();
        Loan mappedLoan = new Loan();

        doReturn(entity).when(mapper).map(loan, LoanEntity.class);
        doReturn(mappedLoan).when(mapper).map(entity, Loan.class);

        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.createLoan(loan))
                .expectNext(mappedLoan)
                .verifyComplete();

        verify(repository).save(entity);
        verify(mapper).map(loan, LoanEntity.class);
        verify(mapper).map(entity, Loan.class);
    }

    @Test
    void countAllLoans_delegatesToRepository() {
        adapter = new LoanReactiveRepositoryAdapter(repository, mapper);
        when(repository.count()).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.countAllLoans())
                .expectNext(5L)
                .verifyComplete();

        verify(repository, times(1)).count();
    }

    @Test
    void findAllLoans_delegatesToRepository() {
        adapter = new LoanReactiveRepositoryAdapter(repository, mapper);

        LoanJoinedProjection projection = new LoanJoinedProjection(
                UUID.randomUUID(), UUID.randomUUID(), null, 12,
                "email@test.com", "12345678", "Pending review", "PERSONAL", null
        );
        when(repository.findAllLoans()).thenReturn(Flux.just(projection));

        StepVerifier.create(adapter.findAllLoans())
                .expectNext(projection)
                .verifyComplete();

        verify(repository, times(1)).findAllLoans();
    }

    @Test
    void countLoansByStatusIds_delegatesToRepository() {
        adapter = new LoanReactiveRepositoryAdapter(repository, mapper);

        List<UUID> statusIds = List.of(UUID.randomUUID());
        when(repository.countByIdStatusIn(statusIds)).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countLoansByStatusIds(statusIds))
                .expectNext(3L)
                .verifyComplete();

        verify(repository, times(1)).countByIdStatusIn(statusIds);
    }

    @Test
    void findLoansWithTypeAndStatus_delegatesToRepository() {
        adapter = new LoanReactiveRepositoryAdapter(repository, mapper);

        UUID[] statusIds = {UUID.randomUUID()};
        LoanJoinedProjection projection = new LoanJoinedProjection(
                UUID.randomUUID(), UUID.randomUUID(), null, 12,
                "email@test.com", "12345678", "Approved", "PERSONAL", null
        );

        when(repository.findLoansWithTypeAndStatus(statusIds, 10, 0))
                .thenReturn(Flux.just(projection));

        StepVerifier.create(adapter.findLoansWithTypeAndStatus(statusIds, 10, 0))
                .expectNext(projection)
                .verifyComplete();

        verify(repository, times(1)).findLoansWithTypeAndStatus(statusIds, 10, 0);
    }
}
