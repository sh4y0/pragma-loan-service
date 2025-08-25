package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.usecase.LoanUseCase;
import com.creditya.loanservice.usecase.exception.BaseException;
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
                .flatMap(validator::validate)
                .flatMap(dto -> {
                    var domain = loanMapper.toDomain(dto);
                    return loanUseCase.createLoan(domain, dto.loanType());
                })
                .flatMap(response
                        -> ServerResponse.created(URI.create("/api/v1/loan"))
                        .bodyValue(response))
                .onErrorResume(ex -> Mono.error(
                        ex instanceof BaseException ? ex : new UnexpectedException(ex)
                ));

    }
}
