package com.talentx.hrms.mapper;

import com.talentx.hrms.dto.compliance.*;
import com.talentx.hrms.entity.compliance.ComplianceCheck;
import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ComplianceMapper {

    // Jurisdiction mappings
    public ComplianceJurisdiction toEntity(ComplianceJurisdictionRequest request) {
        if (request == null) {
            return null;
        }

        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setName(request.getName());
        jurisdiction.setCode(request.getCode());
        jurisdiction.setDescription(request.getDescription());
        jurisdiction.setCountry(request.getCountry());
        jurisdiction.setStateProvince(request.getStateProvince());
        jurisdiction.setCity(request.getCity());
        // Convert string to entity's inner enum
        if (request.getJurisdictionType() != null) {
            jurisdiction.setJurisdictionType(ComplianceJurisdiction.JurisdictionType.valueOf(request.getJurisdictionType()));
        }
        jurisdiction.setIsDefault(request.getIsDefault());
        jurisdiction.setRegulatoryBody(request.getRegulatoryBody());
        jurisdiction.setContactInformation(request.getContactInformation());
        jurisdiction.setWebsite(request.getWebsite());

        return jurisdiction;
    }

    public ComplianceJurisdictionResponse toResponse(ComplianceJurisdiction jurisdiction) {
        if (jurisdiction == null) {
            return null;
        }

        ComplianceJurisdictionResponse response = new ComplianceJurisdictionResponse();
        response.setId(jurisdiction.getId());
        response.setName(jurisdiction.getName());
        response.setCode(jurisdiction.getCode());
        response.setDescription(jurisdiction.getDescription());
        response.setCountry(jurisdiction.getCountry());
        response.setStateProvince(jurisdiction.getStateProvince());
        response.setCity(jurisdiction.getCity());
        response.setJurisdictionType(jurisdiction.getJurisdictionType() != null ? jurisdiction.getJurisdictionType().name() : null);
        response.setIsDefault(jurisdiction.getIsDefault());
        response.setRegulatoryBody(jurisdiction.getRegulatoryBody());
        response.setContactInformation(jurisdiction.getContactInformation());
        response.setWebsite(jurisdiction.getWebsite());

        if (jurisdiction.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(jurisdiction.getCreatedAt(), ZoneId.systemDefault()));
        }
        if (jurisdiction.getUpdatedAt() != null) {
            response.setUpdatedAt(LocalDateTime.ofInstant(jurisdiction.getUpdatedAt(), ZoneId.systemDefault()));
        }

        return response;
    }

    // Rule mappings
    public ComplianceRule toEntity(ComplianceRuleRequest request, ComplianceJurisdiction jurisdiction, Organization organization) {
        if (request == null) {
            return null;
        }

        ComplianceRule rule = new ComplianceRule();
        rule.setName(request.getName());
        rule.setRuleCode(request.getRuleCode());
        rule.setDescription(request.getDescription());
        rule.setCategory(request.getCategory());
        rule.setRuleType(request.getRuleType());
        rule.setSeverity(request.getSeverity());
        rule.setJurisdiction(jurisdiction);
        rule.setOrganization(organization);
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setExpirationDate(request.getExpirationDate());
        rule.setRuleText(request.getRuleText());
        rule.setComplianceCriteria(request.getComplianceCriteria());
        rule.setViolationConsequences(request.getViolationConsequences());
        rule.setRemediationSteps(request.getRemediationSteps());
        rule.setCheckFrequencyDays(request.getCheckFrequencyDays());
        rule.setAutoCheckEnabled(request.getAutoCheckEnabled());
        rule.setCheckQuery(request.getCheckQuery());
        rule.setReferenceUrl(request.getReferenceUrl());
        rule.setLegalReference(request.getLegalReference());
        rule.setNotes(request.getNotes());

        return rule;
    }

    public ComplianceRuleResponse toResponse(ComplianceRule rule) {
        if (rule == null) {
            return null;
        }

        ComplianceRuleResponse response = new ComplianceRuleResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setRuleCode(rule.getRuleCode());
        response.setDescription(rule.getDescription());
        response.setCategory(rule.getCategory());
        response.setRuleType(rule.getRuleType());
        response.setSeverity(rule.getSeverity());
        response.setJurisdiction(toResponse(rule.getJurisdiction()));
        
        if (rule.getOrganization() != null) {
            response.setOrganizationId(rule.getOrganization().getId());
            response.setOrganizationName(rule.getOrganization().getName());
        }
        
        response.setEffectiveDate(rule.getEffectiveDate());
        response.setExpirationDate(rule.getExpirationDate());
        response.setRuleText(rule.getRuleText());
        response.setComplianceCriteria(rule.getComplianceCriteria());
        response.setViolationConsequences(rule.getViolationConsequences());
        response.setRemediationSteps(rule.getRemediationSteps());
        response.setCheckFrequencyDays(rule.getCheckFrequencyDays());
        response.setAutoCheckEnabled(rule.getAutoCheckEnabled());
        response.setCheckQuery(rule.getCheckQuery());
        response.setReferenceUrl(rule.getReferenceUrl());
        response.setLegalReference(rule.getLegalReference());
        response.setNotes(rule.getNotes());
        response.setIsSystemRule(rule.getIsSystemRule());
        response.setIsActive(rule.isActive());

        if (rule.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(rule.getCreatedAt(), ZoneId.systemDefault()));
        }
        if (rule.getUpdatedAt() != null) {
            response.setUpdatedAt(LocalDateTime.ofInstant(rule.getUpdatedAt(), ZoneId.systemDefault()));
        }

        return response;
    }

    // Check mappings
    public ComplianceCheckResponse toResponse(ComplianceCheck check) {
        if (check == null) {
            return null;
        }

        ComplianceCheckResponse response = new ComplianceCheckResponse();
        response.setId(check.getId());
        response.setComplianceRule(toResponse(check.getComplianceRule()));
        
        if (check.getOrganization() != null) {
            response.setOrganizationId(check.getOrganization().getId());
            response.setOrganizationName(check.getOrganization().getName());
        }
        
        if (check.getEmployee() != null) {
            response.setEmployeeId(check.getEmployee().getId());
            response.setEmployeeName(check.getEmployee().getFirstName() + " " + check.getEmployee().getLastName());
        }
        
        response.setCheckDate(check.getCheckDate());
        response.setCheckedAt(check.getCheckedAt() != null ? check.getCheckedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setCheckedBy(check.getCheckedBy());
        response.setCheckType(check.getCheckType());
        response.setStatus(check.getStatus());
        response.setComplianceScore(check.getComplianceScore());
        response.setCheckResults(check.getCheckResults());
        response.setFindings(check.getFindings());
        response.setViolations(check.getViolations());
        response.setRecommendations(check.getRecommendations());
        response.setRemediationActions(check.getRemediationActions());
        response.setRemediationDueDate(check.getRemediationDueDate());
        response.setRemediationCompletedAt(check.getRemediationCompletedAt() != null ? check.getRemediationCompletedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setRemediatedBy(check.getRemediatedBy());
        response.setIsResolved(check.getIsResolved());
        response.setResolvedAt(check.getResolvedAt() != null ? check.getResolvedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setResolvedBy(check.getResolvedBy() != null ? check.getResolvedBy().getFullName() : null);
        response.setResolutionNotes(check.getResolutionNotes());
        response.setNextCheckDate(check.getNextCheckDate());
        response.setNotes(check.getNotes());
        response.setAlertSent(check.getAlertSent());
        response.setAlertSentAt(check.getAlertSentAt() != null ? check.getAlertSentAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        response.setEvidencePath(check.getEvidencePath());
        response.setIsOverdue(check.isOverdue());

        if (check.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(check.getCreatedAt(), java.time.ZoneId.systemDefault()));
        }
        if (check.getUpdatedAt() != null) {
            response.setUpdatedAt(LocalDateTime.ofInstant(check.getUpdatedAt(), java.time.ZoneId.systemDefault()));
        }

        return response;
    }

    public void updateEntity(ComplianceJurisdiction jurisdiction, ComplianceJurisdictionRequest request) {
        if (jurisdiction == null || request == null) {
            return;
        }

        jurisdiction.setName(request.getName());
        jurisdiction.setCode(request.getCode());
        jurisdiction.setDescription(request.getDescription());
        jurisdiction.setCountry(request.getCountry());
        jurisdiction.setStateProvince(request.getStateProvince());
        jurisdiction.setCity(request.getCity());
        // Convert string to entity's inner enum
        if (request.getJurisdictionType() != null) {
            jurisdiction.setJurisdictionType(ComplianceJurisdiction.JurisdictionType.valueOf(request.getJurisdictionType()));
        }
        jurisdiction.setIsDefault(request.getIsDefault());
        jurisdiction.setRegulatoryBody(request.getRegulatoryBody());
        jurisdiction.setContactInformation(request.getContactInformation());
        jurisdiction.setWebsite(request.getWebsite());
    }

    public void updateEntity(ComplianceRule rule, ComplianceRuleRequest request, ComplianceJurisdiction jurisdiction, Organization organization) {
        if (rule == null || request == null) {
            return;
        }

        rule.setName(request.getName());
        rule.setRuleCode(request.getRuleCode());
        rule.setDescription(request.getDescription());
        rule.setCategory(request.getCategory());
        rule.setRuleType(request.getRuleType());
        rule.setSeverity(request.getSeverity());
        rule.setJurisdiction(jurisdiction);
        rule.setOrganization(organization);
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setExpirationDate(request.getExpirationDate());
        rule.setRuleText(request.getRuleText());
        rule.setComplianceCriteria(request.getComplianceCriteria());
        rule.setViolationConsequences(request.getViolationConsequences());
        rule.setRemediationSteps(request.getRemediationSteps());
        rule.setCheckFrequencyDays(request.getCheckFrequencyDays());
        rule.setAutoCheckEnabled(request.getAutoCheckEnabled());
        rule.setCheckQuery(request.getCheckQuery());
        rule.setReferenceUrl(request.getReferenceUrl());
        rule.setLegalReference(request.getLegalReference());
        rule.setNotes(request.getNotes());
    }
}

