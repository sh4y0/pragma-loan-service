package com.creditya.loanservice.sqs.sender;

import com.creditya.loanservice.model.creditanalisys.CreditAnalysis;
import com.creditya.loanservice.model.loan.data.LoanApprovedEvent;
import com.creditya.loanservice.model.loan.data.LoanNotification;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.loan.responseEvent.LoanStatusUpdateEvent;
import com.creditya.loanservice.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements LoanSQSSender {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendStatusNotificationCredit(LoanStatusUpdateEvent creditAnalysis) {
        return sendMessage(
                creditAnalysis,
                properties.queues().statusNotification(),
                "StatusNotificationCreditAnalysis"
        );
    }

    @Override
    public Mono<String> sendStatusNotification(LoanNotification loanNotification) {
        return sendMessage(
                loanNotification,
                properties.queues().statusNotification(),
                "StatusNotification"
        );
    }

    @Override
    public Mono<String> sendCreditAnalysis(CreditAnalysis creditAnalysis) {
        return sendMessage(
                creditAnalysis,
                properties.queues().debtCapacity(),
                "CreditAnalysis"
        );
    }

    @Override
    public Mono<String> sendLoanApprovedEvent(LoanApprovedEvent approvedLoan) {
        return sendMessage(
                approvedLoan,
                properties.queues().loanApproved(),
                "LoanApprovedEvent"
        );
    }

    private <T> Mono<String> sendMessage(T payload, String queueUrl, String messageContext) {
        return Mono.fromCallable(() -> {
                    try {
                        String body = objectMapper.writeValueAsString(payload);
                        return SendMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .messageBody(body)
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize payload for " + messageContext, e);
                    }
                })
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent for context '{}': {}", messageContext, response.messageId()))
                .map(SendMessageResponse::messageId)
                .onErrorResume(e -> {
                    log.error("Failed to send SQS message for context '{}'", messageContext, e);
                    return Mono.error(e);
                });
    }
}