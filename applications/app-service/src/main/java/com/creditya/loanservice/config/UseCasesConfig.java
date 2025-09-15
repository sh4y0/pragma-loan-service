package com.creditya.loanservice.config;

import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loan.gateways.LoanSQSSender;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.utils.AutomaticValidation;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import com.creditya.loanservice.usecase.utils.LoanStatus;
import com.creditya.loanservice.usecase.utils.LoanValidator;
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

    @Bean
    public LoanValidator loanValidator(LoanTypeRepository loanTypeRepository, UseCaseLogger logger) {
        return new LoanValidator(loanTypeRepository, logger);
    }

    @Bean
    public AutomaticValidation automaticValidation(LoanRepository loanRepository,
                                                   UserSnapshotRepository userSnapshotRepository,
                                                   LoanSQSSender loanSQSSender,
                                                   UseCaseLogger logger) {
        return new AutomaticValidation(loanRepository, userSnapshotRepository, loanSQSSender, logger);
    }
}
