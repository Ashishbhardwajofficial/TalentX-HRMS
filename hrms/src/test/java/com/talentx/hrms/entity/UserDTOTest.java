package com.talentx.hrms.entity;

import com.talentx.hrms.dto.user.UserRequest;
import com.talentx.hrms.dto.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User DTOs accessor methods.
 * Validates: Requirements 1.4, 1.5, 1.6
 */
@DisplayName("User DTO Tests")
class UserDTOTest {

    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userResponse = new UserResponse();
    }

    // UserRequest Tests

    @Test
    @DisplayName("UserRequest should set and get firstName")
    void testUserRequestFirstNameAccessors() {
        String firstName = "John";
        userRequest.setFirstName(firstName);
        assertThat(userRequest.getFirstName()).isEqualTo(firstName);
    }

    @Test
    @DisplayName("UserRequest should set and get lastName")
    void testUserRequestLastNameAccessors() {
        String lastName = "Doe";
        userRequest.setLastName(lastName);
        assertThat(userRequest.getLastName()).isEqualTo(lastName);
    }

    @Test
    @DisplayName("UserRequest should set and get phone")
    void testUserRequestPhoneAccessors() {
        String phone = "+1234567890";
        userRequest.setPhone(phone);
        assertThat(userRequest.getPhone()).isEqualTo(phone);
    }

    @Test
    @DisplayName("UserRequest should set and get username")
    void testUserRequestUsernameAccessors() {
        String username = "johndoe";
        userRequest.setUsername(username);
        assertThat(userRequest.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("UserRequest should set and get email")
    void testUserRequestEmailAccessors() {
        String email = "john.doe@example.com";
        userRequest.setEmail(email);
        assertThat(userRequest.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("UserRequest should set and get passwordHash")
    void testUserRequestPasswordHashAccessors() {
        String passwordHash = "hashedPassword123";
        userRequest.setPasswordHash(passwordHash);
        assertThat(userRequest.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    @DisplayName("UserRequest should set and get organizationId")
    void testUserRequestOrganizationIdAccessors() {
        Long organizationId = 1L;
        userRequest.setOrganizationId(organizationId);
        assertThat(userRequest.getOrganizationId()).isEqualTo(organizationId);
    }

    @Test
    @DisplayName("UserRequest should properly store and retrieve all fields together")
    void testUserRequestAllFieldsAccessors() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String passwordHash = "hashedPassword123";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+1234567890";
        Boolean isActive = true;
        Long organizationId = 1L;

        // Act
        userRequest.setUsername(username);
        userRequest.setEmail(email);
        userRequest.setPasswordHash(passwordHash);
        userRequest.setFirstName(firstName);
        userRequest.setLastName(lastName);
        userRequest.setPhone(phone);
        userRequest.setIsActive(isActive);
        userRequest.setOrganizationId(organizationId);

        // Assert
        assertThat(userRequest.getUsername()).isEqualTo(username);
        assertThat(userRequest.getEmail()).isEqualTo(email);
        assertThat(userRequest.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(userRequest.getFirstName()).isEqualTo(firstName);
        assertThat(userRequest.getLastName()).isEqualTo(lastName);
        assertThat(userRequest.getPhone()).isEqualTo(phone);
        assertThat(userRequest.getIsActive()).isEqualTo(isActive);
        assertThat(userRequest.getOrganizationId()).isEqualTo(organizationId);
    }

    // UserResponse Tests

    @Test
    @DisplayName("UserResponse should set and get firstName")
    void testUserResponseFirstNameAccessors() {
        String firstName = "John";
        userResponse.setFirstName(firstName);
        assertThat(userResponse.getFirstName()).isEqualTo(firstName);
    }

    @Test
    @DisplayName("UserResponse should set and get lastName")
    void testUserResponseLastNameAccessors() {
        String lastName = "Doe";
        userResponse.setLastName(lastName);
        assertThat(userResponse.getLastName()).isEqualTo(lastName);
    }

    @Test
    @DisplayName("UserResponse should set and get phone")
    void testUserResponsePhoneAccessors() {
        String phone = "+1234567890";
        userResponse.setPhone(phone);
        assertThat(userResponse.getPhone()).isEqualTo(phone);
    }

    @Test
    @DisplayName("UserResponse should set and get accountExpired")
    void testUserResponseAccountExpiredAccessors() {
        Boolean accountExpired = false;
        userResponse.setAccountExpired(accountExpired);
        assertThat(userResponse.getAccountExpired()).isEqualTo(accountExpired);
    }

    @Test
    @DisplayName("UserResponse should set and get credentialsExpired")
    void testUserResponseCredentialsExpiredAccessors() {
        Boolean credentialsExpired = false;
        userResponse.setCredentialsExpired(credentialsExpired);
        assertThat(userResponse.getCredentialsExpired()).isEqualTo(credentialsExpired);
    }

    @Test
    @DisplayName("UserResponse should set and get passwordChangedAt")
    void testUserResponsePasswordChangedAtAccessors() {
        Timestamp passwordChangedAt = new Timestamp(System.currentTimeMillis());
        userResponse.setPasswordChangedAt(passwordChangedAt);
        assertThat(userResponse.getPasswordChangedAt()).isEqualTo(passwordChangedAt);
    }

    @Test
    @DisplayName("UserResponse should set and get mustChangePassword")
    void testUserResponseMustChangePasswordAccessors() {
        Boolean mustChangePassword = true;
        userResponse.setMustChangePassword(mustChangePassword);
        assertThat(userResponse.getMustChangePassword()).isEqualTo(mustChangePassword);
    }

    @Test
    @DisplayName("UserResponse should set and get username")
    void testUserResponseUsernameAccessors() {
        String username = "johndoe";
        userResponse.setUsername(username);
        assertThat(userResponse.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("UserResponse should set and get email")
    void testUserResponseEmailAccessors() {
        String email = "john.doe@example.com";
        userResponse.setEmail(email);
        assertThat(userResponse.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("UserResponse should properly store and retrieve all fields together")
    void testUserResponseAllFieldsAccessors() {
        // Arrange
        Long id = 1L;
        String username = "johndoe";
        String email = "john.doe@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+1234567890";
        Boolean isActive = true;
        Boolean accountExpired = false;
        Boolean credentialsExpired = false;
        Boolean mustChangePassword = false;
        Timestamp passwordChangedAt = new Timestamp(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Long organizationId = 1L;

        // Act
        userResponse.setId(id);
        userResponse.setUsername(username);
        userResponse.setEmail(email);
        userResponse.setFirstName(firstName);
        userResponse.setLastName(lastName);
        userResponse.setPhone(phone);
        userResponse.setIsActive(isActive);
        userResponse.setAccountExpired(accountExpired);
        userResponse.setCredentialsExpired(credentialsExpired);
        userResponse.setMustChangePassword(mustChangePassword);
        userResponse.setPasswordChangedAt(passwordChangedAt);
        userResponse.setCreatedAt(createdAt);
        userResponse.setOrganizationId(organizationId);

        // Assert
        assertThat(userResponse.getId()).isEqualTo(id);
        assertThat(userResponse.getUsername()).isEqualTo(username);
        assertThat(userResponse.getEmail()).isEqualTo(email);
        assertThat(userResponse.getFirstName()).isEqualTo(firstName);
        assertThat(userResponse.getLastName()).isEqualTo(lastName);
        assertThat(userResponse.getPhone()).isEqualTo(phone);
        assertThat(userResponse.getIsActive()).isEqualTo(isActive);
        assertThat(userResponse.getAccountExpired()).isEqualTo(accountExpired);
        assertThat(userResponse.getCredentialsExpired()).isEqualTo(credentialsExpired);
        assertThat(userResponse.getMustChangePassword()).isEqualTo(mustChangePassword);
        assertThat(userResponse.getPasswordChangedAt()).isEqualTo(passwordChangedAt);
        assertThat(userResponse.getCreatedAt()).isEqualTo(createdAt);
        assertThat(userResponse.getOrganizationId()).isEqualTo(organizationId);
    }

    @Test
    @DisplayName("UserResponse should handle null values properly")
    void testUserResponseNullValues() {
        userResponse.setFirstName(null);
        userResponse.setLastName(null);
        userResponse.setPhone(null);
        userResponse.setAccountExpired(null);
        userResponse.setCredentialsExpired(null);
        userResponse.setPasswordChangedAt(null);
        userResponse.setMustChangePassword(null);

        assertThat(userResponse.getFirstName()).isNull();
        assertThat(userResponse.getLastName()).isNull();
        assertThat(userResponse.getPhone()).isNull();
        assertThat(userResponse.getAccountExpired()).isNull();
        assertThat(userResponse.getCredentialsExpired()).isNull();
        assertThat(userResponse.getPasswordChangedAt()).isNull();
        assertThat(userResponse.getMustChangePassword()).isNull();
    }
}
