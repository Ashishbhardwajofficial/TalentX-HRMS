package com.talentx.hrms.controller.auth;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.dto.auth.JwtResponse;
import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticate(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User logout", description = "Logout current user and invalidate session")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    /**
     * Get current user information
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("organizationId", user.getOrganization().getId());
            userInfo.put("organizationName", user.getOrganization().getName());
            userInfo.put("roles", user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList());
            userInfo.put("isEnabled", user.isEnabled());
            userInfo.put("isAccountNonLocked", user.isAccountNonLocked());
            userInfo.put("mustChangePassword", user.isMustChangePassword());
            
            return ResponseEntity.ok(ApiResponse.success("User information retrieved", userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unable to retrieve user information"));
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token before expiration")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                JwtResponse jwtResponse = authService.refreshToken(token);
                return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", jwtResponse));
            }
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid authorization header"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Change password for the current user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        try {
            authService.changePassword(currentPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check username availability
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", description = "Check if a username is available for registration")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if an email is available for registration")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Reset password (admin function)
     */
    @PostMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset user password", description = "Reset password for a specific user (admin only)")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {
        try {
            authService.resetPassword(userId, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lock user account (admin function)
     */
    @PostMapping("/lock-account/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock user account", description = "Lock a user account (admin only)")
    public ResponseEntity<ApiResponse<Void>> lockAccount(@PathVariable Long userId) {
        try {
            authService.lockAccount(userId);
            return ResponseEntity.ok(ApiResponse.success("Account locked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Unlock user account (admin function)
     */
    @PostMapping("/unlock-account/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account", description = "Unlock a user account (admin only)")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long userId) {
        try {
            authService.unlockAccount(userId);
            return ResponseEntity.ok(ApiResponse.success("Account unlocked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

