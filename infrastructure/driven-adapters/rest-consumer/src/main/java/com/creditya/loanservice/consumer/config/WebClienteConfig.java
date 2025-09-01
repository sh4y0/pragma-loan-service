package com.creditya.loanservice.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClienteConfig {

    @Bean
    public WebClient authWebClient() {
        String authServiceUrl = "http://localhost:8090/api/v1/clients";
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
