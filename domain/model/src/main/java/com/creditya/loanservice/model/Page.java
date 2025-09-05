package com.creditya.loanservice.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Page<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

}