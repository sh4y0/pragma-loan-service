package com.creditya.loanservice.r2dbc.loan_adapter;

import com.creditya.loanservice.model.creditanalisys.ActiveLoanDetails;
import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.r2dbc.entity.LoanEntity;
import lombok.NonNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, UUID>, ReactiveQueryByExampleExecutor<LoanEntity> {

    Mono<Long> countByIdStatusIn(List<UUID> statusIds);
    @NonNull
    Mono<Long>  count();

    @Query("""
    SELECT
        l.id_loan,
        l.user_id,
        l.amount,
        l.loan_term,
        l.email,
        l.dni,
        COALESCE(s.name, 'UNKNOWN') AS loan_status_name,
        COALESCE(t.name, 'UNKNOWN') AS loan_type_name,
        COALESCE(t.interest_rate, 0.0) AS interest_rate
    FROM loan l
    LEFT JOIN loan_type t ON l.id_loan_type = t.id_loan_type
    LEFT JOIN loan_status s ON l.id_status = s.id_status
     WHERE 
      l.id_status != (SELECT ls.id_status FROM loan_status ls WHERE ls.name = 'Approved')
      AND (
        CARDINALITY(:statusIds) = 0
        OR l.id_status = ANY(:statusIds)
      )
    ORDER BY l.id_loan
    LIMIT :limit OFFSET :offset
""")
    Flux<LoanJoinedProjection> findLoansWithTypeAndStatus(
                                                          @Param("statusIds") UUID[] statusIds,
                                                          @Param("limit") int limit,
                                                          @Param("offset") int offset);

    @Query("""
    SELECT
        l.id_loan,
        l.user_id,
        l.amount,
        l.loan_term,
        l.email,
        l.dni,
        COALESCE(s.name, 'UNKNOWN') AS loan_status_name,
        COALESCE(t.name, 'UNKNOWN') AS loan_type_name,
        COALESCE(t.interest_rate, 0.0) AS interest_rate
    FROM loan l
    LEFT JOIN loan_type t ON l.id_loan_type = t.id_loan_type
    LEFT JOIN loan_status s ON l.id_status = s.id_status
    WHERE 
      l.id_status != (SELECT ls.id_status FROM loan_status ls WHERE ls.name = 'Approved')
    ORDER BY l.id_loan
""")
    Flux<LoanJoinedProjection> findAllLoans();


    @Query("""
        SELECT
            l.id_loan,
            l.amount,
            l.loan_term,
            t.interest_rate
        FROM
            loan l
        JOIN
            loan_type t ON l.id_loan_type = t.id_loan_type
        WHERE
            l.user_id = :userId
        AND
            l.id_status = (SELECT id_status FROM loan_status WHERE name = 'Approved' LIMIT 1)
    """)
    Flux<ActiveLoanDetails> findActiveLoansByUserId(UUID uuid);


}
