package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.usecase.LoanUseCase;
import com.creditya.loanservice.usecase.exception.BaseException;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class Handler {
    private final LoanUseCase loanUseCase;
    private final LoanMapper loanMapper;
    private final ValidationService validator;

    public Mono<ServerResponse> createLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanDTO.class)
                .flatMap(validator::validate) // valida DTO
                .flatMap(loanDto -> loanUseCase.createLoan(loanMapper.toDomain(loanDto), loanDto.loanType()))
                .flatMap(savedLoan ->
                        ServerResponse.created(URI.create("/api/v1/loan/" + savedLoan.getLoanId()))
                                .build()
                )
                .onErrorResume(BaseException.class, ex ->
                        ServerResponse.badRequest().bodyValue(ex.getMessage())
                )
                .onErrorResume(Exception.class, ex ->
                        ServerResponse.status(500).bodyValue("Unexpected error: " + ex.getMessage())
                );
    }

    public Mono<Void> createLoanDoc(@RequestBody(description = "Request - for Loan")
                                 LoanDTO dto) {
        return Mono.empty();
    }
}
