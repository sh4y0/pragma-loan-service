package com.creditya.loanservice.sqs.listener;

import com.creditya.loanservice.model.loan.data.LoanDecision;
import com.creditya.loanservice.model.loan.responseEvent.LoanStatusUpdateEvent;
import com.creditya.loanservice.usecase.LoanDecisionProcessorUseCase;
import com.creditya.loanservice.usecase.UpdateLoanUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoanRequestListener {

    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/873522994278/loan-capacidad-endeudamiento-response-sqs";
    private final SqsAsyncClient sqsAsyncClient;
    private final LoanDecisionProcessorUseCase loanDecisionProcessorUseCase;
    private final ObjectMapper objectMapper;


    @PostConstruct
    public void startListening() {
        listen();
    }

    private void listen() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .waitTimeSeconds(20)
                .maxNumberOfMessages(10)
                .build();

        sqsAsyncClient.receiveMessage(request)
                .thenAccept(response -> {
                    List<Message> messages = response.messages();
                    for (Message msg : messages) {
                        processMessage(msg)
                                .doOnSuccess(v -> sqsAsyncClient.deleteMessage(builder ->
                                        builder.queueUrl(QUEUE_URL)
                                                .receiptHandle(msg.receiptHandle())))
                                .doOnError(e -> log.error("Error processing message: {}", msg.messageId(), e))
                                .subscribe();
                    }
                    Mono.delay(Duration.ofSeconds(1))
                            .doOnTerminate(this::listen)
                            .subscribe();
                })
                .exceptionally(e -> {
                    log.error("Error receiving messages", e);
                    Mono.delay(Duration.ofSeconds(5))
                            .doOnTerminate(this::listen)
                            .subscribe();
                    return null;
                });
    }


    private Mono<Void> processMessage(Message message) {
        return Mono.fromCallable(() -> {
                    log.trace("Processing message body: {}", message.body());
                    JsonNode rootNode = objectMapper.readTree(message.body());
                    boolean automaticValidation = rootNode.get("automaticValidation").asBoolean();

                    if (automaticValidation) {
                        LoanStatusUpdateEvent analysisResult = objectMapper.treeToValue(rootNode, LoanStatusUpdateEvent.class);
                        return loanDecisionProcessorUseCase.processAutomaticAnalysisResult(analysisResult);
                    } else {
                        LoanDecision manualUpdate = objectMapper.treeToValue(rootNode, LoanDecision.class);
                        return loanDecisionProcessorUseCase.processManualUpdate(manualUpdate);
                    }
                }).flatMap(mono -> mono)
                .doOnError(e -> log.error("Failed to process message {}: {}", message.messageId(), e.getMessage()))
                .then();
    }

}
