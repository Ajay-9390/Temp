package com.example.nba.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Serializable pagination wrapper so we never leak Spring's {@code Page} implementation
 * (which has an unstable JSON shape) across the API boundary.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return from(page, Function.identity());
    }
}
