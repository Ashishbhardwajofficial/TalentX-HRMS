package com.talentx.hrms.entity;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity accessor methods.
 * Validates: Requirements 1.3, 1.6
 */
@DisplayName("User Entity Tests")
class UserEntityTest {

    private User user;
    private Organization organization;

    @BeforeEach
    void setUp() {
        user = new User();
        organization = new Organization();
        organization.setId(1L);
    }

    @Test
    @DisplayName("Should set and get firstName")
    void testFirstNameAccessors() {
        String firstName = "John";
        user.setFirstName(firstName);
        assertThat(user.getFirstName()).isEqualTo(firstName);
    }

    @Test
    @DisplayName("Should set and get lastName")
    void testLastNameAccessors() {
        String lastName = "Doe";
        user.setLastName(lastName);
        assertThat(user.getLastName()).isEqualTo(lastName);
    }

    @Test
    @DisplayName("Should set and get phone")
    void testPhoneAccessors() {
        String phone = "+1234567890";
        user.setPhone(phone);
        assertThat(user.getPhone()).isEqualTo(phone);
    }

    @Test
    @DisplayName("Should set and get username")
    void testUsernameAccessors() {
        String username = "johndoe";
        user.setUsername(username);
        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should set and get email")
    void testEmailAccessors() {
        String email = "john.doe@example.com";
        user.setEmail(email);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should set and get passwordHash")
    void testPasswordHashAccessors() {
        String passwordHash = "hashedPassword123";
        user.setPasswordHash(passwordHash);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    @DisplayName("Should set and get organization")
    void testOrganizationAccessors() {
        user.setOrganization(organization);
        assertThat(user.getOrganization()).isEqualTo(organization);
    }

    @Test
    @DisplayName("Should set and get isActive")
    void testIsActiveAccessors() {
        Boolean isActive = true;
        user.setIsActive(isActive);
        assertThat(user.getIsActive()).isEqualTo(isActive);
    }

    @Test
    @DisplayName("Should set and get accountExpired")
    void testAccountExpiredAccessors() {
        Boolean accountExpired = false;
        user.setAccountExpired(accountExpired);
        assertThat(user.getAccountExpired()).isEqualTo(accountExpired);
    }

    @Test
    @DisplayName("Should set and get credentialsExpired")
    void testCredentialsExpiredAccessors() {
        Boolean credentialsExpired = false;
        user.setCredentialsExpired(credentialsExpired);
        assertThat(user.getCredentialsExpired()).isEqualTo(credentialsExpired);
    }

    @Test
    @DisplayName("Should set and get passwordChangedAt")
    void testPasswordChangedAtAccessors() {
        Timestamp passwordChangedAt = new Timestamp(System.currentTimeMillis());
        user.setPasswordChangedAt(passwordChangedAt);
        assertThat(user.getPasswordChangedAt()).isEqualTo(passwordChangedAt);
    }

    @Test
    @DisplayName("Should set and get mustChangePassword")
    void testMustChangePasswordAccessors() {
        Boolean mustChangePassword = true;
        user.setMustChangePassword(mustChangePassword);
        assertThat(user.getMustChangePassword()).isEqualTo(mustChangePassword);
    }

    @Test
    @DisplayName("Should properly store and retrieve all fields together")
    void testAllFieldsAccessors() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String passwordHash = "hashedPassword123";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+1234567890";
        Boolean isActive = true;
        Boolean accountExpired = false;
        Boolean credentialsExpired = false;
        Boolean mustChangePassword = false;
        Timestamp passwordChangedAt = new Timestamp(System.currentTimeMillis());

        // Act
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setOrganization(organization);
        user.setIsActive(isActive);
        user.setAccountExpired(accountExpired);
        user.setCredentialsExpired(credentialsExpired);
        user.setMustChangePassword(mustChangePassword);
        user.setPasswordChangedAt(passwordChangedAt);

        // Assert
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getLastName()).isEqualTo(lastName);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getOrganization()).isEqualTo(organization);
        assertThat(user.getIsActive()).isEqualTo(isActive);
        assertThat(user.getAccountExpired()).isEqualTo(accountExpired);
        assertThat(user.getCredentialsExpired()).isEqualTo(credentialsExpired);
        assertThat(user.getMustChangePassword()).isEqualTo(mustChangePassword);
        assertThat(user.getPasswordChangedAt()).isEqualTo(passwordChangedAt);
    }

    @Test
    @DisplayName("Should handle null values properly")
    void testNullValues() {
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhone(null);
        user.setUsername(null);
        user.setIsActive(null);
        user.setAccountExpired(null);
        user.setCredentialsExpired(null);
        user.setMustChangePassword(null);
        user.setPasswordChangedAt(null);

        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getIsActive()).isNull();
        assertThat(user.getAccountExpired()).isNull();
        assertThat(user.getCredentialsExpired()).isNull();
        assertThat(user.getMustChangePassword()).isNull();
        assertThat(user.getPasswordChangedAt()).isNull();
    }

    @Test
    @DisplayName("Should use constructor properly")
    void testConstructor() {
        String username = "johndoe";
        String email = "john.doe@example.com";
        String passwordHash = "hashedPassword123";

        User constructedUser = new User(username, email, passwordHash, organization);

        assertThat(constructedUser.getUsername()).isEqualTo(username);
        assertThat(constructedUser.getEmail()).isEqualTo(email);
        assertThat(constructedUser.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(constructedUser.getOrganization()).isEqualTo(organization);
    }

    @Test
    @DisplayName("Should generate full name from firstName and lastName")
    void testGetFullName() {
        user.setFirstName("John");
        user.setLastName("Doe");
        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return firstName when lastName is null")
    void testGetFullNameWithOnlyFirstName() {
        user.setFirstName("John");
        user.setLastName(null);
        assertThat(user.getFullName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return lastName when firstName is null")
    void testGetFullNameWithOnlyLastName() {
        user.setFirstName(null);
        user.setLastName("Doe");
        assertThat(user.getFullName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return email when both firstName and lastName are null")
    void testGetFullNameWithNoNames() {
        user.setEmail("john.doe@example.com");
        user.setFirstName(null);
        user.setLastName(null);
        assertThat(user.getFullName()).isEqualTo("john.doe@example.com");
    }
}
