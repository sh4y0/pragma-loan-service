package com.creditya.loanservice.model.usersnapshot;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserSnapshot {
    private UUID userId;
    private String name;
    private String email;
    private BigDecimal baseSalary;
}
