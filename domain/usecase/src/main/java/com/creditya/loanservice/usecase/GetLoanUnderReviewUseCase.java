package com.creditya.loanservice.usecase;

import com.creditya.loanservice.model.LoanWithUser;
import com.creditya.loanservice.model.PageResponse;
import com.creditya.loanservice.model.loan.Loan;
import com.creditya.loanservice.model.loan.gateways.LoanRepository;
import com.creditya.loanservice.model.loanstatus.gateways.LoanStatusRepository;
import com.creditya.loanservice.model.usersnapshot.UserSnapshot;
import com.creditya.loanservice.model.usersnapshot.gateways.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetLoanUnderReviewUseCase {

    private final LoanRepository loanRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final UserSnapshotRepository userSnapshotRepository;

    public Mono<PageResponse<LoanWithUser>> execute(int page, int size) {
        List<String> reviewStatusNames = List.of(
                "Pending review",
                "Rejected",
                "Manual review"
        );

        return loanStatusRepository.findIdsByNames(reviewStatusNames)
                .collectList()
                .flatMap(statusIds -> {
                    Mono<Long> totalMono = loanRepository.countLoansByStatusIds(statusIds);
                    Mono<List<Loan>> loansMono = loanRepository
                            .findLoansByStatusIds(statusIds)
                            .collectList();

                    return Mono.zip(totalMono, loansMono);
                })
                .flatMap(tuple -> {
                    Long total = tuple.getT1();
                    List<Loan> loans = tuple.getT2();
                    List<UUID> userIds = loans.stream()
                            .map(Loan::getUserId)
                            .toList();

                    return userSnapshotRepository.findUsersByIds(userIds)
                            .collectMap(UserSnapshot::getUserId)
                            .map(userMap -> {
                                List<LoanWithUser> content = loans.stream()
                                        .map(loan -> LoanWithUser.builder()
                                                .loan(loan)
                                                .userSnapshot(userMap.get(loan.getUserId()))
                                                .build()
                                        )
                                        .toList();
                                return PageResponse.<LoanWithUser>builder()
                                        .content(content)
                                        .page(page)
                                        .size(size)
                                        .totalElements(total)
                                        .totalPages((int) Math.ceil((double) total / size))
                                        .build();
                            });
                });
    }

}
