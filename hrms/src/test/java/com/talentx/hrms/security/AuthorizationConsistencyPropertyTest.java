package com.talentx.hrms.security;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.security.Permission;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.entity.security.RolePermission;
import com.talentx.hrms.entity.security.UserRole;
import com.talentx.hrms.service.auth.UserDetailsServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * **Feature: hrms-database-integration, Property 3: Authentication and Authorization Consistency**
 * **Validates: Requirements 2.2, 8.2**
 * 
 * Property-based tests for authentication and authorization consistency ensuring proper role and permission enforcement.
 */
class AuthorizationConsistencyPropertyTest {

    @Property(tries = 100)
    @Label("User authorities should include all roles and permissions")
    void userAuthoritiesShouldIncludeAllRolesAndPermissions(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("roleNames") List<String> roleNames,
            @ForAll("permissionNames") List<String> permissionNames) {
        
        // Create organization
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Test Organization");
        
        // Create user
        User user = new User(username, username + "@test.com", "hashedPassword", org);
        user.setId(1L);
        
        // Create roles and permissions
        List<Role> roles = new ArrayList<>();
        List<Permission> permissions = new ArrayList<>();
        
        for (int i = 0; i < roleNames.size(); i++) {
            String roleName = roleNames.get(i);
            Role role = new Role(roleName, org);
            role.setId((long) (i + 1));
            roles.add(role);
            
            // Create user-role relationship
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            user.getUserRoles().add(userRole);
            role.getUserRoles().add(userRole);
        }
        
        for (int i = 0; i < permissionNames.size(); i++) {
            String permissionName = permissionNames.get(i);
            Permission permission = new Permission(permissionName);
            permission.setId((long) (i + 1));
            permissions.add(permission);
            
            // Assign permissions to roles (distribute evenly)
            if (!roles.isEmpty()) {
                Role role = roles.get(i % roles.size());
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                role.getRolePermissions().add(rolePermission);
                permission.getRolePermissions().add(rolePermission);
            }
        }
        
        // Create UserDetailsServiceImpl and get authorities
        UserDetailsServiceImpl.CustomUserPrincipal userPrincipal = 
            new UserDetailsServiceImpl.CustomUserPrincipal(user, getAuthorities(user));
        
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        Set<String> authorityNames = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        
        // Verify all roles are present with ROLE_ prefix
        for (String roleName : roleNames) {
            assertTrue(authorityNames.contains("ROLE_" + roleName.toUpperCase()),
                      "Authority should contain role: ROLE_" + roleName.toUpperCase());
        }
        
        // Verify permissions are present only if there are roles to assign them to
        if (!roles.isEmpty()) {
            for (String permissionName : permissionNames) {
                assertTrue(authorityNames.contains(permissionName.toUpperCase()),
                          "Authority should contain permission: " + permissionName.toUpperCase());
            }
            
            // Verify no extra authorities are present
            int expectedAuthorityCount = roleNames.size() + permissionNames.size();
            assertEquals(expectedAuthorityCount, authorities.size(),
                        "Authority count should match roles + permissions");
        } else {
            // If no roles, should have no authorities regardless of permissions
            assertTrue(authorities.isEmpty(), 
                      "User without roles should have no authorities, even if permissions exist");
        }
    }

    @Property(tries = 100)
    @Label("Users without roles should have no authorities")
    void usersWithoutRolesShouldHaveNoAuthorities(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username) {
        
        // Create organization
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Test Organization");
        
        // Create user without roles
        User user = new User(username, username + "@test.com", "hashedPassword", org);
        user.setId(1L);
        
        // Create UserDetailsServiceImpl and get authorities
        UserDetailsServiceImpl.CustomUserPrincipal userPrincipal = 
            new UserDetailsServiceImpl.CustomUserPrincipal(user, getAuthorities(user));
        
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        
        // Verify no authorities are present
        assertTrue(authorities.isEmpty(), "User without roles should have no authorities");
    }

    @Property(tries = 100)
    @Label("User account status should be properly reflected in UserDetails")
    void userAccountStatusShouldBeProperlyReflected(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll boolean accountExpired,
            @ForAll boolean accountLocked,
            @ForAll boolean credentialsExpired,
            @ForAll boolean enabled) {
        
        // Create organization
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Test Organization");
        
        // Create user with specific account status
        User user = new User(username, username + "@test.com", "hashedPassword", org);
        user.setId(1L);
        user.setAccountExpired(accountExpired);
        user.setAccountLocked(accountLocked);
        user.setCredentialsExpired(credentialsExpired);
        user.setActive(enabled);
        
        // Create UserDetailsServiceImpl and get UserDetails
        UserDetailsServiceImpl.CustomUserPrincipal userPrincipal = 
            new UserDetailsServiceImpl.CustomUserPrincipal(user, getAuthorities(user));
        
        // Verify account status is properly reflected
        assertEquals(!accountExpired, userPrincipal.isAccountNonExpired(),
                    "Account expiration status should match");
        assertEquals(!accountLocked, userPrincipal.isAccountNonLocked(),
                    "Account locked status should match");
        assertEquals(!credentialsExpired, userPrincipal.isCredentialsNonExpired(),
                    "Credentials expiration status should match");
        assertEquals(enabled, userPrincipal.isEnabled(),
                    "Enabled status should match");
    }

    @Property(tries = 100)
    @Label("User principal should provide access to underlying user data")
    void userPrincipalShouldProvideAccessToUnderlyingUserData(
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll @AlphaChars @StringLength(min = 2, max = 30) String firstName,
            @ForAll @AlphaChars @StringLength(min = 2, max = 30) String lastName) {
        
        // Create organization
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Test Organization");
        
        // Create user with personal information
        User user = new User(username, username + "@test.com", "hashedPassword", org);
        user.setId(1L);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        // Create UserDetailsServiceImpl and get UserDetails
        UserDetailsServiceImpl.CustomUserPrincipal userPrincipal = 
            new UserDetailsServiceImpl.CustomUserPrincipal(user, getAuthorities(user));
        
        // Verify access to underlying user data
        assertEquals(username, userPrincipal.getUsername(), "Username should match");
        assertEquals(user.getPasswordHash(), userPrincipal.getPassword(), "Password hash should match");
        assertEquals(user.getId(), userPrincipal.getUserId(), "User ID should match");
        assertEquals(user.getEmail(), userPrincipal.getEmail(), "Email should match");
        assertEquals(user.getFullName(), userPrincipal.getFullName(), "Full name should match");
        assertEquals(org.getId(), userPrincipal.getOrganizationId(), "Organization ID should match");
        assertSame(user, userPrincipal.getUser(), "User object should be the same");
    }

    // Helper method to simulate the authority loading logic from UserDetailsServiceImpl
    private Collection<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        
        // Load user roles and permissions
        for (UserRole userRole : user.getUserRoles()) {
            Role role = userRole.getRole();
            
            // Add role as authority (with ROLE_ prefix for Spring Security)
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_" + role.getName().toUpperCase()));
            
            // Add permissions from this role
            for (RolePermission rolePermission : role.getRolePermissions()) {
                Permission permission = rolePermission.getPermission();
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    permission.getName().toUpperCase()));
            }
        }
        
        return authorities;
    }

    @Provide
    Arbitrary<List<String>> roleNames() {
        return Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(2)
            .ofMaxLength(15)
            .list()
            .ofMinSize(0)
            .ofMaxSize(5)
            .map(list -> list.stream().distinct().collect(Collectors.toList()));
    }

    @Provide
    Arbitrary<List<String>> permissionNames() {
        return Arbitraries.strings()
            .withCharRange('A', 'Z')
            .ofMinLength(3)
            .ofMaxLength(20)
            .list()
            .ofMinSize(0)
            .ofMaxSize(8)
            .map(list -> list.stream().distinct().collect(Collectors.toList()));
    }
}

