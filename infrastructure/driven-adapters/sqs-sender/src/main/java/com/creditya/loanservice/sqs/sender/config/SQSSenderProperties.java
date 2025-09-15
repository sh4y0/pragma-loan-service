package com.creditya.loanservice.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     Queues queues,
     String endpoint){

    public record Queues(
            String debtCapacity,
            String statusNotification,
            String loanApproved
    ) {}
}
