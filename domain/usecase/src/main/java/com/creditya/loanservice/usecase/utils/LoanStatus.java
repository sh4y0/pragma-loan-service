package com.creditya.loanservice.usecase.utils;

import lombok.RequiredArgsConstructor;

import static com.creditya.loanservice.model.loanstatus.Status.*;

@RequiredArgsConstructor
public class LoanStatus {

    public boolean isApproved(String statusName) {
        return statusName.equals(APPROVED.getName());
    }

    public boolean isPending(String statusName) {
        return statusName.equals(PENDING.name());
    }

    public boolean isRejected(String statusName) {
        return statusName.equals(REJECTED.getName());
    }
}
