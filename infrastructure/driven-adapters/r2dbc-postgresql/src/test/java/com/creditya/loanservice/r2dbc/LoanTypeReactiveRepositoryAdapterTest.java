package com.creditya.loanservice.r2dbc;

import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.r2dbc.entity.LoanTypeEntity;
import com.creditya.loanservice.r2dbc.loan_type_adapter.LoanTypeReactiveRepository;
import com.creditya.loanservice.r2dbc.loan_type_adapter.LoanTypeReactiveRepositoryAdapter;
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
class LoanTypeReactiveRepositoryAdapterTest {

    @InjectMocks
    LoanTypeReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    LoanTypeReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private LoanType loanType;
    private LoanTypeEntity loanTypeEntity;
    private UUID loanTypeId;

    @BeforeEach
    void setUp() {
        loanTypeId = UUID.randomUUID();

        loanType = new LoanType();
        loanType.setIdLoanType(loanTypeId);
        loanType.setName("PERSONAL");
        loanType.setInterestRate(BigDecimal.valueOf(5.0));

        loanTypeEntity = new LoanTypeEntity();
        loanTypeEntity.setIdLoanType(loanTypeId);
        loanTypeEntity.setName("PERSONAL");
        loanTypeEntity.setInterestRate(BigDecimal.valueOf(5.0));
    }

    @Test
    void findByName_success() {
        when(repository.findByName("PERSONAL")).thenReturn(Mono.just(loanTypeEntity));
        when(mapper.map(loanTypeEntity, LoanType.class)).thenReturn(loanType);

        Mono<LoanType> result = repositoryAdapter.findByName("PERSONAL");

        StepVerifier.create(result)
                .expectNextMatches(l -> l.getIdLoanType().equals(loanTypeId) && l.getName().equals("PERSONAL"))
                .verifyComplete();

        verify(repository, times(1)).findByName("PERSONAL");
    }

    @Test
    void findByName_notFound() {
        when(repository.findByName("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByName("UNKNOWN"))
                .verifyComplete();

        verify(repository, times(1)).findByName("UNKNOWN");
    }

}
