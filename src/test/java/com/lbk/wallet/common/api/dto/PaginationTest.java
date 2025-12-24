package com.lbk.wallet.common.api.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {

    @Test
    void testPageRequestDefaults() {
        var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(null, null);
        assertEquals(1, pageRequest.page());
        assertEquals(20, pageRequest.limit());
    }

    @Test
    void testPageRequestWithValues() {
        var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(2, 10);
        assertEquals(2, pageRequest.page());
        assertEquals(10, pageRequest.limit());
        assertEquals(10, pageRequest.getOffset());
    }

    @Test
    void testPageRequestToPageable() {
        var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(2, 10);
        var pageable = pageRequest.toPageable();
        assertEquals(1, pageable.getPageNumber()); // 0-based
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void testPageInfoCreation() {
        var pageInfo = PageInfo.of(2, 10, 150L);
        assertEquals(2, pageInfo.page());
        assertEquals(10, pageInfo.limit());
        assertEquals(150L, pageInfo.total());
        assertEquals(15, pageInfo.totalPages());
    }

    @Test
    void testPaginatedResponseFromSpringPage() {
        var data = List.of("item1", "item2", "item3");
        var springPage = new PageImpl<>(data, PageRequest.of(1, 10), 100);

        var paginatedResponse = PaginatedResponse.fromSpringPage(springPage);

        assertEquals(data, paginatedResponse.data());
        assertEquals(2, paginatedResponse.pagination().page()); // 1-based
        assertEquals(10, paginatedResponse.pagination().limit());
        assertEquals(100L, paginatedResponse.pagination().total());
        assertEquals(10, paginatedResponse.pagination().totalPages());
    }
}
