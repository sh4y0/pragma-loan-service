package com.creditya.loanservice.config;

import com.creditya.loanservice.usecase.utils.LoanCalculator;
import com.creditya.loanservice.usecase.utils.LoanStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "com.creditya.loanservice.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {
    @Bean
    public LoanStatus loanStatusService() {
        return new LoanStatus();
    }

    @Bean
    public LoanCalculator loanCalculator() {
        return new LoanCalculator(loanStatusService());
    }

}
