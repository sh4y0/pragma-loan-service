package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.api.util.ErrorResponseForSecurity;
import com.creditya.loanservice.usecase.utils.ErrorCatalog;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

    private static final ErrorCatalog error = ErrorCatalog.FORBIDDEN;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    String roles = auth.getAuthorities().toString();
                    return ErrorResponseForSecurity.write(
                            exchange,
                            HttpStatus.FORBIDDEN,
                            error.getCode(),
                            error.getTitle(),
                            error.getMessage(),
                            Map.of("roles", roles)
                    );
                });
    }
}