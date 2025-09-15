package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;

import com.creditya.loanservice.api.dto.request.LoanUpdateRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.api.exception.service.ValidationService;
import com.creditya.loanservice.api.mapper.LoanMapper;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.CreateLoanUseCase;
import com.creditya.loanservice.usecase.LoanDecisionProcessorUseCase;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.creditya.loanservice.api.util.ParseSafe.parseSafe;

@Component
@RequiredArgsConstructor
public class Handler {
    private final CreateLoanUseCase createLoanUseCase;
    private final GetPaginationLoanUseCase getPaginationLoanUseCase;
    private final LoanDecisionProcessorUseCase loanDecisionProcessorUseCase;
    private final LoanMapper loanMapper;
    private final ValidationService validator;


    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADVISER')")
    public Mono<ServerResponse> createLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanCreatedRequestDTO.class)
                .flatMap(validator::validate)
                .flatMap(loanDto -> createLoanUseCase.createLoan(loanMapper.toLoan(loanDto), loanDto.loanType())
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
        int page = parseSafe(request.queryParam("start"), 0);
        int size = parseSafe(request.queryParam("limit"), 20);

        List<String> filterStatuses = request.queryParams().getOrDefault("status", List.of());

        return getPaginationLoanUseCase.execute(page, size, filterStatuses)
                .flatMap(pageResponse -> {
                    if (pageResponse.getContent().isEmpty()) {
                        Map<String, Object> emptyContent = Map.of("content", Collections.emptyList());
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(emptyContent);
                    } else {
                        Page<LoanResponseDTO> dtoPage = Page.<LoanResponseDTO>builder()
                                .start(pageResponse.getStart())
                                .limit(pageResponse.getLimit())
                                .totalElements(pageResponse.getTotalElements())
                                .totalPages(pageResponse.getTotalPages())
                                .content(pageResponse.getContent().stream()
                                        .map(loanMapper::toLoanCreateResponseDTO)
                                        .toList())
                                .build();
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dtoPage);
                    }
                });

    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADVISER')")
    public Mono<ServerResponse> updateLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanUpdateRequestDTO.class)
                .flatMap(validator::validate)
                .flatMap(loanDto -> loanDecisionProcessorUseCase.processManualUpdate(loanMapper.toLoanDecision(loanDto))
                )
                .map(loanMapper::toLoanUpdateResponseDTO)
                .flatMap(response ->
                        ServerResponse.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                .onErrorResume(ex -> Mono.error(
                        ex instanceof BaseException ? ex : new UnexpectedException(ex)
                ));
    }



    public Mono<Void> createLoanDoc(@RequestBody(description = "Request - for Loan")
                                    LoanCreatedRequestDTO dto) {
        return Mono.empty();
    }
}
