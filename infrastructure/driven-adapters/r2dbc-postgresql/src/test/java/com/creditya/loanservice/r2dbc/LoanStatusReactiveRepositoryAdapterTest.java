package com.creditya.loanservice.r2dbc;

import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.r2dbc.entity.LoanStatusEntity;
import com.creditya.loanservice.r2dbc.loan_status_adapter.LoanStatusReactiveRepository;
import com.creditya.loanservice.r2dbc.loan_status_adapter.LoanStatusReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class LoanStatusReactiveRepositoryAdapterTest {

    @InjectMocks
    LoanStatusReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    LoanStatusReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private UUID statusId;
    private LoanStatus loanStatus;
    private LoanStatusEntity loanStatusEntity;

    @BeforeEach
    void setUp() {
        statusId = UUID.randomUUID();

        loanStatus = new LoanStatus();
        loanStatus.setIdStatus(statusId);
        loanStatus.setName("PENDING");

        loanStatusEntity = new LoanStatusEntity();
        loanStatusEntity.setIdStatus(statusId);
        loanStatusEntity.setName("PENDING");
    }

    @Test
    void findByName_success() {
        when(repository.findByName("PENDING")).thenReturn(Mono.just(loanStatusEntity));
        when(mapper.map(loanStatusEntity, LoanStatus.class)).thenReturn(loanStatus);

        Mono<LoanStatus> result = repositoryAdapter.findByName("PENDING");

        StepVerifier.create(result)
                .expectNextMatches(l -> l.getIdStatus().equals(statusId) && l.getName().equals("PENDING"))
                .verifyComplete();

        verify(repository, times(1)).findByName("PENDING");
    }

    @Test
    void findIdsByNames_success() {
        List<String> names = List.of("PENDING");

        when(repository.findByNameIn(names)).thenReturn(Flux.just(loanStatusEntity));

        Flux<UUID> result = repositoryAdapter.findIdsByNames(names);

        StepVerifier.create(result)
                .expectNextMatches(id -> id.equals(statusId))
                .verifyComplete();

        verify(repository, times(1)).findByNameIn(names);
    }

    @Test
    void findByName_notFound() {
        when(repository.findByName("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByName("UNKNOWN"))
                .verifyComplete();

        verify(repository, times(1)).findByName("UNKNOWN");
    }

    @Test
    void findIdsByNames_emptyList() {
        List<String> names = List.of();

        when(repository.findByNameIn(names)).thenReturn(Flux.empty());

        StepVerifier.create(repositoryAdapter.findIdsByNames(names))
                .verifyComplete();

        verify(repository, times(1)).findByNameIn(names);
    }
}
