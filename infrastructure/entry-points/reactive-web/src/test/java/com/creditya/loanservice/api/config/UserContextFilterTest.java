package com.creditya.loanservice.api.config;

import com.creditya.loanservice.api.exception.model.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserContextFilterTest {

    private UserContextFilter filter;
    private ServerWebExchange exchange;
    private WebFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new UserContextFilter();
        exchange = mock(ServerWebExchange.class);
        chain = mock(WebFilterChain.class);
    }

    @Test
    void filter_ShouldPass_WhenUserIdExists() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("userId")).thenReturn("12345");
        when(jwt.getClaimAsString("userDni")).thenReturn("12345678");

        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
        when(exchange.getPrincipal()).thenReturn(Mono.just(token));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_ShouldReturnUnauthorizedException_WhenUserIdNull() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("userId")).thenReturn(null);

        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
        when(exchange.getPrincipal()).thenReturn(Mono.just(token));

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException)
                .verify();

        verify(chain, never()).filter(any());
    }

    @Test
    void filter_ShouldReturnEmpty_WhenPrincipalIsEmpty() {
        when(exchange.getPrincipal()).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain, never()).filter(any());
    }

}