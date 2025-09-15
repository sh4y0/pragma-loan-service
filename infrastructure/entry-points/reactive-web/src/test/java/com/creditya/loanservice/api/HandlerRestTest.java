package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.loan.data.LoanWithUser;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.CreateLoanUseCase;
import com.creditya.loanservice.usecase.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlerRestTest {

    @Mock
    private CreateLoanUseCase createLoanUseCase;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private ValidationService validator;

    @Mock
    private GetPaginationLoanUseCase getPaginationLoanUseCase;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private Handler handler;

    private Loan loan;
    private LoanWithUser loanWithUser;
    private LoanData loanData;
    private LoanCreatedRequestDTO loanRequest;
    private LoanCreatedResponseDTO loanCreatedResponseDTO;

    @BeforeEach
    void setUp() {
        loan = Loan.builder()
                .loanId(UUID.randomUUID())
                .amount(new BigDecimal("1500"))
                .loanTerm(12)
                .email("jhoedoe@test.com")
                .dni("12345678")
                .idStatus(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .build();

        UserSnapshot userSnapshot = UserSnapshot.builder()
                .userId(UUID.randomUUID())
                .name("Jhon")
                .email("jhoedoe@test.com")
                .baseSalary(new BigDecimal("1500"))
                .build();

        loanWithUser = LoanWithUser.builder()
                .loanId(UUID.randomUUID())
                .amount(new BigDecimal("1500"))
                .loanTerm(12)
                .email("jhoedoe@test.com")
                .dni("12345678")
                .userSnapshot(userSnapshot)
                .loanTypeName("PERSONAL")
                .loanStatusName("Pending review")
                .interestRate(new BigDecimal("12"))
                .totalMontlyDebt(new BigDecimal("2000"))
                .approvedLoan(0L)
                .build();

        loanData = LoanData.builder()
                .loanId(UUID.randomUUID())
                .amount(new BigDecimal("1500"))
                .loanTerm(12)
                .email("jhoedoe@test.com")
                .dni("12345678")
                .status("Pending review")
                .loanType("PERSONAL")
                .build();

        loanRequest = LoanCreatedRequestDTO.builder()
                .amount(new BigDecimal("1500"))
                .loanTerm(12)
                .email("jhoedoe@test.com")
                .dni("12345678")
                .loanType("PERSONAL")
                .build();

        loanCreatedResponseDTO = LoanCreatedResponseDTO.builder()
                .amount(new BigDecimal("1500"))
                .loanTerm(12)
                .email("jhoedoe@test.com")
                .dni("12345678")
                .loanType("PERSONAL")
                .build();
    }

    @Test
    void createLoan_success() {
        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.just(loanRequest));
        when(validator.validate(loanRequest)).thenReturn(Mono.just(loanRequest));
        when(loanMapper.toLoan(loanRequest)).thenReturn(loan);
        when(createLoanUseCase.createLoan(loan, loanRequest.loanType())).thenReturn(Mono.just(loanData));
        when(loanMapper.toLoanCreateResponseDTO(loanData)).thenReturn(loanCreatedResponseDTO);

        Mono<ServerResponse> responseMono = handler.createLoan(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response ->
                        response.statusCode().equals(HttpStatus.CREATED) &&
                                Objects.equals(response.headers().getContentType(), MediaType.APPLICATION_JSON))
                .verifyComplete();
    }

    @Test
    void createLoan_validationFails() {
        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.just(loanRequest));

        Map<String, String> errors = Map.of("field1", "must not be null");
        BaseException baseException = new BaseException(
                "ERR_VALIDATION",
                "Validation Error",
                "Validation failed",
                400,
                errors
        );

        when(validator.validate(loanRequest)).thenReturn(Mono.error(baseException));

        Mono<ServerResponse> responseMono = handler.createLoan(serverRequest);

        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof BaseException &&
                        throwable.getMessage().equals("Validation failed") &&
                        ((BaseException) throwable).getErrorCode().equals("ERR_VALIDATION") &&
                        ((BaseException) throwable).getStatus() == 400 &&
                        ((BaseException) throwable).getErrors().equals(errors))
                .verify();
    }
    @Test
    void createLoan_unexpectedException() {
        when(serverRequest.bodyToMono(LoanCreatedRequestDTO.class)).thenReturn(Mono.just(loanRequest));
        when(validator.validate(loanRequest)).thenReturn(Mono.just(loanRequest));
        when(loanMapper.toLoan(loanRequest)).thenReturn(loan);
        when(createLoanUseCase.createLoan(loan, loanRequest.loanType())).thenReturn(Mono.error(new RuntimeException("Boom")));

        Mono<ServerResponse> responseMono = handler.createLoan(serverRequest);

        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof UnexpectedException &&
                        throwable.getCause().getMessage().equals("Boom"))
                .verify();
    }

    @Test
    void getLoans_withContent() {
        when(serverRequest.queryParam("start")).thenReturn(java.util.Optional.of("1"));
        when(serverRequest.queryParam("limit")).thenReturn(java.util.Optional.of("10"));
        when(serverRequest.queryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());

        Page<LoanWithUser> page = Page.<LoanWithUser>builder()
                .start(1)
                .limit(10)
                .totalElements(1)
                .totalPages(1)
                .content(List.of(loanWithUser))
                .build();

        when(getPaginationLoanUseCase.execute(1, 10, Collections.emptyList())).thenReturn(Mono.just(page));
        Mono<ServerResponse> responseMono = handler.getLoans(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful() &&
                        Objects.equals(response.headers().getContentType(), MediaType.APPLICATION_JSON))
                .verifyComplete();
    }

    @Test
    void getLoans_emptyContent() {
        when(serverRequest.queryParam("start")).thenReturn(java.util.Optional.empty());
        when(serverRequest.queryParam("limit")).thenReturn(java.util.Optional.empty());
        when(serverRequest.queryParams()).thenReturn(new org.springframework.util.LinkedMultiValueMap<>());

        Page<LoanWithUser> page = Page.<LoanWithUser>builder()
                .start(0)
                .limit(20)
                .totalElements(0)
                .totalPages(0)
                .content(Collections.emptyList())
                .build();

        when(getPaginationLoanUseCase.execute(0, 20, Collections.emptyList())).thenReturn(Mono.just(page));

        Mono<ServerResponse> responseMono = handler.getLoans(serverRequest);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful() &&
                        Objects.equals(response.headers().getContentType(), MediaType.APPLICATION_JSON))
                .verifyComplete();
    }

    @Test
    void createLoanDoc_returnsEmptyMono() {
        Mono<Void> result = handler.createLoanDoc(loanRequest);
        StepVerifier.create(result)
                .verifyComplete();
    }
}
