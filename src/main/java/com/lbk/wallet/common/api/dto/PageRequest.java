package com.lbk.wallet.common.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Standard pagination request parameters
 */
public record PageRequest(
    @Min(1) Integer page,
    @Min(1) @Max(100) Integer limit
) {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;

    public PageRequest {
        if (page == null) page = DEFAULT_PAGE;
        if (limit == null) limit = DEFAULT_LIMIT;
    }

    /**
     * Convert to Spring Data Pageable (0-based)
     */
    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(page - 1, limit);
    }

    /**
     * Calculate offset for custom queries
     */
    public int getOffset() {
        return (page - 1) * limit;
    }
}
