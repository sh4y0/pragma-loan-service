package com.creditya.loanservice.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class TestSecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(org.springframework.security.config.web.server.ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}