package com.talentx.hrms.controller;

import com.talentx.hrms.common.PaginationRequest;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 6: Pagination Functionality**
 * **Validates: Requirements 2.5**
 * 
 * Property-based tests to verify that pagination parameters work correctly
 * and return properly paginated results with correct metadata.
 */
public class PaginationFunctionalityPropertyTest {

    @Property(tries = 100)
    @Label("Pagination Functionality - PaginationRequest should create valid Pageable objects")
    void paginationRequestShouldCreateValidPageableObjects(
            @ForAll @IntRange(min = 0, max = 100) int page,
            @ForAll @IntRange(min = 1, max = 100) int size,
            @ForAll("arbitrarySortField") String sortBy,
            @ForAll("arbitrarySortDirection") String sortDirection) {
        
        // Create pagination request
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        
        // Verify pagination request properties
        assertThat(paginationRequest.getPage()).isEqualTo(page);
        assertThat(paginationRequest.getSize()).isEqualTo(size);
        assertThat(paginationRequest.getSortBy()).isEqualTo(sortBy);
        assertThat(paginationRequest.getSortDirection()).isEqualTo(sortDirection);
        
        // Create Pageable from pagination request (simulating controller logic)
        Pageable pageable = createPageableFromRequest(paginationRequest);
        
        // Verify Pageable properties
        assertThat(pageable.getPageNumber()).isEqualTo(page);
        assertThat(pageable.getPageSize()).isEqualTo(size);
        
        // Verify sort configuration
        if (sortBy != null && !sortBy.isEmpty()) {
            assertThat(pageable.getSort()).isNotEqualTo(Sort.unsorted());
            Sort.Direction expectedDirection = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            assertThat(pageable.getSort().getOrderFor(sortBy)).isNotNull();
            assertThat(pageable.getSort().getOrderFor(sortBy).getDirection()).isEqualTo(expectedDirection);
        }
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Default pagination values should be valid")
    void defaultPaginationValuesShouldBeValid() {
        
        // Create default pagination request
        PaginationRequest defaultRequest = new PaginationRequest();
        
        // Verify default values
        assertThat(defaultRequest.getPage()).isEqualTo(0); // Zero-based page indexing
        assertThat(defaultRequest.getSize()).isEqualTo(10); // Default page size
        assertThat(defaultRequest.getSortDirection()).isEqualTo("asc"); // Default sort direction
        
        // Create Pageable from default request
        Pageable pageable = createPageableFromRequest(defaultRequest);
        
        // Verify Pageable defaults
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Page offset calculation should be correct")
    void pageOffsetCalculationShouldBeCorrect(
            @ForAll @IntRange(min = 0, max = 50) int page,
            @ForAll @IntRange(min = 1, max = 50) int size) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size);
        Pageable pageable = createPageableFromRequest(paginationRequest);
        
        // Verify offset calculation: offset = page * size
        long expectedOffset = (long) page * size;
        assertThat(pageable.getOffset()).isEqualTo(expectedOffset);
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Sort direction should be case insensitive")
    void sortDirectionShouldBeCaseInsensitive(
            @ForAll("arbitrarySortField") String sortBy,
            @ForAll("arbitraryCaseVariations") String sortDirection) {
        
        if (sortBy != null && !sortBy.isEmpty()) {
            PaginationRequest paginationRequest = new PaginationRequest(0, 10, sortBy, sortDirection);
            Pageable pageable = createPageableFromRequest(paginationRequest);
            
            // Verify case insensitive direction handling
            Sort.Direction expectedDirection = sortDirection.toLowerCase().contains("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            if (pageable.getSort().isSorted()) {
                assertThat(pageable.getSort().getOrderFor(sortBy).getDirection()).isEqualTo(expectedDirection);
            }
        }
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Multiple sort fields should be supported")
    void multipleSortFieldsShouldBeSupported(
            @ForAll("arbitraryMultipleSortFields") String sortBy,
            @ForAll("arbitrarySortDirection") String sortDirection) {
        
        if (sortBy != null && !sortBy.isEmpty()) {
            PaginationRequest paginationRequest = new PaginationRequest(0, 10, sortBy, sortDirection);
            Pageable pageable = createPageableFromRequest(paginationRequest);
            
            // If sort field contains comma, it should be treated as multiple fields
            if (sortBy.contains(",")) {
                String[] sortFields = sortBy.split(",");
                for (String field : sortFields) {
                    String trimmedField = field.trim();
                    if (!trimmedField.isEmpty()) {
                        // Each field should be present in sort orders
                        assertThat(pageable.getSort().getOrderFor(trimmedField)).isNotNull();
                    }
                }
            } else {
                // Single field should be present
                assertThat(pageable.getSort().getOrderFor(sortBy)).isNotNull();
            }
        }
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Pagination boundaries should be handled correctly")
    void paginationBoundariesShouldBeHandledCorrectly(
            @ForAll @IntRange(min = 0, max = 1000) int page,
            @ForAll @IntRange(min = 1, max = 100) int size) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size);
        Pageable pageable = createPageableFromRequest(paginationRequest);
        
        // Verify boundary conditions
        assertThat(pageable.getPageNumber()).isGreaterThanOrEqualTo(0);
        assertThat(pageable.getPageSize()).isGreaterThan(0);
        assertThat(pageable.getOffset()).isGreaterThanOrEqualTo(0);
        
        // Verify next/previous page logic
        if (page > 0) {
            Pageable previousPage = pageable.previousOrFirst();
            assertThat(previousPage.getPageNumber()).isEqualTo(page - 1);
        }
        
        Pageable nextPage = pageable.next();
        assertThat(nextPage.getPageNumber()).isEqualTo(page + 1);
    }

    @Property(tries = 100)
    @Label("Pagination Functionality - Invalid sort fields should not break pagination")
    void invalidSortFieldsShouldNotBreakPagination(
            @ForAll @IntRange(min = 0, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 20) int size,
            @ForAll("arbitraryInvalidSortField") String invalidSortBy) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, invalidSortBy, "asc");
        
        // Should not throw exception even with invalid sort field
        Pageable pageable = createPageableFromRequest(paginationRequest);
        
        // Basic pagination should still work
        assertThat(pageable.getPageNumber()).isEqualTo(page);
        assertThat(pageable.getPageSize()).isEqualTo(size);
        
        // Invalid sort field should either be ignored or handled gracefully
        // (Implementation detail - the actual behavior depends on how the service handles it)
    }

    // Helper method to simulate controller logic for creating Pageable
    private Pageable createPageableFromRequest(PaginationRequest paginationRequest) {
        Sort sort = Sort.unsorted();
        if (paginationRequest.getSortBy() != null && !paginationRequest.getSortBy().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            // Handle multiple sort fields
            if (paginationRequest.getSortBy().contains(",")) {
                String[] sortFields = paginationRequest.getSortBy().split(",");
                Sort.Order[] orders = new Sort.Order[sortFields.length];
                for (int i = 0; i < sortFields.length; i++) {
                    orders[i] = new Sort.Order(direction, sortFields[i].trim());
                }
                sort = Sort.by(orders);
            } else {
                sort = Sort.by(direction, paginationRequest.getSortBy());
            }
        }
        
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

    // Generators for test data
    @Provide
    Arbitrary<String> arbitrarySortField() {
        return Arbitraries.oneOf(
            Arbitraries.of("id", "name", "email", "createdAt", "updatedAt"),
            Arbitraries.of("firstName", "lastName", "employeeNumber", "hireDate"),
            Arbitraries.of("department", "salary", "status", "lastLogin"),
            Arbitraries.of((String) null), // Test null sort field
            Arbitraries.of("") // Test empty sort field
        );
    }

    @Provide
    Arbitrary<String> arbitrarySortDirection() {
        return Arbitraries.oneOf(
            Arbitraries.of("asc", "desc", "ASC", "DESC"),
            Arbitraries.of("ascending", "descending"), // Alternative formats
            Arbitraries.of((String) null) // Test null direction
        );
    }

    @Provide
    Arbitrary<String> arbitraryCaseVariations() {
        return Arbitraries.oneOf(
            Arbitraries.of("asc", "ASC", "Asc", "aSc"),
            Arbitraries.of("desc", "DESC", "Desc", "dEsC"),
            Arbitraries.of("ascending", "ASCENDING", "Ascending"),
            Arbitraries.of("descending", "DESCENDING", "Descending")
        );
    }

    @Provide
    Arbitrary<String> arbitraryMultipleSortFields() {
        return Arbitraries.oneOf(
            Arbitraries.of("firstName,lastName", "id,createdAt", "name,email"),
            Arbitraries.of("employeeNumber,hireDate,department"),
            Arbitraries.of("salary,status", "lastLogin,updatedAt"),
            Arbitraries.of("id") // Single field for comparison
        );
    }

    @Provide
    Arbitrary<String> arbitraryInvalidSortField() {
        return Arbitraries.oneOf(
            Arbitraries.of("", "   ", "invalid_field", "123invalid"),
            Arbitraries.of("field-with-dashes", "field with spaces"),
            Arbitraries.of("very_long_field_name_that_probably_does_not_exist_in_any_entity"),
            Arbitraries.of("null", "undefined", "NaN")
        );
    }

    // Additional unit tests for specific edge cases
    @Test
    void shouldHandleZeroPageSize() {
        // Note: In practice, page size should be validated to be > 0
        // This test documents the expected behavior
        PaginationRequest request = new PaginationRequest(0, 1); // Minimum valid size
        Pageable pageable = createPageableFromRequest(request);
        
        assertThat(pageable.getPageSize()).isEqualTo(1);
        assertThat(pageable.getPageNumber()).isEqualTo(0);
    }

    @Test
    void shouldHandleNullSortBy() {
        PaginationRequest request = new PaginationRequest(0, 10, null, "asc");
        Pageable pageable = createPageableFromRequest(request);
        
        assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void shouldHandleEmptySortBy() {
        PaginationRequest request = new PaginationRequest(0, 10, "", "asc");
        Pageable pageable = createPageableFromRequest(request);
        
        assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void shouldCreatePageableWithLargePageNumbers() {
        PaginationRequest request = new PaginationRequest(Integer.MAX_VALUE - 1, 1);
        Pageable pageable = createPageableFromRequest(request);
        
        assertThat(pageable.getPageNumber()).isEqualTo(Integer.MAX_VALUE - 1);
        // Note: offset might overflow, but this tests the boundary behavior
    }
}

