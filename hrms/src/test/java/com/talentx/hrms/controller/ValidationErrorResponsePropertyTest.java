package com.talentx.hrms.controller;

import com.talentx.hrms.common.ApiResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 5: Validation Error Response Completeness**
 * **Validates: Requirements 2.4**
 * 
 * Property-based tests to verify that validation error responses contain
 * detailed field-level validation information with consistent structure.
 */
public class ValidationErrorResponsePropertyTest {

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Error responses should contain field-level validation details")
    void errorResponsesShouldContainFieldLevelValidationDetails(
            @ForAll @NotEmpty String errorMessage,
            @ForAll("arbitraryFieldErrors") Map<String, String> fieldErrors) {
        
        // Create error response with field errors
        ApiResponse<Object> response = ApiResponse.error(errorMessage, fieldErrors);
        
        // Verify response structure
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getData()).isNull();
        assertThat(response.getFieldErrors()).isNotNull();
        assertThat(response.getFieldErrors()).isEqualTo(fieldErrors);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Field errors should have meaningful field names and messages")
    void fieldErrorsShouldHaveMeaningfulFieldNamesAndMessages(
            @ForAll("arbitraryValidationErrors") Map<String, String> validationErrors) {
        
        ApiResponse<Object> response = ApiResponse.error("Validation failed", validationErrors);
        
        // Verify field errors structure
        if (validationErrors != null && !validationErrors.isEmpty()) {
            assertThat(response.getFieldErrors()).isNotNull();
            assertThat(response.getFieldErrors()).hasSize(validationErrors.size());
            
            // Verify each field error has non-empty field name and message
            response.getFieldErrors().forEach((fieldName, errorMessage) -> {
                assertThat(fieldName).isNotNull().isNotEmpty();
                assertThat(errorMessage).isNotNull().isNotEmpty();
            });
        }
    }

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Multiple field errors should be preserved")
    void multipleFieldErrorsShouldBePreserved(
            @ForAll("arbitraryMultipleFieldErrors") Map<String, String> multipleErrors) {
        
        ApiResponse<Object> response = ApiResponse.error("Multiple validation errors", multipleErrors);
        
        // Verify all field errors are preserved
        assertThat(response.getFieldErrors()).isEqualTo(multipleErrors);
        
        if (multipleErrors != null && multipleErrors.size() > 1) {
            assertThat(response.getFieldErrors()).hasSizeGreaterThan(1);
            
            // Verify each error is distinct
            assertThat(response.getFieldErrors().keySet()).hasSize(multipleErrors.size());
        }
    }

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Field error keys should be consistent with common validation patterns")
    void fieldErrorKeysShouldFollowCommonValidationPatterns(
            @ForAll("arbitraryCommonFieldErrors") Map<String, String> commonFieldErrors) {
        
        ApiResponse<Object> response = ApiResponse.error("Common validation errors", commonFieldErrors);
        
        if (commonFieldErrors != null && !commonFieldErrors.isEmpty()) {
            // Verify field names follow common patterns
            response.getFieldErrors().keySet().forEach(fieldName -> {
                // Field names should be valid identifiers (letters, numbers, dots, underscores)
                assertThat(fieldName).matches("^[a-zA-Z][a-zA-Z0-9._]*$");
            });
            
            // Verify error messages are descriptive
            response.getFieldErrors().values().forEach(errorMessage -> {
                assertThat(errorMessage).hasSizeGreaterThan(5); // Meaningful error messages
            });
        }
    }

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Nested field errors should be supported")
    void nestedFieldErrorsShouldBeSupported(
            @ForAll("arbitraryNestedFieldErrors") Map<String, String> nestedFieldErrors) {
        
        ApiResponse<Object> response = ApiResponse.error("Nested validation errors", nestedFieldErrors);
        
        if (nestedFieldErrors != null && !nestedFieldErrors.isEmpty()) {
            // Verify nested field paths are preserved
            response.getFieldErrors().keySet().forEach(fieldPath -> {
                if (fieldPath.contains(".")) {
                    // Nested field path should have at least one dot
                    assertThat(fieldPath.split("\\.")).hasSizeGreaterThan(1);
                    
                    // Each part of the path should be a valid identifier
                    for (String part : fieldPath.split("\\.")) {
                        assertThat(part).matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                    }
                }
            });
        }
    }

    @Property(tries = 100)
    @Label("Validation Error Response Completeness - Error response should maintain consistency regardless of field error count")
    void errorResponseShouldMaintainConsistencyRegardlessOfFieldErrorCount(
            @ForAll @NotEmpty String message,
            @ForAll("arbitraryVariableSizeFieldErrors") Map<String, String> fieldErrors) {
        
        ApiResponse<Object> response = ApiResponse.error(message, fieldErrors);
        
        // Basic structure should always be consistent
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        
        // Field errors should match input exactly
        assertThat(response.getFieldErrors()).isEqualTo(fieldErrors);
        
        // If field errors exist, they should be accessible
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            assertThat(response.getFieldErrors()).isNotEmpty();
            assertThat(response.getFieldErrors()).hasSize(fieldErrors.size());
        }
    }

    // Generators for test data
    @Provide
    Arbitrary<Map<String, String>> arbitraryFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200)
        ).ofMinSize(0).ofMaxSize(10);
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryValidationErrors() {
        return Arbitraries.maps(
            Arbitraries.oneOf(
                Arbitraries.of("firstName", "lastName", "email", "phone", "dateOfBirth"),
                Arbitraries.of("employeeNumber", "department", "salary", "hireDate"),
                Arbitraries.of("username", "password", "confirmPassword")
            ),
            Arbitraries.oneOf(
                Arbitraries.of("is required", "must not be empty", "is invalid"),
                Arbitraries.of("must be a valid email", "must be at least 8 characters"),
                Arbitraries.of("must be a positive number", "must be a future date")
            )
        ).ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryMultipleFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(30),
            Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100)
        ).ofMinSize(2).ofMaxSize(8);
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryCommonFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.oneOf(
                Arbitraries.of("firstName", "lastName", "email", "phoneNumber"),
                Arbitraries.of("employeeId", "departmentId", "managerId"),
                Arbitraries.of("startDate", "endDate", "salary", "hourlyRate")
            ),
            Arbitraries.oneOf(
                Arbitraries.of("First name is required"),
                Arbitraries.of("Email must be a valid email address"),
                Arbitraries.of("Phone number must be in valid format"),
                Arbitraries.of("Salary must be a positive number"),
                Arbitraries.of("Start date cannot be in the past")
            )
        ).ofMinSize(1).ofMaxSize(6);
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryNestedFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.oneOf(
                Arbitraries.of("employee.firstName", "employee.lastName", "employee.address.street"),
                Arbitraries.of("leaveRequest.startDate", "leaveRequest.endDate", "leaveRequest.type.id"),
                Arbitraries.of("payroll.employee.id", "payroll.grossPay", "payroll.deductions.tax")
            ),
            Arbitraries.oneOf(
                Arbitraries.of("Nested field validation failed"),
                Arbitraries.of("Invalid value for nested property"),
                Arbitraries.of("Required nested field is missing")
            )
        ).ofMinSize(1).ofMaxSize(4);
    }

    @Provide
    Arbitrary<Map<String, String>> arbitraryVariableSizeFieldErrors() {
        return Arbitraries.maps(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(40),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(150)
        ).ofMinSize(0).ofMaxSize(15);
    }

    // Additional unit tests for edge cases
    @Test
    void shouldHandleEmptyFieldErrorsMap() {
        Map<String, String> emptyErrors = Map.of();
        ApiResponse<Object> response = ApiResponse.error("Validation error", emptyErrors);
        
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Validation error");
        assertThat(response.getFieldErrors()).isEqualTo(emptyErrors);
        assertThat(response.getFieldErrors()).isEmpty();
    }

    @Test
    void shouldHandleNullFieldErrors() {
        ApiResponse<Object> response = ApiResponse.error("Error without field details");
        
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error without field details");
        assertThat(response.getFieldErrors()).isNull();
    }

    @Test
    void shouldPreserveLongFieldErrorMessages() {
        String longErrorMessage = "This is a very long validation error message that contains detailed information about what went wrong with the field validation and how the user can fix it";
        Map<String, String> fieldErrors = Map.of("description", longErrorMessage);
        
        ApiResponse<Object> response = ApiResponse.error("Validation failed", fieldErrors);
        
        assertThat(response.getFieldErrors().get("description")).isEqualTo(longErrorMessage);
    }
}

