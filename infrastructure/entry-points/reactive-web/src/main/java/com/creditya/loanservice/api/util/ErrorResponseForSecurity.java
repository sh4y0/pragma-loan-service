package com.creditya.loanservice.api.util;

import com.creditya.loanservice.api.exception.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseForSecurity {

    private ErrorResponseForSecurity() {}

    public static Mono<Void> write(ServerWebExchange exchange,
                                   HttpStatus status,
                                   String code,
                                   String title,
                                   String message,
                                   Object errors) {

        ErrorResponse response = ErrorResponse.builder()
                .code(code)
                .tittle(title)
                .message(message)
                .errors(errors)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ServerResponse.status(status)
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
}
