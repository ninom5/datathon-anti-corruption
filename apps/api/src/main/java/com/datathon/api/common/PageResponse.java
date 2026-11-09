package com.datathon.api.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, long totalElements, int page, int size) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
