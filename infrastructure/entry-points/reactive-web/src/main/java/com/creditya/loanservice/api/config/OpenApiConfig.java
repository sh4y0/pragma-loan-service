package com.creditya.loanservice.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Loan-Service",
        version = "1.0",
        description = "Service of Loan for Crediya"
))
public class OpenApiConfig {
}
