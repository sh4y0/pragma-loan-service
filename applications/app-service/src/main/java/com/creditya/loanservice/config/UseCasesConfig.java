package com.creditya.loanservice.config;

import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.LoanUseCase;
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
    LoanUseCase loanUseCase(
            LoanRepository loanApplicationRepository,
            LoanTypeRepository loanTypeRepository,
            LoanStatusRepository statusRepository,
            UseCaseLogger logger,
            TransactionalGateway transactionalGateway

    ) {
        return new LoanUseCase(loanApplicationRepository, loanTypeRepository, statusRepository, logger, transactionalGateway);
    }

}
