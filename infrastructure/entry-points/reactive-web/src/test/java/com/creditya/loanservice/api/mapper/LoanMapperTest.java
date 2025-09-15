package com.creditya.loanservice.api.mapper;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.model.loan.data.LoanWithUser;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoanMapperTest {

    private LoanMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LoanMapper();
    }

    @Test
    void toLoan_shouldMapRequestDTOToDomain() {
        LoanCreatedRequestDTO dto = new LoanCreatedRequestDTO(
                new BigDecimal("10000.00"),
                12,
                "test@example.com",
                "12345678",
                "PERSONAL"
        );

        Mono<Loan> result = Mono.fromSupplier(() -> mapper.toLoan(dto));

        StepVerifier.create(result)
                .assertNext(loan -> {
                    assertThat(loan.getAmount()).isEqualByComparingTo("10000.00");
                    assertThat(loan.getLoanTerm()).isEqualTo(12);
                    assertThat(loan.getEmail()).isEqualTo("test@example.com");
                    assertThat(loan.getDni()).isEqualTo("12345678");
                })
                .verifyComplete();
    }

    @Test
    void toLoanCreateResponseDTO_shouldMapLoanDataToResponse() {
        LoanData loanData = new LoanData();
        loanData.setAmount(new BigDecimal("5000.00"));
        loanData.setLoanTerm(24);
        loanData.setEmail("user@mail.com");
        loanData.setDni("87654321");
        loanData.setLoanType("MORTGAGE");

        Mono<LoanCreatedResponseDTO> result = Mono.fromSupplier(() -> mapper.toLoanCreateResponseDTO(loanData));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.amount()).isEqualByComparingTo("5000.00");
                    assertThat(response.loanTerm()).isEqualTo(24);
                    assertThat(response.email()).isEqualTo("user@mail.com");
                    assertThat(response.dni()).isEqualTo("87654321");
                    assertThat(response.loanType()).isEqualTo("MORTGAGE");
                })
                .verifyComplete();
    }

    @Test
    void toLoanCreateResponseDTO_shouldMapLoanWithUserToResponse() {
        UserSnapshot user = new UserSnapshot(UUID.randomUUID(),"John Doe", "loan@mail.com", new BigDecimal("1500.00"));

        LoanWithUser loanWithUser = LoanWithUser.builder()
                .loanId(UUID.randomUUID())
                .amount(new BigDecimal("7500.00"))
                .loanTerm(36)
                .email("loan@mail.com")
                .dni("11223344")
                .userSnapshot(user)
                .loanTypeName("CAR")
                .loanStatusName("APPROVED")
                .interestRate(new BigDecimal("5.5"))
                .totalMontlyDebt(new BigDecimal("200.00"))
                .approvedLoan(3L)
                .build();

        Mono<LoanResponseDTO> result = Mono.fromSupplier(() -> mapper.toLoanCreateResponseDTO(loanWithUser));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.amount()).isEqualByComparingTo("7500.00");
                    assertThat(response.loanTerm()).isEqualTo(36);
                    assertThat(response.email()).isEqualTo("loan@mail.com");
                    assertThat(response.name()).isEqualTo("John Doe");
                    assertThat(response.baseSalary()).isEqualByComparingTo("1500.00");
                    assertThat(response.loanType()).isEqualTo("CAR");
                    assertThat(response.loanStatus()).isEqualTo("APPROVED");
                    assertThat(response.interestRate()).isEqualByComparingTo("5.5");
                    assertThat(response.totalMontlyDebt()).isEqualByComparingTo("200.00");
                    assertThat(response.approvedLoans()).isEqualTo(3L);
                })
                .verifyComplete();
    }

}
