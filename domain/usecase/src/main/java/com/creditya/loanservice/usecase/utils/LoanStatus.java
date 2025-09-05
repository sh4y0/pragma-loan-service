package com.creditya.loanservice.usecase.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoanStatus {
    private static final String APPROVED = "Approved";
    private static final String PENDING_REVIEW = "Pending review";
    private static final String REJECTED = "Rejected";

    public boolean isApproved(String statusName) {
        return statusName.equals(APPROVED);
    }

    public boolean isPending(String statusName) {
        return statusName.equals(PENDING_REVIEW);
    }
}
