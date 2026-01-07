package com.talentx.hrms.service.permission;

import com.talentx.hrms.entity.security.Permission;
import com.talentx.hrms.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Get all permissions
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Get permission by ID
     */
    public Permission getPermission(Long id) {
        return permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found"));
    }

    /**
     * Get permission by name
     */
    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    /**
     * Get permissions by resource
     */
    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    /**
     * Get system permissions
     */
    public List<Permission> getSystemPermissions() {
        return permissionRepository.findByIsSystemPermissionTrue();
    }

    /**
     * Get permissions grouped by category (resource)
     */
    public Map<String, List<Permission>> getPermissionsByCategory() {
        List<Permission> allPermissions = permissionRepository.findAll();
        
        // Group permissions by resource (category)
        Map<String, List<Permission>> groupedPermissions = allPermissions.stream()
            .collect(Collectors.groupingBy(
                permission -> permission.getResource() != null ? permission.getResource() : "Other",
                TreeMap::new,
                Collectors.toList()
            ));
        
        // Sort permissions within each category by name
        groupedPermissions.values().forEach(permissions -> 
            permissions.sort(Comparator.comparing(Permission::getName))
        );
        
        return groupedPermissions;
    }

    /**
     * Get all permission categories
     */
    public List<String> getAllCategories() {
        List<String> categories = permissionRepository.findAllCategories();
        
        // Add "Other" category if there are permissions without a resource
        long permissionsWithoutResource = permissionRepository.findAll().stream()
            .filter(p -> p.getResource() == null)
            .count();
        
        if (permissionsWithoutResource > 0) {
            categories.add("Other");
        }
        
        return categories;
    }

    /**
     * Get permissions by specific category
     */
    public List<Permission> getPermissionsBySpecificCategory(String category) {
        if ("Other".equalsIgnoreCase(category)) {
            // Return permissions without a resource
            return permissionRepository.findAll().stream()
                .filter(p -> p.getResource() == null)
                .sorted(Comparator.comparing(Permission::getName))
                .collect(Collectors.toList());
        }
        
        return permissionRepository.findByCategory(category);
    }

    /**
     * Search permissions by name
     */
    public List<Permission> searchPermissions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllPermissions();
        }
        
        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        return permissionRepository.findAll().stream()
            .filter(p -> p.getName().toLowerCase().contains(lowerSearchTerm) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerSearchTerm)) ||
                        (p.getResource() != null && p.getResource().toLowerCase().contains(lowerSearchTerm)))
            .sorted(Comparator.comparing(Permission::getName))
            .collect(Collectors.toList());
    }

    /**
     * Get permission statistics
     */
    public PermissionStatistics getPermissionStatistics() {
        long totalPermissions = permissionRepository.count();
        long systemPermissions = permissionRepository.findByIsSystemPermissionTrue().size();
        long categoriesCount = permissionRepository.findAllCategories().size();

        return new PermissionStatistics(totalPermissions, systemPermissions, categoriesCount);
    }

    /**
     * Check if permission exists by name
     */
    public boolean permissionExists(String name) {
        return permissionRepository.existsByName(name);
    }

    /**
     * Permission statistics inner class
     */
    public static class PermissionStatistics {
        private final long totalPermissions;
        private final long systemPermissions;
        private final long categoriesCount;

        public PermissionStatistics(long totalPermissions, long systemPermissions, long categoriesCount) {
            this.totalPermissions = totalPermissions;
            this.systemPermissions = systemPermissions;
            this.categoriesCount = categoriesCount;
        }

        // Getters
        public long getTotalPermissions() { return totalPermissions; }
        public long getSystemPermissions() { return systemPermissions; }
        public long getCategoriesCount() { return categoriesCount; }
    }
}

