package com.creditya.loanservice.consumer;

import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserSnapshotRepository {
    private final WebClient webClient;

    @Override
    public Flux<UserSnapshot> findUsersByIds(List<UUID> userIds) {
        return webClient.post()
                .uri("/api/v1/clients/by-ids")
                .bodyValue(userIds)
                .retrieve()
                .bodyToFlux(UserSnapshot.class);
    }

    @Override
    public Mono<UserSnapshot> findUserById(UUID userId) {
        return webClient.get()
                .uri("/api/v1/clients/{userId}", userId)
                .retrieve()
                .bodyToMono(UserSnapshot.class);
    }
}
