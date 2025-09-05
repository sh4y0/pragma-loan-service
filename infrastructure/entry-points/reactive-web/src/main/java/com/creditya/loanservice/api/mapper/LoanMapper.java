package com.creditya.loanservice.api.mapper;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.dto.response.LoanCreatedResponseDTO;
import com.creditya.loanservice.api.dto.response.LoanResponseDTO;
import com.creditya.loanservice.model.LoanWithUser;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.data.LoanData;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

    public LoanCreatedResponseDTO toLoanCreateResponseDTO(LoanData loandData) {
        if ( loandData == null ) {
            return null;
        }

        BigDecimal amount;
        int loanTerm = 0;
        String email;
        String dni;
        String loanType;

        amount = loandData.getAmount();
        if ( loandData.getLoanTerm() != null ) {
            loanTerm = loandData.getLoanTerm();
        }
        email = loandData.getEmail();
        dni = loandData.getDni();
        loanType = loandData.getLoanType();

        return new LoanCreatedResponseDTO( amount, loanTerm, email, dni, loanType );
    }

    public LoanResponseDTO toLoanCreateResponseDTO(LoanWithUser loanWithUser) {
        Loan loan = loanWithUser.getLoan();
        UserSnapshot user = loanWithUser.getUserSnapshot();

        return LoanResponseDTO.builder()
                .amount(loan.getAmount())
                .loanTerm(loan.getLoanTerm())
                .email(loan.getEmail())
                .loanType(loanWithUser.getLoanTypeName())
                .loanStatus(loanWithUser.getLoanStatusName())
                .interestRate(loanWithUser.getInterestRate())
                .totalMontlyDebt(loanWithUser.getTotalMontlyDebt())
                .approvedLoans(loanWithUser.getApprovedLoan())
                .name(user.getName())
                .email(user.getEmail())
                .baseSalary(user.getBaseSalary())
                .build();
    }
}
