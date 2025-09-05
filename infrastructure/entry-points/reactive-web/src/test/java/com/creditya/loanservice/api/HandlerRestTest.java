package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.LoanWithUser;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.LoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class HandlerRestTest {

    @Mock private LoanUseCase loanUseCase;
    @Mock private LoanMapper loanMapper;
    @Mock private ValidationService validator;
    @Mock private GetPaginationLoanUseCase getPaginationLoanUseCase;
    @Mock private ServerRequest serverRequest;

    private Handler handler;

    private LoanCreatedRequestDTO loanDTO;
    private Loan domain;
    private LoanData domainData;
    private LoanWithUser loanWithUser;
    private LoanResponseDTO responseDTO;
    private LoanCreatedResponseDTO responseCreateLoanDTO;

    @BeforeEach
    void setUp() {
        handler = new Handler(loanUseCase, loanMapper, validator, getPaginationLoanUseCase);

        loanDTO = new LoanCreatedRequestDTO(
                new BigDecimal("10000.00"),
                12,
                "test@example.com",
                "12345678",
                "PERSONAL"
        );

        domain = Loan.builder()
                .loanId(UUID.randomUUID())
                .amount(loanDTO.amount())
                .loanTerm(loanDTO.loanTerm())
                .email(loanDTO.email())
                .dni(loanDTO.dni())
                .build();

        domainData = LoanData.builder()
                .loanId(domain.getLoanId())
                .amount(domain.getAmount())
                .loanTerm(domain.getLoanTerm())
                .email(domain.getEmail())
                .dni(domain.getDni())
                .build();


         responseCreateLoanDTO= new LoanCreatedResponseDTO(
                domain.getAmount(),
                domain.getLoanTerm(),
                domain.getEmail(),
                domain.getDni(),
                loanDTO.loanType()
        );

        loanWithUser = LoanWithUser.builder()
                .loan(domain)
                .loanTypeName("PERSONAL")
                .loanStatusName("PENDING")
                .interestRate(BigDecimal.valueOf(0.05))
                .totalMontlyDebt(BigDecimal.valueOf(500))
                .approvedLoan(1L)
                .build();

        responseDTO = LoanResponseDTO.builder()
                .name("Test User")
                .email(domain.getEmail())
                .amount(domain.getAmount())
                .loanTerm(domain.getLoanTerm())
                .baseSalary(BigDecimal.valueOf(2000))
                .loanType(loanWithUser.getLoanTypeName())
                .loanStatus(loanWithUser.getLoanStatusName())
                .interestRate(loanWithUser.getInterestRate())
                .totalMontlyDebt(loanWithUser.getTotalMontlyDebt())
                .approvedLoans(loanWithUser.getApprovedLoan())
                .build();
    }

    @Test
    @DisplayName("createLoan should return ServerResponse with created Loan")
    void createLoan_shouldReturnServerResponse() {
        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.just(loanDTO));
        when(validator.validate(loanDTO)).thenReturn(Mono.just(loanDTO));
        when(loanMapper.toLoan(loanDTO)).thenReturn(domain);
        when(loanUseCase.createLoan(domain, "PERSONAL")).thenReturn(Mono.just(domainData));
        when(loanMapper.toLoanCreateResponseDTO(domainData)).thenReturn(responseCreateLoanDTO);

        Mono<ServerResponse> result = handler.createLoan(serverRequest);

        StepVerifier.create(result)
                .expectNextMatches(res -> res.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(loanUseCase, times(1)).createLoan(domain, "PERSONAL");
        verify(loanMapper, times(1)).toLoanCreateResponseDTO(domainData);
    }

    @Test
    @DisplayName("getLoans should return PageResponse<LoanResponseDTO>")
    void getLoans_shouldReturnPageResponse() {
        Page<LoanWithUser> pageResponse = Page.<LoanWithUser>builder()
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .content(List.of(loanWithUser))
                .build();

        when(serverRequest.queryParam("page")).thenReturn(java.util.Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(java.util.Optional.of("10"));
        when(serverRequest.queryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());
        when(getPaginationLoanUseCase.execute(0, 10, List.of())).thenReturn(Mono.just(pageResponse));
        when(loanMapper.toLoanCreateResponseDTO(loanWithUser)).thenReturn(responseDTO);

        Mono<ServerResponse> result = handler.getLoans(serverRequest);

        StepVerifier.create(result)
                .expectNextMatches(res -> res.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(getPaginationLoanUseCase, times(1)).execute(0, 10, List.of());
        verify(loanMapper, times(1)).toLoanCreateResponseDTO(loanWithUser);
    }

    @Test
    @DisplayName("createLoanDoc should return empty Mono")
    void createLoanDoc_shouldReturnEmptyMono() {
        StepVerifier.create(handler.createLoanDoc(loanDTO))
                .expectNextCount(0)
                .verifyComplete();
    }
}
