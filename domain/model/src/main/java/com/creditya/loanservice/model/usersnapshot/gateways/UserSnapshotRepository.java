package com.creditya.loanservice.model.usersnapshot.gateways;

import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserSnapshotRepository {

    Flux<UserSnapshot> findUsersByIds(List<UUID> userIds);
    Mono<UserSnapshot> findUserById(UUID userId);
}
