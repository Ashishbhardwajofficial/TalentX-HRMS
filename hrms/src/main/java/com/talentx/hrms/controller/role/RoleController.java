package com.talentx.hrms.controller.role;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.dto.role.PermissionAssignmentRequest;
import com.talentx.hrms.dto.role.RoleRequest;
import com.talentx.hrms.dto.role.RoleResponse;
import com.talentx.hrms.dto.role.RoleUpdateRequest;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.mapper.RoleMapper;
import com.talentx.hrms.service.role.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "Role CRUD operations and permission management")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @Autowired
    public RoleController(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    /**
     * Get all roles with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get all roles", description = "Retrieve all roles with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAllRoles(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Search by name") @RequestParam(required = false) String name) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        
        Page<Role> roles;
        if (name != null && !name.trim().isEmpty()) {
            roles = roleService.searchRoles(name, paginationRequest);
        } else {
            roles = roleService.getRoles(paginationRequest);
        }
        
        Page<RoleResponse> roleResponses = roles.map(roleMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roleResponses));
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable Long id) {
        try {
            Role role = roleService.getRole(id);
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create new role
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            Role role = roleService.createRole(
                request.getName(),
                request.getDescription(),
                request.getOrganizationId()
            );
            
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update role", description = "Update an existing role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id, 
            @Valid @RequestBody RoleUpdateRequest request) {
        try {
            Role role = roleService.updateRole(
                id,
                request.getName(),
                request.getDescription()
            );
            
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete role", description = "Delete a role (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Assign permission to role
     */
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Assign permission to role", description = "Assign a permission to a role")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionAssignmentRequest request) {
        try {
            roleService.assignPermission(id, request.getPermissionId());
            Role role = roleService.getRole(id);
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.ok(ApiResponse.success("Permission assigned successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Remove permission from role
     */
    @DeleteMapping("/{id}/permissions/{permId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermission(
            @PathVariable Long id,
            @PathVariable Long permId) {
        try {
            roleService.removePermission(id, permId);
            Role role = roleService.getRole(id);
            RoleResponse response = roleMapper.toResponse(role);
            return ResponseEntity.ok(ApiResponse.success("Permission removed successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all roles (non-paginated)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get all roles", description = "Retrieve all roles without pagination")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRolesNonPaginated() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleResponse> responses = roles.stream()
            .map(roleMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", responses));
    }

    /**
     * Get custom roles
     */
    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get custom roles", description = "Retrieve custom (non-system) roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getCustomRoles() {
        List<Role> roles = roleService.getCustomRoles();
        List<RoleResponse> responses = roles.stream()
            .map(roleMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Custom roles retrieved successfully", responses));
    }

    /**
     * Get system roles
     */
    @GetMapping("/system")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get system roles", description = "Retrieve system roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getSystemRoles() {
        List<Role> roles = roleService.getSystemRoles();
        List<RoleResponse> responses = roles.stream()
            .map(roleMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("System roles retrieved successfully", responses));
    }

    /**
     * Get role statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Get role statistics", description = "Get comprehensive role statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoleStatistics() {
        RoleService.RoleStatistics stats = roleService.getRoleStatistics();
        
        Map<String, Object> statisticsMap = Map.of(
            "totalRoles", stats.getTotalRoles(),
            "systemRoles", stats.getSystemRoles(),
            "customRoles", stats.getCustomRoles()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Role statistics retrieved successfully", statisticsMap));
    }
}

