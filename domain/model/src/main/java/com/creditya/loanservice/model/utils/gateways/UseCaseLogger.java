package com.creditya.loanservice.model.utils.gateways;

public interface UseCaseLogger {
    void trace(String message, Object ... args);
    void info(String message, Object ... args);
    void warn(String message, Object ... args);
    void error(String message, Object ... args);
}
