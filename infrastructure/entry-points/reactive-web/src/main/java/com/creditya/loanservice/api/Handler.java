package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;

import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.facade.SecureLoanFacade;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.PageResponse;
import com.creditya.loanservice.usecase.GetLoanUnderReviewUseCase;
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

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Handler {
    private final SecureLoanFacade secureLoanFacade;
    private final LoanMapper loanMapper;
    private final ValidationService validator;
    private final GetLoanUnderReviewUseCase getLoanUnderReviewUseCase;

    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public Mono<ServerResponse> createLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanCreatedRequestDTO.class)
                .flatMap(validator::validate)
                .flatMap(loanDto ->
                        Mono.deferContextual(ctx -> {
                            String userId = ctx.get("userId");
                            var loanDomain = loanMapper.toLoan(loanDto);
                            loanDomain.setUserId(UUID.fromString(userId));

                            return secureLoanFacade.createLoan(loanDomain, loanDto.loanType());
                        })
                )
                .map(loanMapper::toLoanResponseDTO)
                .flatMap(response ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                .onErrorResume(ex -> Mono.error(
                        ex instanceof BaseException ? ex : new UnexpectedException(ex)
                ));
    }

    @PreAuthorize("hasAuthority('ROLE_ADVISER')")
    public Mono<ServerResponse> getLoans(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        return getLoanUnderReviewUseCase.execute(page, size)
                .map(pageResponse -> PageResponse.<LoanResponseDTO>builder()
                        .page(pageResponse.getPage())
                        .size(pageResponse.getSize())
                        .totalElements(pageResponse.getTotalElements())
                        .totalPages(pageResponse.getTotalPages())
                        .content(
                                pageResponse.getContent().stream()
                                        .map(loanMapper::toLoanResponseDTO)
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
