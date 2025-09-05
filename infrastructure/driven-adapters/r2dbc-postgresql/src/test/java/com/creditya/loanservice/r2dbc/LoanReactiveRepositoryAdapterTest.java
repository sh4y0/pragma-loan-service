package com.creditya.loanservice.r2dbc;

import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import com.creditya.loanservice.r2dbc.loan_adapter.LoanReactiveRepository;
import com.creditya.loanservice.r2dbc.loan_adapter.LoanReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class LoanReactiveRepositoryAdapterTest {

    @InjectMocks
    LoanReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    LoanReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private Loan loan;
    private LoanEntity loanEntity;
    private UUID statusId;

    @BeforeEach
    void setUp() {
        statusId = UUID.randomUUID();

        loan = Loan.builder()
                .loanId(UUID.randomUUID())
                .amount(new BigDecimal("10000"))
                .loanTerm(12)
                .email("test@example.com")
                .dni("12345678")
                .idStatus(UUID.randomUUID())
                .idLoanType(UUID.randomUUID())
                .build();

        loanEntity = LoanEntity.builder()
                .idLoan(loan.getLoanId())
                .amount(loan.getAmount())
                .loanTerm(loan.getLoanTerm())
                .email(loan.getEmail())
                .dni(loan.getDni())
                .idStatus(loan.getIdStatus())
                .idLoanType(loan.getIdLoanType())
                .build();
    }

    @Test
    void createLoan_success() {
        when(mapper.map(loan, LoanEntity.class)).thenReturn(loanEntity);
        when(repository.save(loanEntity)).thenReturn(Mono.just(loanEntity));
        when(mapper.map(loanEntity, Loan.class)).thenReturn(loan);

        Mono<Loan> result = repositoryAdapter.createLoan(loan);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.getLoanId().equals(loan.getLoanId()) &&
                        l.getAmount().equals(loan.getAmount()))
                .verifyComplete();

        verify(repository, times(1)).save(loanEntity);
    }

    @Test
    void findLoansByStatusIds_success() {
        List<UUID> statusIds = List.of(statusId);

        when(repository.findAllByIdStatusIn(statusIds)).thenReturn(Flux.just(loanEntity));
        when(mapper.map(loanEntity, Loan.class)).thenReturn(loan);

        Flux<Loan> result = repositoryAdapter.findLoansByStatusIds(statusIds);

        StepVerifier.create(result)
                .expectNextMatches(l -> l.getLoanId().equals(loan.getLoanId()))
                .verifyComplete();

        verify(repository, times(1)).findAllByIdStatusIn(statusIds);
    }

    @Test
    void countAllLoans_success() {
        when(repository.count()).thenReturn(Mono.just(5L));

        Mono<Long> result = repositoryAdapter.countAllLoans();

        StepVerifier.create(result)
                .expectNext(5L)
                .verifyComplete();

        verify(repository, times(1)).count();
    }

    @Test
    void findAllLoans_success() {
        when(repository.findAll()).thenReturn(Flux.just(loanEntity));
        when(mapper.map(loanEntity, Loan.class)).thenReturn(loan);

        Flux<Loan> result = repositoryAdapter.findAllLoans();

        StepVerifier.create(result)
                .expectNextMatches(l -> l.getLoanId().equals(loan.getLoanId()))
                .verifyComplete();

        verify(repository, times(1)).findAll();
    }

    @Test
    void countLoansByStatusIds_success() {
        List<UUID> statusIds = List.of(statusId);

        when(repository.countByIdStatusIn(statusIds)).thenReturn(Mono.just(3L));

        Mono<Long> result = repositoryAdapter.countLoansByStatusIds(statusIds);

        StepVerifier.create(result)
                .expectNext(3L)
                .verifyComplete();

        verify(repository, times(1)).countByIdStatusIn(statusIds);
    }
}
