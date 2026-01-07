package com.talentx.hrms.controller.compliance;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.dto.compliance.*;
import com.talentx.hrms.entity.compliance.ComplianceCheck;
import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.mapper.ComplianceMapper;
import com.talentx.hrms.repository.ComplianceJurisdictionRepository;
import com.talentx.hrms.repository.OrganizationRepository;
import com.talentx.hrms.service.compliance.ComplianceService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compliance")
@Tag(name = "Compliance Management", description = "Compliance rules, jurisdictions, and checks management")
public class ComplianceController {

    private final ComplianceService complianceService;
    private final ComplianceMapper complianceMapper;
    private final ComplianceJurisdictionRepository jurisdictionRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public ComplianceController(ComplianceService complianceService,
                              ComplianceMapper complianceMapper,
                              ComplianceJurisdictionRepository jurisdictionRepository,
                              OrganizationRepository organizationRepository) {
        this.complianceService = complianceService;
        this.complianceMapper = complianceMapper;
        this.jurisdictionRepository = jurisdictionRepository;
        this.organizationRepository = organizationRepository;
    }

    // ===== JURISDICTION ENDPOINTS =====

    /**
     * Create a new compliance jurisdiction
     */
    @PostMapping("/jurisdictions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Create jurisdiction", description = "Create a new compliance jurisdiction")
    public ResponseEntity<ApiResponse<ComplianceJurisdictionResponse>> createJurisdiction(
            @Valid @RequestBody ComplianceJurisdictionRequest request) {
        
        ComplianceJurisdiction jurisdiction = complianceMapper.toEntity(request);
        ComplianceJurisdiction savedJurisdiction = complianceService.createJurisdiction(jurisdiction);
        ComplianceJurisdictionResponse response = complianceMapper.toResponse(savedJurisdiction);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Jurisdiction created successfully", response));
    }

    /**
     * Get all compliance jurisdictions
     */
    @GetMapping("/jurisdictions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "List jurisdictions", description = "Get all compliance jurisdictions")
    public ResponseEntity<ApiResponse<List<ComplianceJurisdictionResponse>>> getJurisdictions() {
        
        List<ComplianceJurisdiction> jurisdictions = complianceService.getAllJurisdictions();
        List<ComplianceJurisdictionResponse> responses = jurisdictions.stream()
                .map(complianceMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Jurisdictions retrieved successfully", responses));
    }

    /**
     * Get jurisdiction by ID
     */
    @GetMapping("/jurisdictions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get jurisdiction", description = "Get compliance jurisdiction by ID")
    public ResponseEntity<ApiResponse<ComplianceJurisdictionResponse>> getJurisdiction(
            @Parameter(description = "Jurisdiction ID") @PathVariable Long id) {
        
        ComplianceJurisdiction jurisdiction = complianceService.getJurisdiction(id);
        ComplianceJurisdictionResponse response = complianceMapper.toResponse(jurisdiction);
        
        return ResponseEntity.ok(ApiResponse.success("Jurisdiction retrieved successfully", response));
    }

    /**
     * Update jurisdiction
     */
    @PutMapping("/jurisdictions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Update jurisdiction", description = "Update compliance jurisdiction")
    public ResponseEntity<ApiResponse<ComplianceJurisdictionResponse>> updateJurisdiction(
            @Parameter(description = "Jurisdiction ID") @PathVariable Long id,
            @Valid @RequestBody ComplianceJurisdictionRequest request) {
        
        ComplianceJurisdiction existingJurisdiction = complianceService.getJurisdiction(id);
        complianceMapper.updateEntity(existingJurisdiction, request);
        ComplianceJurisdiction updatedJurisdiction = jurisdictionRepository.save(existingJurisdiction);
        ComplianceJurisdictionResponse response = complianceMapper.toResponse(updatedJurisdiction);
        
        return ResponseEntity.ok(ApiResponse.success("Jurisdiction updated successfully", response));
    }

    // ===== RULE ENDPOINTS =====

    /**
     * Create a new compliance rule
     */
    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Create rule", description = "Create a new compliance rule")
    public ResponseEntity<ApiResponse<ComplianceRuleResponse>> createRule(
            @Valid @RequestBody ComplianceRuleRequest request) {
        
        ComplianceJurisdiction jurisdiction = complianceService.getJurisdiction(request.getJurisdictionId());
        
        Organization organization = null;
        if (request.getOrganizationId() != null) {
            organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found with id: " + request.getOrganizationId()));
        }
        
        ComplianceRule rule = complianceMapper.toEntity(request, jurisdiction, organization);
        ComplianceRule savedRule = complianceService.createComplianceRule(rule);
        ComplianceRuleResponse response = complianceMapper.toResponse(savedRule);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compliance rule created successfully", response));
    }

    /**
     * Get all compliance rules
     */
    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "List rules", description = "Get all compliance rules")
    public ResponseEntity<ApiResponse<List<ComplianceRuleResponse>>> getRules(
            @Parameter(description = "Organization ID filter") @RequestParam(required = false) Long organizationId,
            @Parameter(description = "Jurisdiction ID filter") @RequestParam(required = false) Long jurisdictionId,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Severity filter") @RequestParam(required = false) String severity,
            @Parameter(description = "Active rules only") @RequestParam(defaultValue = "false") boolean activeOnly) {
        
        List<ComplianceRule> rules;
        
        if (organizationId != null) {
            if (activeOnly) {
                rules = complianceService.getActiveComplianceRulesByOrganization(organizationId);
            } else {
                rules = complianceService.getComplianceRulesByOrganization(organizationId);
            }
        } else if (jurisdictionId != null) {
            rules = complianceService.getComplianceRulesByJurisdiction(jurisdictionId);
        } else {
            // For now, return empty list if no filters provided
            // In a real implementation, you might want to add a service method to get all rules
            rules = List.of();
        }
        
        // Apply additional filters
        if (category != null) {
            rules = rules.stream()
                    .filter(rule -> category.equalsIgnoreCase(rule.getCategory()))
                    .collect(Collectors.toList());
        }
        
        if (severity != null) {
            rules = rules.stream()
                    .filter(rule -> severity.equalsIgnoreCase(rule.getSeverity()))
                    .collect(Collectors.toList());
        }
        
        List<ComplianceRuleResponse> responses = rules.stream()
                .map(complianceMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Compliance rules retrieved successfully", responses));
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get rule", description = "Get compliance rule by ID")
    public ResponseEntity<ApiResponse<ComplianceRuleResponse>> getRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        
        ComplianceRule rule = complianceService.getComplianceRule(id);
        ComplianceRuleResponse response = complianceMapper.toResponse(rule);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance rule retrieved successfully", response));
    }

    /**
     * Update rule
     */
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Update rule", description = "Update compliance rule")
    public ResponseEntity<ApiResponse<ComplianceRuleResponse>> updateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody ComplianceRuleRequest request) {
        
        ComplianceJurisdiction jurisdiction = complianceService.getJurisdiction(request.getJurisdictionId());
        
        Organization organization = null;
        if (request.getOrganizationId() != null) {
            organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new RuntimeException("Organization not found with id: " + request.getOrganizationId()));
        }
        
        ComplianceRule rule = complianceMapper.toEntity(request, jurisdiction, organization);
        ComplianceRule updatedRule = complianceService.updateComplianceRule(id, rule);
        ComplianceRuleResponse response = complianceMapper.toResponse(updatedRule);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance rule updated successfully", response));
    }

    /**
     * Delete rule
     */
    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Delete rule", description = "Delete compliance rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        
        complianceService.deleteComplianceRule(id);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance rule deleted successfully", null));
    }

    // ===== COMPLIANCE CHECK ENDPOINTS =====

    /**
     * Get compliance checks
     */
    @GetMapping("/checks")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get compliance checks", description = "Get compliance checks with pagination")
    public ResponseEntity<ApiResponse<Page<ComplianceCheckResponse>>> getComplianceChecks(
            @Parameter(description = "Organization ID") @RequestParam Long organizationId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<ComplianceCheck> checks = complianceService.getComplianceChecksByOrganization(
                organizationId, page, size);
        
        Page<ComplianceCheckResponse> responses = checks.map(complianceMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance checks retrieved successfully", responses));
    }

    /**
     * Get compliance check by ID
     */
    @GetMapping("/checks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get compliance check", description = "Get compliance check by ID")
    public ResponseEntity<ApiResponse<ComplianceCheckResponse>> getComplianceCheck(
            @Parameter(description = "Check ID") @PathVariable Long id) {
        
        ComplianceCheck check = complianceService.getComplianceCheck(id);
        ComplianceCheckResponse response = complianceMapper.toResponse(check);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance check retrieved successfully", response));
    }

    /**
     * Run compliance check
     */
    @PostMapping("/checks/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Run compliance check", description = "Run compliance checks for an organization")
    public ResponseEntity<ApiResponse<List<ComplianceCheckResponse>>> runComplianceCheck(
            @Valid @RequestBody ComplianceCheckRunRequest request) {
        
        List<ComplianceCheck> checks;
        
        if (request.getRuleId() != null) {
            // Run check for specific rule
            ComplianceCheck check;
            if (request.getEmployeeId() != null) {
                check = complianceService.performComplianceCheck(
                        request.getRuleId(), 
                        request.getOrganizationId(), 
                        request.getEmployeeId(), 
                        request.getCheckedBy());
            } else {
                check = complianceService.performComplianceCheck(
                        request.getRuleId(), 
                        request.getOrganizationId(), 
                        request.getCheckedBy());
            }
            checks = List.of(check);
        } else {
            // Run checks for all active rules in organization
            checks = complianceService.runComplianceChecks(
                    request.getOrganizationId(), 
                    request.getCheckedBy());
        }
        
        List<ComplianceCheckResponse> responses = checks.stream()
                .map(complianceMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Compliance checks executed successfully", responses));
    }

    /**
     * Resolve compliance check violation
     */
    @PutMapping("/checks/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Resolve violation", description = "Resolve compliance check violation")
    public ResponseEntity<ApiResponse<ComplianceCheckResponse>> resolveViolation(
            @Parameter(description = "Check ID") @PathVariable Long id,
            @Valid @RequestBody ComplianceCheckResolveRequest request) {
        
        ComplianceCheck resolvedCheck = complianceService.resolveComplianceCheck(
                id, 
                Long.valueOf(request.getResolvedBy()), 
                request.getResolutionNotes());
        
        ComplianceCheckResponse response = complianceMapper.toResponse(resolvedCheck);
        
        return ResponseEntity.ok(ApiResponse.success("Compliance violation resolved successfully", response));
    }

    /**
     * Get overdue compliance checks
     */
    @GetMapping("/checks/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get overdue checks", description = "Get overdue compliance checks")
    public ResponseEntity<ApiResponse<List<ComplianceCheckResponse>>> getOverdueChecks(
            @Parameter(description = "Organization ID") @RequestParam Long organizationId) {
        
        List<ComplianceCheck> overdueChecks = complianceService.getOverdueComplianceChecks(organizationId);
        List<ComplianceCheckResponse> responses = overdueChecks.stream()
                .map(complianceMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Overdue compliance checks retrieved successfully", responses));
    }

    /**
     * Get violation alerts
     */
    @GetMapping("/checks/violations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'HR_MANAGER')")
    @Operation(summary = "Get violation alerts", description = "Get compliance violation alerts")
    public ResponseEntity<ApiResponse<List<ComplianceCheckResponse>>> getViolationAlerts(
            @Parameter(description = "Organization ID") @RequestParam Long organizationId) {
        
        List<ComplianceCheck> violationAlerts = complianceService.getViolationAlerts(organizationId);
        List<ComplianceCheckResponse> responses = violationAlerts.stream()
                .map(complianceMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Violation alerts retrieved successfully", responses));
    }
}

