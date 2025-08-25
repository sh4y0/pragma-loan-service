package com.creditya.loanservice.model.loanstatus;

import lombok.Getter;

@Getter
public enum Status {
    PENDING("Pending review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DISBURSED("Loan has been disbursed to the client"),
    ACTIVE("Loan is active and payments are being made"),
    DELINQUENT("Loan payments are overdue"),
    CLOSED("Loan has been fully paid or cancelled");

    private final String name;

    Status(String name) {
        this.name = name;
    }

}
