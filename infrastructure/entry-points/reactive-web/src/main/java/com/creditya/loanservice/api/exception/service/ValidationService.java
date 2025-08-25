package com.creditya.loanservice.api.exception.service;

import com.creditya.loanservice.api.exception.model.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidationService {
    private final Validator validator;

    public <T> Mono<T> validate(T obj) {
        return Mono.fromCallable(() -> validator.validate(obj))
                .flatMap(violations -> violations.isEmpty()
                                ? Mono.just(obj)
                                : Mono.error(new ValidationException(
                                violations.stream()
                                        .collect(Collectors.toMap(
                                                v -> v.getPropertyPath().toString(),
                                                ConstraintViolation::getMessage
                                        ))
                        ))
                );
    }
}