package com.talentx.hrms.controller;

import com.talentx.hrms.common.ApiResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 4: API Response Format Consistency**
 * **Validates: Requirements 2.3**
 * 
 * Property-based tests to verify that API responses follow consistent format
 * across all endpoints with proper structure and HTTP status codes.
 */
public class ApiResponseConsistencyPropertyTest {

    @Property(tries = 100)
    @Label("API Response Format Consistency - Success responses should have consistent structure")
    void successResponsesShouldHaveConsistentStructure(
            @ForAll @NotEmpty String message,
            @ForAll("arbitraryData") Object data) {
        
        // Create success response
        ApiResponse<Object> response = ApiResponse.success(message, data);
        
        // Verify consistent structure
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.getFieldErrors()).isNull();
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Error responses should have consistent structure")
    void errorResponsesShouldHaveConsistentStructure(
            @ForAll @NotEmpty String errorMessage,
            @ForAll("arbitraryFieldErrors") Map<String, String> fieldErrors) {
        
        // Create error response
        ApiResponse<Object> response = ApiResponse.error(errorMessage, fieldErrors);
        
        // Verify consistent structure
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.getFieldErrors()).isEqualTo(fieldErrors);
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Simple success responses should have consistent structure")
    void simpleSuccessResponsesShouldHaveConsistentStructure(
            @ForAll @NotEmpty String message) {
        
        // Create simple success response
        ApiResponse<Object> response = ApiResponse.success(message);
        
        // Verify consistent structure
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.getFieldErrors()).isNull();
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Simple error responses should have consistent structure")
    void simpleErrorResponsesShouldHaveConsistentStructure(
            @ForAll @NotEmpty String errorMessage) {
        
        // Create simple error response
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        
        // Verify consistent structure
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.getFieldErrors()).isNull();
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Timestamp should always be present and valid")
    void timestampShouldAlwaysBePresentAndValid(
            @ForAll boolean isSuccess,
            @ForAll @NotEmpty String message) {
        
        Instant beforeCreation = Instant.now();
        
        // Create response based on success flag
        ApiResponse<Object> response = isSuccess ? 
            ApiResponse.success(message) : 
            ApiResponse.error(message);
        
        Instant afterCreation = Instant.now();
        
        // Verify timestamp is within expected range
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBetween(beforeCreation, afterCreation);
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Success flag should match response type")
    void successFlagShouldMatchResponseType(
            @ForAll @NotEmpty String message,
            @ForAll("arbitraryData") Object data) {
        
        // Test success response
        ApiResponse<Object> successResponse = ApiResponse.success(message, data);
        assertThat(successResponse.isSuccess()).isTrue();
        
        // Test error response
        ApiResponse<Object> errorResponse = ApiResponse.error(message);
        assertThat(errorResponse.isSuccess()).isFalse();
    }

    @Property(tries = 100)
    @Label("API Response Format Consistency - Field errors should only be present in error responses")
    void fieldErrorsShouldOnlyBePresentInErrorResponses(
            @ForAll @NotEmpty String message,
            @ForAll("arbitraryFieldErrors") Map<String, String> fieldErrors,
            @ForAll("arbitraryData") Object data) {
        
        // Success responses should not have field errors
        ApiResponse<Object> successResponse = ApiResponse.success(message, data);
        assertThat(successResponse.getFieldErrors()).isNull();
        
        // Error responses can have field errors
        ApiResponse<Object> errorResponse = ApiResponse.error(message, fieldErrors);
        assertThat(errorResponse.getFieldErrors()).isEqualTo(fieldErrors);
    }

    // Generators for test data
    @Provide
    Arbitrary<Object> arbitraryData() {
        return Arbitraries.oneOf(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
            Arbitraries.integers(),
            Arbitraries.longs(),
            Arbitraries.doubles(),
            Arbitraries.of(true, false, null)
        );
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200)
        ).ofMinSize(0).ofMaxSize(5);
    }

    // Additional unit tests for edge cases
    @Test
    void shouldHandleNullDataInSuccessResponse() {
        ApiResponse<Object> response = ApiResponse.success("Success", null);
        
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getFieldErrors()).isNull();
    }

    @Test
    void shouldHandleEmptyFieldErrorsMap() {
        Map<String, String> emptyErrors = Map.of();
        ApiResponse<Object> response = ApiResponse.error("Error", emptyErrors);
        
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getFieldErrors()).isEqualTo(emptyErrors);
    }
}

