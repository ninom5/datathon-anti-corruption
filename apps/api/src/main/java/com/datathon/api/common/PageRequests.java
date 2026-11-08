package com.datathon.api.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequests {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageRequests() {
    }

    /** A size above {@link #MAX_SIZE} is cut to it, not rejected, so a client asking for too much still gets an answer. */
    public static Pageable of(Integer page, Integer size, Sort sort) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;
        if (pageNumber < 0) {
            throw new InvalidRequestException("page: ne smije biti negativan");
        }
        if (pageSize < 1) {
            throw new InvalidRequestException("size: mora biti najmanje 1");
        }
        return PageRequest.of(pageNumber, Math.min(pageSize, MAX_SIZE), sort);
    }
}
