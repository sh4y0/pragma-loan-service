package com.creditya.loanservice.api.exception;

import com.creditya.loanservice.api.exception.model.ErrorResponse;
import com.creditya.loanservice.api.exception.model.UnexpectedException;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.BaseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GlobalExceptionFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final UseCaseLogger logger;
    private static final String INVALID_REQUEST_FORMAT_CODE = "INVALID_REQUEST_FORMAT";
    private static final String INVALID_REQUEST_FORMAT_TITLE = "Invalid Request Format";
    private static final String UNEXPECTED_ERROR_CODE = "UNEXPECTED_ERROR";
    private static final String UNEXPECTED_ERROR_TITLE = "Unexpected Error";
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";


    @Override
    @NonNull
    public Mono<ServerResponse> filter(@NonNull ServerRequest request,
                                       @NonNull HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .onErrorResume(BaseException.class, this::handleBaseException)
                .onErrorResume(Throwable.class, this::handleGenericException);
    }

    private Mono<ServerResponse> handleBaseException(BaseException ex) {
        if (ex instanceof UnexpectedException unexpectedException) {
            return findCause(unexpectedException, InvalidFormatException.class)
                    .map(this::handleInvalidFormat)
                    .orElseGet(() -> {

                        logger.error("Unexpected (Base) Exception: {} - {} - errors: {}",
                                ex.getErrorCode(), ex.getMessage(), ex.getErrors(), ex);

                        return buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getTitle(), ex.getMessage(), ex.getErrors());
                    });
        } else {
            logger.trace("Handled BaseException: {} - {} - errors: {}",
                    ex.getErrorCode(), ex.getMessage(), ex.getErrors());

            return buildErrorResponse(ex.getStatus(), ex.getErrorCode(), ex.getTitle(), ex.getMessage(), ex.getErrors());
        }
    }

    private Mono<ServerResponse> handleInvalidFormat(InvalidFormatException formatException) {
        String fieldName = getFieldName(formatException);
        Object invalidValue = formatException.getValue();

        logger.trace("Invalid format for field '{}': Invalid value '{}', expected type '{}'",
                fieldName, invalidValue, formatException.getTargetType().getSimpleName());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                INVALID_REQUEST_FORMAT_CODE,
                INVALID_REQUEST_FORMAT_TITLE,
                String.format("Invalid value '%s' for field '%s'", invalidValue, fieldName),
                Map.of(
                        "field", fieldName,
                        "invalidValue", String.valueOf(invalidValue),
                        "expectedType", formatException.getTargetType().getSimpleName()
                )
        );
    }

    private Mono<ServerResponse> handleGenericException(Throwable ex) {
        logger.error("An unhandled exception occurred during request processing: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                UNEXPECTED_ERROR_CODE,
                UNEXPECTED_ERROR_TITLE,
                GENERIC_ERROR_MESSAGE,
                null
        );
    }

    private <T extends Throwable> Optional<T> findCause(Throwable throwable, Class<T> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return Optional.of(causeType.cast(current));
            }
            current = current.getCause();
        }
        return Optional.empty();
    }

    private String getFieldName(InvalidFormatException ex) {
        return Optional.ofNullable(ex.getPath())
                .filter(path -> !path.isEmpty())
                .map(path -> path.getLast().getFieldName())
                .orElse("unknown");
    }

    private Mono<ServerResponse> buildErrorResponse(int status, String code, String title, String message, Object errors) {
        return ServerResponse.status(status).bodyValue(
                ErrorResponse.builder()
                        .code(code)
                        .tittle(title)
                        .message(message)
                        .errors(errors)
                        .status(status)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}