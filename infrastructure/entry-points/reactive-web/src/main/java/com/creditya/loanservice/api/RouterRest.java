package com.creditya.loanservice.api;

import com.creditya.loanservice.api.dto.request.LoanCreatedRequestDTO;
import com.creditya.loanservice.api.exception.GlobalExceptionFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/loan",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createLoanDoc",
                    operation = @Operation(
                            summary = "Create a new loan application",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoanCreatedRequestDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Loan application created successfully"
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Validation errors"
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Loan type or status not found"
                                    ),
                                    @ApiResponse(
                                            responseCode = "422",
                                            description = "Loan amount out of allowed range"
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler, GlobalExceptionFilter globalExceptionHandler) {
        return route(POST("/api/v1/loan"), handler::createLoan)
                .andRoute(GET("/api/v1/loan"), handler::getLoans)
                .filter(globalExceptionHandler);
    }
}
