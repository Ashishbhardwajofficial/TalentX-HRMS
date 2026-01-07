package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Find by name
    Optional<Role> findByName(String name);
    
    // Find by name and organization
    Optional<Role> findByNameAndOrganization(String name, Organization organization);
    
    // Find all roles by organization
    List<Role> findByOrganization(Organization organization);
    
    // Find system roles
    List<Role> findByIsSystemRoleTrue();
    
    // Find non-system roles by organization
    @Query("SELECT r FROM Role r WHERE r.organization = :organization AND r.isSystemRole = false")
    List<Role> findCustomRolesByOrganization(@Param("organization") Organization organization);
    
    // Check if role name exists in organization
    boolean existsByNameAndOrganization(String name, Organization organization);
    
    // Count roles by organization
    long countByOrganization(Organization organization);
    
    // Find roles by organization with pagination
    @Query("SELECT r FROM Role r WHERE r.organization = :organization")
    org.springframework.data.domain.Page<Role> findByOrganization(
        @Param("organization") Organization organization, 
        org.springframework.data.domain.Pageable pageable);
    
    // Search roles by name with pagination
    @Query("SELECT r FROM Role r WHERE r.organization = :organization AND LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    org.springframework.data.domain.Page<Role> findByOrganizationAndNameContainingIgnoreCase(
        @Param("organization") Organization organization,
        @Param("name") String name,
        org.springframework.data.domain.Pageable pageable);
}

