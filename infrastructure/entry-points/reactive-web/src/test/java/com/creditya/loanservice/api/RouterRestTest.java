package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.exception.GlobalExceptionFilter;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.facade.SecureLoanFacade;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, GlobalExceptionFilter.class})
@WebFluxTest
class RouterRestTest {

    @MockitoBean
    private SecureLoanFacade loanUseCase;

    @MockitoBean
    private LoanMapper loanMapper;

    @MockitoBean
    private ValidationService validator;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UseCaseLogger useCaseLogger;

    private LoanCreatedRequestDTO loanDTO;

    @BeforeEach
    void setUp() {
        Handler handler = new Handler(loanUseCase, loanMapper, validator);

        webTestClient = WebTestClient.bindToRouterFunction(
                RouterFunctions.route()
                        .POST("/api/v1/loan", handler::createLoan)
                        .build()
        ).build();

        loanDTO = LoanCreatedRequestDTO.builder()
                .amount(new BigDecimal("2500.00"))
                .loanTerm(12)
                .email("test@email.com")
                .dni("12345678")
                .loanType("PERSONAL")
                .build();
    }

    @Test
    void givenValidLoanRequest_whenCreateLoan_thenReturnsCreated() {

        when(validator.validate(any(LoanCreatedRequestDTO.class)))
                .thenReturn(Mono.just(loanDTO));
        when(loanMapper.toLoan(any(LoanCreatedRequestDTO.class)))
                .thenReturn(new com.creditya.loanservice.model.loan.Loan());
        LoanData savedLoan =
                new LoanData();
        savedLoan.setLoanId(UUID.randomUUID());
        when(loanUseCase.createLoan(any(), any()))
                .thenReturn(Mono.just(savedLoan));

        webTestClient.post()
                .uri("/api/v1/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loanDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();
    }

    @Test
    void givenValidationFails_whenCreateLoan_thenReturnsBadRequest() {
        BaseException validationException = new BaseException(
                "VAL-001",
                "Validation error",
                "Invalid loan request",
                400,
                Map.of("amount", "must be greater than 0")
        );

        when(validator.validate(any(LoanCreatedRequestDTO.class)))
                .thenReturn(Mono.error(validationException));

        webTestClient.post()
                .uri("/api/v1/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loanDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid loan request");
    }


    @Test
    void givenUnexpectedError_whenCreateLoan_thenReturnsInternalServerError() {
        when(validator.validate(any(LoanCreatedRequestDTO.class)))
                .thenReturn(Mono.just(loanDTO));
        when(loanMapper.toLoan(any(LoanCreatedRequestDTO.class)))
                .thenReturn(new com.creditya.loanservice.model.loan.Loan());
        when(loanUseCase.createLoan(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("DB down")));

        webTestClient.post()
                .uri("/api/v1/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loanDTO)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Unexpected error: DB down");
    }
}
