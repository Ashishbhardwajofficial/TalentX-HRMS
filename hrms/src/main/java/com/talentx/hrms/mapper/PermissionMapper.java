package com.talentx.hrms.mapper;

import com.talentx.hrms.dto.permission.PermissionResponse;
import com.talentx.hrms.entity.security.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setDescription(permission.getDescription());
        response.setResource(permission.getResource());
        response.setAction(permission.getAction());
        response.setIsSystemPermission(permission.getIsSystemPermission());
        response.setCreatedAt(permission.getCreatedAt() != null ? permission.getCreatedAt().toInstant() : null);
        response.setUpdatedAt(permission.getUpdatedAt() != null ? permission.getUpdatedAt().toInstant() : null);

        return response;
    }
}

