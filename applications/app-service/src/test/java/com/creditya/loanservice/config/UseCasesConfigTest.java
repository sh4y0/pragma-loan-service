package com.creditya.loanservice.config;

import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.TransactionalGateway;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.GetPaginationLoanUseCase;
import com.creditya.loanservice.usecase.CreateLoanUseCase;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import com.creditya.loanservice.usecase.utils.LoanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Test
    @DisplayName("Should register LoanUseCase bean in application context")
    void testLoanUseCaseBeanExists() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            CreateLoanUseCase createLoanUseCase = context.getBean(CreateLoanUseCase.class);
            assertNotNull(createLoanUseCase, "LoanUseCase bean should be registered");
        }
    }

    @Test
    @DisplayName("Should register GetLoanUnderReviewUseCase bean in application context")
    void testGetLoanUnderReviewUseCaseBeanExists() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            GetPaginationLoanUseCase useCase = context.getBean(GetPaginationLoanUseCase.class);
            assertNotNull(useCase, "GetLoanUnderReviewUseCase bean should be registered");
        }
    }

    @Test
    @DisplayName("Should register LoanStatus and LoanCalculator utility beans")
    void testUtilityBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            LoanStatus status = context.getBean(LoanStatus.class);
            LoanCalculator calculator = context.getBean(LoanCalculator.class);

            assertNotNull(status, "LoanStatus bean should be registered");
            assertNotNull(calculator, "LoanCalculator bean should be registered");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        @Bean
        public LoanRepository loanRepository() {
            return mock(LoanRepository.class);
        }

        @Bean
        public LoanStatusRepository loanStatusRepository() {
            return mock(LoanStatusRepository.class);
        }

        @Bean
        public UserSnapshotRepository userSnapshotRepository() {
            return mock(UserSnapshotRepository.class);
        }

        @Bean
        public LoanTypeRepository loanTypeRepository() {
            return mock(LoanTypeRepository.class);
        }

        @Bean
        public LoanCalculator loanCalculator() {
            return mock(LoanCalculator.class);
        }

        @Bean
        public UseCaseLogger logger() {
            return mock(UseCaseLogger.class);
        }

        @Bean
        public TransactionalGateway transactionalGateway() {
            return mock(TransactionalGateway.class);
        }



    }
}