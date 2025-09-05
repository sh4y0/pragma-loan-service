package com.creditya.loanservice.api.config;

import com.creditya.loanservice.api.exception.model.UnauthorizedException;
import lombok.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
public class UserContextFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String userId = authToken.getToken().getClaimAsString("userId");
                    String userDni = authToken.getToken().getClaimAsString("userDni");
                    if (userId == null) {
                        return Mono.error(new UnauthorizedException("Missing userId claim"));
                    }

                    return chain.filter(exchange)
                            .contextWrite(Context.of("userId", userId, "userDni", userDni));
                });
    }
}
