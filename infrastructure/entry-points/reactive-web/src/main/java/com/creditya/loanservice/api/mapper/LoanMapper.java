package com.creditya.loanservice.api.mapper;

import com.creditya.loanservice.api.dto.request.LoanDTO;
import com.creditya.loanservice.model.loan.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanMapper {

    Loan toDomain(LoanDTO loanDTO);
}
