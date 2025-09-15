package com.creditya.loanservice.api.util;

import java.util.Optional;

public class ParseSafe {

    private ParseSafe() {}

    public static int parseSafe(Optional<String> param, int defaultValue) {
        return param.filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
