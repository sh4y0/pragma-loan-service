package com.creditya.loanservice.consumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

class RestConsumerTest {

    private static RestConsumer restConsumer;
    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockBackEnd.url("/").toString())
                .build();
        restConsumer = new RestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Validate findUsersByIds returns UserSnapshot flux")
    void findUsersByIds_shouldReturnUserSnapshots() {
        String userId = UUID.randomUUID().toString();
        String jsonBody = "[{\"userId\":\"" + userId + "\",\"dni\":\"12345678A\",\"email\":\"test@test.com\"}]";

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(jsonBody));

        List<UUID> ids = List.of(UUID.fromString(userId));
        var result = restConsumer.findUsersByIds(ids);

        StepVerifier.create(result)
                .expectNextMatches(user ->
                        user.getUserId().toString().equals(userId) &&
                                user.getEmail().equals("test@test.com"))
                .verifyComplete();
    }
}
