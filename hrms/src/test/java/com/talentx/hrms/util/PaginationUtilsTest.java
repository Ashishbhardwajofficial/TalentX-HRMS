package com.talentx.hrms.util;

import com.talentx.hrms.common.PaginationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PaginationUtils to ensure consistent pagination behavior
 */
public class PaginationUtilsTest {

    @Test
    public void testCreatePageableWithValidRequest() {
        PaginationRequest request = new PaginationRequest(1, 20, "name", "desc");
        Pageable pageable = PaginationUtils.createPageable(request);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "name"), pageable.getSort());
    }

    @Test
    public void testCreatePageableWithNullRequest() {
        Pageable pageable = PaginationUtils.createPageable(null);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.unsorted(), pageable.getSort());
    }

    @Test
    public void testCreatePageableWithInvalidPage() {
        PaginationRequest request = new PaginationRequest(-1, 10, null, null);
        Pageable pageable = PaginationUtils.createPageable(request);

        assertEquals(0, pageable.getPageNumber()); // Should be normalized to 0
    }

    @Test
    public void testCreatePageableWithInvalidSize() {
        PaginationRequest request = new PaginationRequest(0, 200, null, null);
        Pageable pageable = PaginationUtils.createPageable(request);

        assertEquals(100, pageable.getPageSize()); // Should be capped at MAX_PAGE_SIZE
    }

    @Test
    public void testCreatePageableWithZeroSize() {
        PaginationRequest request = new PaginationRequest(0, 0, null, null);
        Pageable pageable = PaginationUtils.createPageable(request);

        assertEquals(1, pageable.getPageSize()); // Should be normalized to 1
    }

    @Test
    public void testCreateSortWithValidParameters() {
        Sort sort = PaginationUtils.createSort("name", "desc");
        assertEquals(Sort.by(Sort.Direction.DESC, "name"), sort);
    }

    @Test
    public void testCreateSortWithNullSortBy() {
        Sort sort = PaginationUtils.createSort(null, "desc");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    public void testCreateSortWithEmptySortBy() {
        Sort sort = PaginationUtils.createSort("", "desc");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    public void testCreateSortWithWhitespaceSortBy() {
        Sort sort = PaginationUtils.createSort("  ", "desc");
        assertEquals(Sort.unsorted(), sort);
    }

    @Test
    public void testParseSortDirectionAsc() {
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection("asc"));
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection("ASC"));
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection("Asc"));
    }

    @Test
    public void testParseSortDirectionDesc() {
        assertEquals(Sort.Direction.DESC, PaginationUtils.parseSortDirection("desc"));
        assertEquals(Sort.Direction.DESC, PaginationUtils.parseSortDirection("DESC"));
        assertEquals(Sort.Direction.DESC, PaginationUtils.parseSortDirection("Desc"));
    }

    @Test
    public void testParseSortDirectionInvalid() {
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection("invalid"));
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection(null));
        assertEquals(Sort.Direction.ASC, PaginationUtils.parseSortDirection(""));
    }

    @Test
    public void testValidateAndNormalize() {
        PaginationRequest normalized = PaginationUtils.validateAndNormalize(-1, 200, "  name  ", "  DESC  ");

        assertEquals(0, normalized.getPage());
        assertEquals(100, normalized.getSize());
        assertEquals("name", normalized.getSortBy());
        assertEquals("DESC", normalized.getSortDirection());
    }

    @Test
    public void testCreateDefault() {
        PaginationRequest defaultRequest = PaginationUtils.createDefault();

        assertEquals(0, defaultRequest.getPage());
        assertEquals(10, defaultRequest.getSize());
        assertNull(defaultRequest.getSortBy());
        assertEquals("asc", defaultRequest.getSortDirection());
    }

    @Test
    public void testCreateWithPageAndSize() {
        PaginationRequest request = PaginationUtils.create(2, 25);

        assertEquals(2, request.getPage());
        assertEquals(25, request.getSize());
        assertNull(request.getSortBy());
        assertEquals("asc", request.getSortDirection());
    }

    @Test
    public void testCreateWithAllParameters() {
        PaginationRequest request = PaginationUtils.create(1, 15, "createdAt", "desc");

        assertEquals(1, request.getPage());
        assertEquals(15, request.getSize());
        assertEquals("createdAt", request.getSortBy());
        assertEquals("desc", request.getSortDirection());
    }

    @Test
    public void testIsValidPagination() {
        assertTrue(PaginationUtils.isValidPagination(0, 10));
        assertTrue(PaginationUtils.isValidPagination(5, 50));
        assertTrue(PaginationUtils.isValidPagination(0, 100));

        assertFalse(PaginationUtils.isValidPagination(-1, 10)); // Negative page
        assertFalse(PaginationUtils.isValidPagination(0, 0));   // Zero size
        assertFalse(PaginationUtils.isValidPagination(0, 101)); // Size too large
    }

    @Test
    public void testGetMaxPageSize() {
        assertEquals(100, PaginationUtils.getMaxPageSize());
    }
}

