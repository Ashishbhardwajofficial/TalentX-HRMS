package com.talentx.hrms.repository;

import com.talentx.hrms.entity.security.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    // Find by name
    Optional<Permission> findByName(String name);
    
    // Find by resource
    List<Permission> findByResource(String resource);
    
    // Find by resource and action
    Optional<Permission> findByResourceAndAction(String resource, String action);
    
    // Find system permissions
    List<Permission> findByIsSystemPermissionTrue();
    
    // Find permissions by category (resource)
    @Query("SELECT p FROM Permission p WHERE p.resource = :category ORDER BY p.name")
    List<Permission> findByCategory(@Param("category") String category);
    
    // Get all distinct categories (resources)
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.resource IS NOT NULL ORDER BY p.resource")
    List<String> findAllCategories();
    
    // Check if permission name exists
    boolean existsByName(String name);
    
    // Count permissions
    long count();
}

