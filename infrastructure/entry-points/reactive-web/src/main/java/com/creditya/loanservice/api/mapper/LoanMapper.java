package com.creditya.loanservice.api.mapper;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.request.LoanUpdateRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.api.dto.response.LoanUpdateResponseDTO;
import com.creditya.loanservice.model.loan.data.LoanWithUser;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.loan.data.LoanDecision;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LoanMapper {
    
    public Loan toLoan(LoanCreatedRequestDTO loanDTO) {
        if ( loanDTO == null ) {
            return null;
        }

        Loan.LoanBuilder loan = Loan.builder();

        loan.amount( loanDTO.amount() );
        loan.loanTerm( loanDTO.loanTerm() );
        loan.email( loanDTO.email() );
        loan.dni( loanDTO.dni() );

        return loan.build();
    }

    public LoanDecision toLoanDecision(LoanUpdateRequestDTO loanUpdateRequestDTO) {
        if ( loanUpdateRequestDTO == null ) {
            return null;
        }

        LoanDecision.LoanDecisionBuilder loanDecision = LoanDecision.builder();

        loanDecision.idLoan(loanUpdateRequestDTO.idLoan());
        loanDecision.status( loanUpdateRequestDTO.status());

        return loanDecision.build();

    }

    public LoanCreatedResponseDTO toLoanCreateResponseDTO(LoanData loandData) {
        if ( loandData == null ) {
            return null;
        }

        UUID idLoan;
        BigDecimal amount;
        int loanTerm = 0;
        String email;
        String dni;
        String loanType;

        idLoan = loandData.getIdLoan();
        amount = loandData.getAmount();
        if ( loandData.getLoanTerm() != null ) {
            loanTerm = loandData.getLoanTerm();
        }
        email = loandData.getEmail();
        dni = loandData.getDni();
        loanType = loandData.getLoanType();

        return new LoanCreatedResponseDTO(idLoan, amount, loanTerm, email, dni, loanType );
    }

    public LoanResponseDTO toLoanCreateResponseDTO(LoanWithUser loanWithUser) {
        UserSnapshot user = loanWithUser.getUserSnapshot();

        return LoanResponseDTO.builder()
                .idLoan(loanWithUser.getIdLoan())
                .amount(loanWithUser.getAmount())
                .loanTerm(loanWithUser.getLoanTerm())
                .email(loanWithUser.getEmail())
                .dni(loanWithUser.getDni())
                .loanType(loanWithUser.getLoanTypeName())
                .loanStatus(loanWithUser.getLoanStatusName())
                .interestRate(loanWithUser.getInterestRate())
                .totalMontlyDebt(loanWithUser.getTotalMonthlyDebt())
                .approvedLoans(loanWithUser.getApprovedLoan())
                .name(user.getName())
                .email(user.getEmail())
                .baseSalary(user.getBaseSalary())
                .build();
    }

    public LoanUpdateResponseDTO toLoanUpdateResponseDTO(LoanDecision loanDecision) {
        if ( loanDecision == null ) {
            return null;
        }

        UUID idLoan;
        String status;

        idLoan = loanDecision.getIdLoan();
        status = loanDecision.getStatus();

        return new LoanUpdateResponseDTO(idLoan, status);
    }
}
