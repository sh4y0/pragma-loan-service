package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;

import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.LoanUseCase;
import com.creditya.loanservice.usecase.exception.BaseException;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Handler {
    private final LoanUseCase loanUseCase;
    private final LoanMapper loanMapper;
    private final ValidationService validator;
    private final GetPaginationLoanUseCase getPaginationLoanUseCase;

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADVISER')")
    public Mono<ServerResponse> createLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanCreatedRequestDTO.class)
                .flatMap(validator::validate)
                .flatMap(loanDto -> loanUseCase.createLoan(loanMapper.toLoan(loanDto), loanDto.loanType())
                )
                .map(loanMapper::toLoanCreateResponseDTO)
                .flatMap(response ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                .onErrorResume(ex -> Mono.error(
                        ex instanceof BaseException ? ex : new UnexpectedException(ex)
                ));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ADVISER')")
    public Mono<ServerResponse> getLoans(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        List<String> filterStatuses = request.queryParams().getOrDefault("status", List.of());

        return getPaginationLoanUseCase.execute(page, size, filterStatuses)
                .map(pageResponse ->
                        Page.<LoanResponseDTO>builder()
                                .page(pageResponse.getPage())
                                .size(pageResponse.getSize())
                                .totalElements(pageResponse.getTotalElements())
                                .totalPages(pageResponse.getTotalPages())
                                .content(
                                        pageResponse.getContent().stream()
                                                .map(loanMapper::toLoanCreateResponseDTO)
                                                .toList()
                                )
                                .build()
                )
                .flatMap(dtoPage ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dtoPage)
                );
    }

    public Mono<Void> createLoanDoc(@RequestBody(description = "Request - for Loan")
                                    LoanCreatedRequestDTO dto) {
        return Mono.empty();
    }
}
