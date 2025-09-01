package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.api.exception.model.ErrorResponse;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.BaseException;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(-2)
public class GlobalSecurityExceptionHandler implements WebExceptionHandler {

    private final UseCaseLogger logger;

    public GlobalSecurityExceptionHandler(UseCaseLogger logger) {
        this.logger = logger;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            logger.trace("Handled Security BaseException: {} - {} - errors: {}",
                    baseEx.getErrorCode(), baseEx.getMessage(), baseEx.getErrors());

            ErrorResponse response = ErrorResponse.builder()
                    .code(baseEx.getErrorCode())
                    .tittle(baseEx.getTitle())
                    .message(baseEx.getMessage())
                    .errors(baseEx.getErrors())
                    .status(baseEx.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ServerResponse.status(baseEx.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response)
                    .flatMap(resp -> resp.writeTo(exchange, new ServerResponse.Context() {
                        @Override
                        public List<HttpMessageWriter<?>> messageWriters() {
                            return HandlerStrategies.withDefaults().messageWriters();
                        }

                        @Override
                        public List<ViewResolver> viewResolvers() {
                            return HandlerStrategies.withDefaults().viewResolvers();
                        }
                    }));
        }

        return Mono.error(ex);
    }
}
