package com.creditya.loanservice.api;

import com.creditya.loanservice.api.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler, GlobalExceptionHandler globalExceptionHandler) {
        return route(POST("/api/v1/loan"), handler::createLoan)
                .filter(globalExceptionHandler);
    }
}
