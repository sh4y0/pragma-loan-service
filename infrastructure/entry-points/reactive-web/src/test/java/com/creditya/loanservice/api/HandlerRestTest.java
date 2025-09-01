package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.facade.SecureLoanFacade;
import com.creditya.loanservice.model.loan.Loan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerRestTest {

    @Mock private LoanMapper mapper;
    @Mock private ValidationService validator;
    @Mock private SecureLoanFacade loanUseCase;
    @Mock private ServerRequest serverRequest;

    private Handler handler;

    private LoanCreatedRequestDTO loanDTO;
    private Loan domain;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        loanDTO = new LoanCreatedRequestDTO(
                new BigDecimal("10000.00"),
                12,
                "test@example.com",
                "12345678",
                "Préstamo Personal"
        );

        domain = Loan.builder()
                .loanId(UUID.randomUUID())
                .amount(loanDTO.amount())
                .loanTerm(loanDTO.loanTerm())
                .email(loanDTO.email())
                .dni(loanDTO.dni())
                .build();



        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.empty());
        when(validator.validate(any(LoanCreatedRequestDTO.class))).thenReturn(Mono.just(loanDTO));
        when(mapper.toLoan(loanDTO)).thenReturn(domain);

        handler = new Handler(loanUseCase, mapper, validator);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("Should handle POST /loan and return ServerResponse with created LoanApplication")
    void testListenPOSTCreateLoanApplicationUseCase() {
        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.just(loanDTO));

        Mono<ServerResponse> result = handler.createLoan(serverRequest);

        StepVerifier.create(result)
                .expectNextMatches(ServerResponse.class::isInstance)
                .verifyComplete();

        verify(loanUseCase, times(1)).createLoan(domain, "PERSONAL");
    }

    @Test
    @DisplayName("Should return empty Mono when createLoanApplicationDoc is invoked")
    void testCreateLoanApplicationDoc(){
        StepVerifier.create(handler.createLoanDoc(loanDTO))
                .expectNextCount(0)
                .verifyComplete();
    }
}
