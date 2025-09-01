package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.usecase.exception.BaseException;
import com.creditya.loanservice.usecase.utils.ErrorCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

    private static final ErrorCatalog error = ErrorCatalog.UNAUTHORIZED;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    String roles = auth.getAuthorities().toString();

                    BaseException ex = new BaseException(
                            error.getCode(),
                            error.getTitle(),
                            error.getMessage(),
                            error.getStatus(),
                            Map.of("roles", roles)
                    );

                    return Mono.<Void>error(ex);
                })
                .switchIfEmpty(Mono.error(new BaseException(
                        error.getCode(),
                        error.getTitle(),
                        error.getMessage(),
                        error.getStatus(),
                        Map.of("reason", "Access denied without principal")
                )));
    }
}


