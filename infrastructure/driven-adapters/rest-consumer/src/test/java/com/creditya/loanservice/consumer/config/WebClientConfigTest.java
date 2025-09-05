package com.creditya.loanservice.consumer.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class WebClienteConfigTest {

    private final WebClienteConfig config = new WebClienteConfig();

    @Test
    void authWebClient_shouldBeConfiguredCorrectly_reactive() {
        WebClient webClient = config.authWebClient();

        ExchangeFunction exchangeMock = request -> {
            assertThat(request.url().toString()).startsWith("http://localhost:8090");
            assertThat(request.headers().getFirst(HttpHeaders.CONTENT_TYPE))
                    .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient testClient = webClient.mutate().exchangeFunction(exchangeMock).build();

        StepVerifier.create(testClient.get().uri("/test").retrieve().bodyToMono(String.class))
                .verifyComplete();
    }

}
