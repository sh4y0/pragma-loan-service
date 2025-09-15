package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.exception.GlobalExceptionFilter;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.CreateLoanUseCase;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, GlobalExceptionFilter.class})
@WebFluxTest
class RouterRestTest {

    @MockitoBean
    private CreateLoanUseCase createLoanUseCase;

    @MockitoBean
    private LoanMapper loanMapper;

    @MockitoBean
    private ValidationService validator;

    @MockitoBean
    private GetPaginationLoanUseCase getPaginationLoanUseCase;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UseCaseLogger useCaseLogger;

    private LoanCreatedRequestDTO loanDTO;

    @BeforeEach
    void setUp() {
        Handler handler = new Handler(createLoanUseCase, loanMapper, validator, getPaginationLoanUseCase);

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

        Loan domain = Loan.builder()
                .loanId(UUID.randomUUID())
                .amount(loanDTO.amount())
                .loanTerm(loanDTO.loanTerm())
                .email(loanDTO.email())
                .dni(loanDTO.dni())
                .build();

        LoanData domainLoanData = LoanData.builder()
                .loanId(UUID.randomUUID())
                .amount(loanDTO.amount())
                .loanTerm(loanDTO.loanTerm())
                .email(loanDTO.email())
                .dni(loanDTO.dni())
                .build();

        LoanCreatedResponseDTO responseDTO = new LoanCreatedResponseDTO(
                domainLoanData.getAmount(),
                domainLoanData.getLoanTerm(),
                domainLoanData.getEmail(),
                domainLoanData.getDni(),
                loanDTO.loanType()
        );

        when(validator.validate(any(LoanCreatedRequestDTO.class)))
                .thenReturn(Mono.just(loanDTO));
        when(loanMapper.toLoan(any(LoanCreatedRequestDTO.class)))
                .thenReturn(domain);
        when(createLoanUseCase.createLoan(any(), any()))
                .thenReturn(Mono.just(domainLoanData));
        when(loanMapper.toLoanCreateResponseDTO(domainLoanData))
                .thenReturn(responseDTO);

        webTestClient.post()
                .uri("/api/v1/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loanDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LoanCreatedResponseDTO.class)
                .isEqualTo(responseDTO);
    }

}
