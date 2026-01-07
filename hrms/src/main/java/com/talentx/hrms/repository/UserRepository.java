package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

       // Find by username
       Optional<User> findByUsername(String username);

       // Find by email
       Optional<User> findByEmail(String email);

       // Find by username and organization
       Optional<User> findByUsernameAndOrganization(String username, Organization organization);

       // Find by email and organization
       Optional<User> findByEmailAndOrganization(String email, Organization organization);

       // Find all users by organization
       List<User> findByOrganization(Organization organization);

       // Find all users by organization with pagination
       Page<User> findByOrganization(Organization organization, Pageable pageable);

       // Check if username exists
       boolean existsByUsername(String username);

       // Check if email exists
       boolean existsByEmail(String email);

       // Check if username exists in organization
       boolean existsByUsernameAndOrganization(String username, Organization organization);

       // Check if email exists in organization
       boolean existsByEmailAndOrganization(String email, Organization organization);

       // Find users with email verified
       List<User> findByOrganizationAndIsVerifiedTrue(Organization organization);

       // Find locked users
       List<User> findByOrganizationAndAccountLockedTrue(Organization organization);

       // Find users with expired accounts
       List<User> findByOrganizationAndAccountExpiredTrue(Organization organization);

       // Find users with expired credentials
       List<User> findByOrganizationAndCredentialsExpiredTrue(Organization organization);

       // Find users who must change password
       List<User> findByOrganizationAndMustChangePasswordTrue(Organization organization);

       // Search users by name within organization
       @Query("SELECT u FROM User u WHERE u.organization = :organization AND " +
                     "(LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :name, '%')))")
       Page<User> findByOrganizationAndNameContainingIgnoreCase(@Param("organization") Organization organization,
                     @Param("name") String name,
                     Pageable pageable);

       // Find users with failed login attempts greater than threshold
       @Query("SELECT u FROM User u WHERE u.organization = :organization AND u.failedLoginAttempts >= :threshold")
       List<User> findByOrganizationAndFailedLoginAttemptsGreaterThanEqual(
                     @Param("organization") Organization organization,
                     @Param("threshold") Integer threshold);

       // Find users who haven't logged in since specified date
       @Query("SELECT u FROM User u WHERE u.organization = :organization AND " +
                     "(u.lastLoginAt IS NULL OR u.lastLoginAt < :since)")
       List<User> findByOrganizationAndLastLoginBefore(@Param("organization") Organization organization,
                     @Param("since") Instant since);

       // Find users with roles
       @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role " +
                     "WHERE u.id = :id")
       Optional<User> findByIdWithRoles(@Param("id") Long id);

       // Find users by role name
       @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
                     "WHERE u.organization = :organization AND r.name = :roleName")
       List<User> findByOrganizationAndRoleName(@Param("organization") Organization organization,
                     @Param("roleName") String roleName);

       // Count users by organization
       long countByOrganization(Organization organization);

       // Count active users by organization
       @Query("SELECT COUNT(u) FROM User u WHERE u.organization = :organization AND u.isActive = true")
       long countActiveByOrganization(@Param("organization") Organization organization);

       // Update failed login attempts
       @Modifying
       @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :id")
       void updateFailedLoginAttempts(@Param("id") Long id, @Param("attempts") Integer attempts);

       // Update last login time
       @Modifying
       @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.failedLoginAttempts = 0 WHERE u.id = :id")
       void updateLastLogin(@Param("id") Long id, @Param("loginTime") Instant loginTime);

       // Lock user account
       @Modifying
       @Query("UPDATE User u SET u.accountLocked = true WHERE u.id = :id")
       void lockAccount(@Param("id") Long id);

       // Unlock user account
       @Modifying
       @Query("UPDATE User u SET u.accountLocked = false, u.failedLoginAttempts = 0 WHERE u.id = :id")
       void unlockAccount(@Param("id") Long id);

       // Find active users
       @Query("SELECT u FROM User u WHERE u.organization = :organization AND u.isActive = true")
       List<User> findActiveByOrganization(@Param("organization") Organization organization);
}

