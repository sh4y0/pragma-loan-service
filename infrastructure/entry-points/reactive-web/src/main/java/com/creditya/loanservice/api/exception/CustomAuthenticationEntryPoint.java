package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.api.util.ErrorResponseForSecurity;
import com.creditya.loanservice.usecase.utils.ErrorCatalog;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final ErrorCatalog error = ErrorCatalog.UNAUTHORIZED;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return ErrorResponseForSecurity.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                error.getCode(),
                error.getTitle(),
                error.getMessage(),
                error.getErrors()
        );
    }
}