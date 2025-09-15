package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.loan.data.LoanWithUser;
import com.creditya.loanservice.model.Page;
import com.creditya.loanservice.model.loan.data.LoanJoinedProjection;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import com.creditya.loanservice.model.utils.gateways.UseCaseLogger;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPage;
import com.creditya.loanservice.usecase.exception.IndexOutOfBoundsExceptionPageSize;
import com.creditya.loanservice.usecase.utils.LoanCalculator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.*;

@RequiredArgsConstructor
public class GetPaginationLoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final LoanCalculator loanCalculator;
    private final UseCaseLogger logger;

    private static final int MAX_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE = 0;
    private static final int MIN_PAGE_SIZE = 1;

    public Mono<Page<LoanWithUser>> execute(int page, int size, List<String> filterStatuses) {
        validatePaginationParameters(page, size);

        int normalizedPageSize = Math.min(size, MAX_PAGE_SIZE);

        return resolveStatusIds(filterStatuses)
                .flatMap(statusIds -> buildPage(page, normalizedPageSize, statusIds))
                .doOnSubscribe(sub -> logExecutionStart(page, size, filterStatuses))
                .doOnSuccess(this::logExecutionSuccess)
                .doOnError(this::logExecutionError);
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < DEFAULT_PAGE) {
            throw new IndexOutOfBoundsExceptionPage();
        }
        if (size < MIN_PAGE_SIZE) {
            throw new IndexOutOfBoundsExceptionPageSize();
        }
    }

    private Mono<List<UUID>> resolveStatusIds(List<String> filterStatuses) {
        if (filterStatuses == null || filterStatuses.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        return loanStatusRepository.findIdsByNames(filterStatuses)
                .collectList()
                .doOnNext(ids -> logger.trace("Resolved {} status IDs from {} filter statuses",
                        ids.size(), filterStatuses.size()))
                .onErrorReturn(Collections.emptyList());
    }

    private Mono<Page<LoanWithUser>> buildPage(int page, int size, List<UUID> statusIds) {
        int offset = page * size;

        Mono<List<LoanJoinedProjection>> loansListMono = getLoansList(statusIds, size, offset);
        Mono<Long> totalCountMono = getTotalCount(statusIds);

        return Mono.zip(loansListMono, totalCountMono)
                .flatMap(tuple -> {
                    List<LoanJoinedProjection> loans = tuple.getT1();
                    Long totalCount = tuple.getT2();

                    return enrichLoansWithUserData(loans)
                            .map(enrichedLoans -> buildPageResponse(page, size, totalCount, enrichedLoans));
                });
    }

    private Mono<List<LoanJoinedProjection>> getLoansList(List<UUID> statusIds, int size, int offset) {
        UUID[] statusIdsArray = statusIds.toArray(UUID[]::new);
        return loanRepository.findLoansWithTypeAndStatus(statusIdsArray, size, offset)
                .collectList();
    }

    private Mono<Long> getTotalCount(List<UUID> statusIds) {
        return statusIds.isEmpty()
                ? loanRepository.countAllLoans()
                : loanRepository.countLoansByStatusIds(statusIds);
    }

    private Mono<List<LoanWithUser>> enrichLoansWithUserData(List<LoanJoinedProjection> loans) {
        if (loans.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        List<UUID> userIds = extractUniqueUserIds(loans);

        return userSnapshotRepository.findUsersByIds(userIds)
                .collectMap(UserSnapshot::getUserId)
                .map(userMap -> mapLoansWithUsers(loans, userMap))
                .onErrorReturn(loans.stream()
                        .map(loan -> buildLoanWithUser(loan, null))
                        .toList());
    }

    private List<UUID> extractUniqueUserIds(List<LoanJoinedProjection> loans) {
        return loans.stream()
                .map(LoanJoinedProjection::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<LoanWithUser> mapLoansWithUsers(List<LoanJoinedProjection> loans, Map<UUID, UserSnapshot> userMap) {
        return loans.stream()
                .map(loan -> buildLoanWithUser(loan, userMap.get(loan.userId())))
                .toList();
    }

    private LoanWithUser buildLoanWithUser(LoanJoinedProjection loan, UserSnapshot user) {
        return LoanWithUser.builder()
                .idLoan(loan.idLoan())
                .userSnapshot(user)
                .amount(loan.amount())
                .loanTerm(loan.loanTerm())
                .email(loan.email())
                .dni(loan.dni())
                .loanStatusName(loan.loanStatusName())
                .loanTypeName(loan.loanTypeName())
                .interestRate(loan.interestRate())
                .totalMonthlyDebt(loanCalculator.calculateTotalMonthlyDebt(loan))
                .approvedLoan(loanCalculator.calculateApprovedLoansCount(loan))
                .build();
    }

    private Page<LoanWithUser> buildPageResponse(int page, int size, long totalElements, List<LoanWithUser> content) {
        int totalPages = calculateTotalPages(totalElements, size);

        logger.trace("Built page response: page={}, size={}, totalElements={}, totalPages={}, contentSize={}",
                page, size, totalElements, totalPages, content.size());

        return Page.<LoanWithUser>builder()
                .content(content)
                .start(page)
                .limit(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    private int calculateTotalPages(long totalElements, int size) {
        return totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    private void logExecutionStart(int page, int size, List<String> filterStatuses) {
        logger.trace("Executing GetPaginationLoanUseCase: page={}, size={}, filters={}",
                page, size, filterStatuses);
    }

    private void logExecutionSuccess(Page<LoanWithUser> result) {
        if (result != null) {
            logger.trace("Successfully executed GetPaginationLoanUseCase: returned {} items",
                    result.getContent().size());
        }
    }

    private void logExecutionError(Throwable error) {
        logger.error("Error executing GetPaginationLoanUseCase: {}", error.getMessage(), error);
    }
}