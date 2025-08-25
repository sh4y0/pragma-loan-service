package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.api.exception.model.ErrorResponse;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final UseCaseLogger logger;

    @Override
    @NonNull
    public Mono<ServerResponse> filter(@NonNull ServerRequest request,
                                       @NonNull HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .onErrorResume(BaseException.class, ex -> {
                    logger.trace(
                            "Exception: {} - {} - errors: {}",
                            ex.getErrorCode(),
                            ex.getMessage(),
                            ex.getErrors());
                    return ServerResponse.status(ex.getStatus()).bodyValue(
                            ErrorResponse.builder()
                                    .code(ex.getErrorCode())
                                    .tittle(ex.getTitle())
                                    .message(ex.getMessage())
                                    .errors(ex.getErrors())
                                    .status(ex.getStatus())
                                    .timestamp(ex.getTimestamp())
                                    .build()
                    );
                });
    }
}