package com.talentx.hrms.security;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * **Feature: hrms-database-integration, Property 20: Password Security**
 * **Validates: Requirements 8.1**
 * 
 * Property-based tests for password security ensuring proper hashing and JWT token validation.
 */
class PasswordSecurityPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    
    public PasswordSecurityPropertyTest() {
        jwtUtil = new JwtUtil();
        // Set the required fields using reflection since @Value won't work in unit tests
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Property(tries = 100)
    @Label("Password hashing should be secure and non-reversible")
    void passwordHashingShouldBeSecure(@ForAll @StringLength(min = 8, max = 50) String rawPassword) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // Verify properties of secure hashing
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(rawPassword, hashedPassword, "Hashed password should not equal raw password");
        // BCrypt hashes are always 60 characters, so we check for minimum length instead
        assertTrue(hashedPassword.length() >= 60, "BCrypt hashed password should be at least 60 characters");
        assertTrue(passwordEncoder.matches(rawPassword, hashedPassword), "Password encoder should validate correct password");
        
        // Verify that the same password produces different hashes (salt verification)
        String secondHash = passwordEncoder.encode(rawPassword);
        assertNotEquals(hashedPassword, secondHash, "Same password should produce different hashes due to salt");
        assertTrue(passwordEncoder.matches(rawPassword, secondHash), "Both hashes should validate the same password");
    }

    @Property(tries = 100)
    @Label("Password validation should reject incorrect passwords")
    void passwordValidationShouldRejectIncorrectPasswords(
            @ForAll @StringLength(min = 8, max = 50) String correctPassword,
            @ForAll @StringLength(min = 8, max = 50) String incorrectPassword) {
        
        Assume.that(!correctPassword.equals(incorrectPassword));
        
        String hashedPassword = passwordEncoder.encode(correctPassword);
        
        // Correct password should match
        assertTrue(passwordEncoder.matches(correctPassword, hashedPassword), 
                  "Correct password should match its hash");
        
        // Incorrect password should not match
        assertFalse(passwordEncoder.matches(incorrectPassword, hashedPassword), 
                   "Incorrect password should not match the hash");
    }

    @Property(tries = 100)
    @Label("JWT tokens should be properly generated and validated")
    void jwtTokensShouldBeProperlyGeneratedAndValidated(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 8, max = 100) String password) {
        
        // Create a mock user
        Organization org = new Organization();
        org.setId(1L);
        
        User user = new User(username, username + "@test.com", passwordEncoder.encode(password), org);
        
        // Create UserDetails implementation for testing
        var userDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            user.isEnabled(),
            user.isAccountNonExpired(),
            user.isCredentialsNonExpired(),
            user.isAccountNonLocked(),
            java.util.Collections.emptyList()
        );
        
        // Generate token
        String token = jwtUtil.generateToken(userDetails);
        
        // Verify token properties
        assertNotNull(token, "Generated token should not be null");
        assertTrue(token.length() > 0, "Generated token should not be empty");
        assertTrue(token.contains("."), "JWT token should contain dots as separators");
        
        // Verify token validation
        assertTrue(jwtUtil.validateToken(token, userDetails), "Token should be valid for the user it was generated for");
        
        // Verify username extraction
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername, "Extracted username should match original username");
        
        // Verify token expiration is set
        assertNotNull(jwtUtil.extractExpiration(token), "Token should have expiration date");
        assertTrue(jwtUtil.extractExpiration(token).after(new java.util.Date()), "Token should not be expired immediately after generation");
    }

    @Property(tries = 100)
    @Label("JWT token validation should reject invalid tokens")
    void jwtTokenValidationShouldRejectInvalidTokens(
            @ForAll @AlphaChars @StringLength(min = 3, max = 50) String username,
            @ForAll @StringLength(min = 10, max = 100) String invalidToken) {
        
        // Assume the invalid token doesn't look like a real JWT
        Assume.that(!invalidToken.matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$"));
        
        // Create a mock user
        Organization org = new Organization();
        org.setId(1L);
        
        User user = new User(username, username + "@test.com", "hashedPassword", org);
        
        var userDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            true, true, true, true,
            java.util.Collections.emptyList()
        );
        
        // Invalid token should not validate - the validateToken methods should handle exceptions internally
        assertFalse(jwtUtil.validateToken(invalidToken), 
                   "Invalid token should not validate without user details");
        
        // For the two-parameter version, we expect it to return false or throw an exception
        // Both behaviors are acceptable for invalid tokens
        try {
            boolean result = jwtUtil.validateToken(invalidToken, userDetails);
            assertFalse(result, "Invalid token should not validate");
        } catch (Exception e) {
            // Exception is also acceptable for invalid tokens
            assertTrue(true, "Exception thrown for invalid token is acceptable");
        }
    }

    @Provide
    Arbitrary<String> validPasswords() {
        return Combinators.combine(
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(4).ofMaxLength(20),
            Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().withCharRange('0', '9').ofMinLength(1).ofMaxLength(5),
            Arbitraries.strings().withChars("!@#$%^&*").ofMinLength(1).ofMaxLength(3)
        ).as((lower, upper, digits, special) -> lower + upper + digits + special);
    }
}

