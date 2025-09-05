package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.LoanWithUser;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.LoanStatus;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.loantype.LoanType;
import com.creditya.loanservice.model.loantype.gateways.LoanTypeRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPageSize;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RequiredArgsConstructor
public class GetPaginationLoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanCalculator loanCalculator;
    private final UseCaseLogger logger;

    public Mono<Page<LoanWithUser>> execute(int page, int size, List<String> filterStatuses) {
        validatePaginationParameters(page, size);

        return resolveStatusIds(filterStatuses)
                .flatMap(statusIds -> buildPagedResponse(page, size, statusIds))
                .doOnError(error -> logger.trace("Error executing GetLoanUnderReviewUseCase", error));
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) throw new IndexOutOfBoundsExceptionPage();
        if (size <= 0) throw new IndexOutOfBoundsExceptionPageSize();
    }

    private Mono<List<UUID>> resolveStatusIds(List<String> filterStatuses) {
        if (filterStatuses == null || filterStatuses.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        return loanStatusRepository.findIdsByNames(filterStatuses)
                .collectList()
                .doOnNext(ids -> logger.trace("Resolved {} status IDs from {} filter statuses",
                        ids.size(), filterStatuses.size()));
    }

    private Mono<Page<LoanWithUser>> buildPagedResponse(int page, int size, List<UUID> statusIds) {
        return Mono.zip(
                getTotalCount(statusIds),
                getPaginatedLoans(page, size, statusIds)
        ).flatMap(tuple -> enrichLoansWithUserData(tuple.getT2())
                .map(enrichedLoans -> buildPageResponse(page, size, tuple.getT1(), enrichedLoans)));
    }

    private Mono<Long> getTotalCount(List<UUID> statusIds) {
        return statusIds.isEmpty() ? loanRepository.countAllLoans()
                : loanRepository.countLoansByStatusIds(statusIds);
    }

    private Mono<List<Loan>> getPaginatedLoans(int page, int size, List<UUID> statusIds) {
        Flux<Loan> loanFlux = statusIds.isEmpty() ? loanRepository.findAllLoans()
                : loanRepository.findLoansByStatusIds(statusIds);

        return loanFlux.skip((long) page * size) // 2 * 10 = 20
                .take(size)
                .collectList();
    }

    private Mono<List<LoanWithUser>> enrichLoansWithUserData(List<Loan> loans) {
        if (loans.isEmpty()) return Mono.just(Collections.emptyList());

        List<UUID> userIds = loans.stream().map(Loan::getUserId).distinct().toList();
        List<UUID> loanTypeIds = loans.stream().map(Loan::getIdLoanType).distinct().toList();
        List<UUID> statusIds = loans.stream().map(Loan::getIdStatus).distinct().toList();

        return Mono.zip(
                userSnapshotRepository.findUsersByIds(userIds).collectMap(UserSnapshot::getUserId),
                loanTypeRepository.findByIds(loanTypeIds).collectMap(LoanType::getIdLoanType),
                loanStatusRepository.findByIds(statusIds).collectMap(LoanStatus::getIdStatus)
        ).map(tuple -> loans.stream()
                .map(loan -> buildLoanWithUser(loan, tuple.getT1(), tuple.getT2(), tuple.getT3(), loans))
                .toList());
    }

    private LoanWithUser buildLoanWithUser(
            Loan loan,
            Map<UUID, UserSnapshot> userMap,
            Map<UUID, LoanType> loanTypeMap,
            Map<UUID, LoanStatus> loanStatusMap,
            List<Loan> allLoans
    ) {
        UserSnapshot user = userMap.get(loan.getUserId());
        LoanType type = loanTypeMap.get(loan.getIdLoanType());
        LoanStatus status = loanStatusMap.get(loan.getIdStatus());

        return LoanWithUser.builder()
                .loan(loan)
                .userSnapshot(user)
                .loanTypeName(type != null ? type.getName() : null)
                .loanStatusName(status != null ? status.getName() : null)
                .interestRate(type != null ? type.getInterestRate() : null)
                .totalMontlyDebt(loanCalculator.calculateTotalMonthlyDebt(user, allLoans, loanTypeMap, loanStatusMap))
                .approvedLoan(loanCalculator.calculateApprovedLoansCount(user, allLoans, loanStatusMap))
                .build();
    }

    private Page<LoanWithUser> buildPageResponse(int page, int size, Long totalElements, List<LoanWithUser> content) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return Page.<LoanWithUser>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
