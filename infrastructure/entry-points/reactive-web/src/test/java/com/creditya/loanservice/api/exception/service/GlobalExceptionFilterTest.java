package com.creditya.loanservice.api.exception.service;

import com.creditya.loanservice.api.exception.GlobalExceptionFilter;
import com.creditya.loanservice.api.exception.model.ErrorResponse;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionFilterTest {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        UseCaseLogger mockLogger = Mockito.mock(UseCaseLogger.class);
        GlobalExceptionFilter filter = new GlobalExceptionFilter(mockLogger);

        RouterFunction<ServerResponse> router = RouterFunctions
                .route()
                .GET("/base-exception", req -> Mono.error(new UnexpectedException(null)))
                .GET("/invalid-format", req -> {
                    InvalidFormatException cause =
                            new InvalidFormatException(null, "bad value", "oops", String.class);
                    return Mono.error(new UnexpectedException(cause));
                })
                .GET("/generic-error", req -> Mono.error(new IOException("IO failed")))
                .GET("/success", req -> ServerResponse.ok().bodyValue("ok"))
                .filter(filter)
                .build();

        client = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    void shouldHandleBaseUnexpectedException() {
        client.get().uri("/base-exception")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody(ErrorResponse.class)
                .consumeWith(result -> {
                    ErrorResponse body = result.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
                    assertThat(body.getTittle()).isEqualTo("Internal Server Error");
                    assertThat(body.getStatus()).isEqualTo(500);
                });
    }

    @Test
    void shouldHandleInvalidFormatInsideUnexpectedException() {
        client.get().uri("/invalid-format")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .consumeWith(result -> {
                    ErrorResponse body = result.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getCode()).isEqualTo("INVALID_REQUEST_FORMAT");
                    assertThat(body.getMessage()).contains("Invalid value");
                    assertThat(body.getErrors()).isInstanceOfAny(java.util.Map.class);
                });
    }

    @Test
    void shouldHandleGenericException() {
        client.get().uri("/generic-error")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody(ErrorResponse.class)
                .consumeWith(result -> {
                    ErrorResponse body = result.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getCode()).isEqualTo("UNEXPECTED_ERROR");
                    assertThat(body.getTittle()).isEqualTo("Unexpected Error");
                });
    }

    @Test
    void shouldPassThroughOnSuccess() {
        client.get().uri("/success")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("ok");
    }
}
