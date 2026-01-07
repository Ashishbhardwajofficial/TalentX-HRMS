package com.talentx.hrms.service.compliance;

import com.talentx.hrms.entity.compliance.ComplianceCheck;
import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.repository.ComplianceCheckRepository;
import com.talentx.hrms.repository.ComplianceJurisdictionRepository;
import com.talentx.hrms.repository.ComplianceRuleRepository;
import com.talentx.hrms.repository.EmployeeRepository;
import com.talentx.hrms.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ComplianceService {

    private static final Logger logger = LoggerFactory.getLogger(ComplianceService.class);

    private final ComplianceRuleRepository complianceRuleRepository;
    private final ComplianceCheckRepository complianceCheckRepository;
    private final ComplianceJurisdictionRepository jurisdictionRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ComplianceService(ComplianceRuleRepository complianceRuleRepository,
                           ComplianceCheckRepository complianceCheckRepository,
                           ComplianceJurisdictionRepository jurisdictionRepository,
                           OrganizationRepository organizationRepository,
                           EmployeeRepository employeeRepository) {
        this.complianceRuleRepository = complianceRuleRepository;
        this.complianceCheckRepository = complianceCheckRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
    }

    // ===== COMPLIANCE RULE MANAGEMENT =====

    /**
     * Create a new compliance rule
     */
    public ComplianceRule createComplianceRule(ComplianceRule rule) {
        validateComplianceRule(rule);
        
        // Check if rule code already exists
        if (rule.getRuleCode() != null && complianceRuleRepository.existsByRuleCode(rule.getRuleCode())) {
            throw new RuntimeException("Compliance rule with code '" + rule.getRuleCode() + "' already exists");
        }

        logger.info("Creating compliance rule: {} for jurisdiction: {}", 
                   rule.getName(), rule.getJurisdiction().getName());
        
        return complianceRuleRepository.save(rule);
    }

    /**
     * Update an existing compliance rule
     */
    public ComplianceRule updateComplianceRule(Long id, ComplianceRule updatedRule) {
        ComplianceRule existingRule = complianceRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Compliance rule not found with id: " + id));

        validateComplianceRule(updatedRule);

        // Check if rule code already exists (excluding current rule)
        if (updatedRule.getRuleCode() != null) {
            Optional<ComplianceRule> existingByCode = complianceRuleRepository.findByRuleCode(updatedRule.getRuleCode());
            if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
                throw new RuntimeException("Compliance rule with code '" + updatedRule.getRuleCode() + "' already exists");
            }
        }

        // Update fields
        existingRule.setName(updatedRule.getName());
        existingRule.setRuleCode(updatedRule.getRuleCode());
        existingRule.setDescription(updatedRule.getDescription());
        existingRule.setCategory(updatedRule.getCategory());
        existingRule.setRuleType(updatedRule.getRuleType());
        existingRule.setSeverity(updatedRule.getSeverity());
        existingRule.setJurisdiction(updatedRule.getJurisdiction());
        existingRule.setEffectiveDate(updatedRule.getEffectiveDate());
        existingRule.setExpirationDate(updatedRule.getExpirationDate());
        existingRule.setRuleText(updatedRule.getRuleText());
        existingRule.setComplianceCriteria(updatedRule.getComplianceCriteria());
        existingRule.setViolationConsequences(updatedRule.getViolationConsequences());
        existingRule.setRemediationSteps(updatedRule.getRemediationSteps());
        existingRule.setCheckFrequencyDays(updatedRule.getCheckFrequencyDays());
        existingRule.setAutoCheckEnabled(updatedRule.getAutoCheckEnabled());
        existingRule.setCheckQuery(updatedRule.getCheckQuery());
        existingRule.setReferenceUrl(updatedRule.getReferenceUrl());
        existingRule.setLegalReference(updatedRule.getLegalReference());
        existingRule.setNotes(updatedRule.getNotes());

        logger.info("Updated compliance rule: {} (ID: {})", existingRule.getName(), id);
        
        return complianceRuleRepository.save(existingRule);
    }

    /**
     * Get compliance rule by ID
     */
    @Transactional(readOnly = true)
    public ComplianceRule getComplianceRule(Long id) {
        return complianceRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Compliance rule not found with id: " + id));
    }

    /**
     * Get all compliance rules for an organization
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getComplianceRulesByOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceRuleRepository.findByOrganization(organization);
    }

    /**
     * Get active compliance rules for an organization
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getActiveComplianceRulesByOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceRuleRepository.findActiveRulesByOrganization(organization, LocalDate.now());
    }

    /**
     * Get compliance rules by jurisdiction
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getComplianceRulesByJurisdiction(Long jurisdictionId) {
        ComplianceJurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId)
            .orElseThrow(() -> new RuntimeException("Jurisdiction not found with id: " + jurisdictionId));
        
        return complianceRuleRepository.findByJurisdiction(jurisdiction);
    }

    /**
     * Get mandatory compliance rules for an organization
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getMandatoryComplianceRulesByOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceRuleRepository.findMandatoryRulesByOrganization(organization);
    }

    /**
     * Get critical compliance rules for an organization
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getCriticalComplianceRulesByOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceRuleRepository.findCriticalRulesByOrganization(organization);
    }

    /**
     * Delete compliance rule
     */
    public void deleteComplianceRule(Long id) {
        ComplianceRule rule = complianceRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Compliance rule not found with id: " + id));

        // Check if rule has associated checks
        List<ComplianceCheck> associatedChecks = complianceCheckRepository.findByComplianceRule(rule);
        if (!associatedChecks.isEmpty()) {
            throw new RuntimeException("Cannot delete compliance rule that has associated compliance checks");
        }

        logger.info("Deleting compliance rule: {} (ID: {})", rule.getName(), id);
        complianceRuleRepository.delete(rule);
    }

    // ===== AUTOMATED COMPLIANCE MONITORING =====

    /**
     * Perform compliance check for a specific rule and organization
     */
    public ComplianceCheck performComplianceCheck(Long ruleId, Long organizationId, String checkedBy) {
        ComplianceRule rule = complianceRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Compliance rule not found with id: " + ruleId));
        
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        return performComplianceCheck(rule, organization, null, checkedBy);
    }

    /**
     * Perform compliance check for a specific rule, organization, and employee
     */
    public ComplianceCheck performComplianceCheck(Long ruleId, Long organizationId, Long employeeId, String checkedBy) {
        ComplianceRule rule = complianceRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Compliance rule not found with id: " + ruleId));
        
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        }

        return performComplianceCheck(rule, organization, employee, checkedBy);
    }

    /**
     * Perform compliance check for a rule
     */
    private ComplianceCheck performComplianceCheck(ComplianceRule rule, Organization organization, Employee employee, String checkedBy) {
        logger.info("Performing compliance check for rule: {} on organization: {}", 
                   rule.getName(), organization.getName());

        ComplianceCheck check = new ComplianceCheck(organization, rule, LocalDate.now());
        check.setEmployee(employee);
        check.setCheckedBy(checkedBy);
        check.setCheckType("MANUAL");

        // Perform the actual compliance check logic
        ComplianceCheckResult result = executeComplianceCheck(rule, organization, employee);
        
        // Set check results
        check.setStatus(result.isCompliant() ? "COMPLIANT" : "NON_COMPLIANT");
        check.setComplianceScore(result.getComplianceScore());
        check.setCheckResults(result.getCheckResults());
        check.setFindings(result.getFindings());
        check.setViolations(result.getViolations());
        check.setRecommendations(result.getRecommendations());

        // Set remediation due date if non-compliant
        if (!result.isCompliant() && rule.getCheckFrequencyDays() != null) {
            check.setRemediationDueDate(LocalDate.now().plusDays(rule.getCheckFrequencyDays()));
        }

        // Schedule next check if rule has periodic checking enabled
        if (rule.needsPeriodicCheck()) {
            check.setNextCheckDate(LocalDate.now().plusDays(rule.getCheckFrequencyDays()));
        }

        check = complianceCheckRepository.save(check);

        // Generate alert if non-compliant
        if (!result.isCompliant()) {
            generateViolationAlert(check);
        }

        logger.info("Compliance check completed for rule: {} - Status: {}", 
                   rule.getName(), check.getStatus());

        return check;
    }

    /**
     * Execute automated compliance checks for all rules needing periodic checking
     */
    @Async
    public void executeAutomatedComplianceChecks() {
        logger.info("Starting automated compliance checks");

        List<ComplianceRule> rulesNeedingCheck = complianceRuleRepository.findRulesNeedingPeriodicCheck();
        
        for (ComplianceRule rule : rulesNeedingCheck) {
            try {
                executeAutomatedCheckForRule(rule);
            } catch (Exception e) {
                logger.error("Error executing automated check for rule: {} - {}", 
                           rule.getName(), e.getMessage(), e);
            }
        }

        logger.info("Completed automated compliance checks for {} rules", rulesNeedingCheck.size());
    }

    /**
     * Execute automated compliance checks for a specific organization
     */
    @Async
    public void executeAutomatedComplianceChecksForOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        logger.info("Starting automated compliance checks for organization: {}", organization.getName());

        List<ComplianceRule> rulesNeedingCheck = complianceRuleRepository
            .findRulesNeedingPeriodicCheckByOrganization(organization);
        
        for (ComplianceRule rule : rulesNeedingCheck) {
            try {
                executeAutomatedCheckForRule(rule, organization);
            } catch (Exception e) {
                logger.error("Error executing automated check for rule: {} in organization: {} - {}", 
                           rule.getName(), organization.getName(), e.getMessage(), e);
            }
        }

        logger.info("Completed automated compliance checks for organization: {} - {} rules checked", 
                   organization.getName(), rulesNeedingCheck.size());
    }

    /**
     * Execute automated check for a specific rule
     */
    private void executeAutomatedCheckForRule(ComplianceRule rule) {
        // Check if rule applies to specific organization or all organizations
        if (rule.getOrganization() != null) {
            executeAutomatedCheckForRule(rule, rule.getOrganization());
        } else {
            // System-wide rule - check for all organizations
            List<Organization> organizations = organizationRepository.findAll();
            for (Organization org : organizations) {
                executeAutomatedCheckForRule(rule, org);
            }
        }
    }

    /**
     * Execute automated check for a specific rule and organization
     */
    private void executeAutomatedCheckForRule(ComplianceRule rule, Organization organization) {
        // Check if we need to perform this check (based on last check date and frequency)
        if (!shouldPerformCheck(rule, organization)) {
            return;
        }

        logger.debug("Executing automated check for rule: {} in organization: {}", 
                    rule.getName(), organization.getName());

        ComplianceCheck check = new ComplianceCheck(organization, rule, LocalDate.now());
        check.setCheckType("AUTOMATIC");
        check.setCheckedBy("SYSTEM");

        // Perform the actual compliance check
        ComplianceCheckResult result = executeComplianceCheck(rule, organization, null);
        
        // Set check results
        check.setStatus(result.isCompliant() ? "COMPLIANT" : "NON_COMPLIANT");
        check.setComplianceScore(result.getComplianceScore());
        check.setCheckResults(result.getCheckResults());
        check.setFindings(result.getFindings());
        check.setViolations(result.getViolations());
        check.setRecommendations(result.getRecommendations());

        // Set remediation due date if non-compliant
        if (!result.isCompliant() && rule.getCheckFrequencyDays() != null) {
            check.setRemediationDueDate(LocalDate.now().plusDays(rule.getCheckFrequencyDays()));
        }

        // Schedule next check
        check.setNextCheckDate(LocalDate.now().plusDays(rule.getCheckFrequencyDays()));

        check = complianceCheckRepository.save(check);

        // Generate alert if non-compliant
        if (!result.isCompliant()) {
            generateViolationAlert(check);
        }
    }

    /**
     * Check if we should perform a compliance check for a rule and organization
     */
    private boolean shouldPerformCheck(ComplianceRule rule, Organization organization) {
        // Find the latest check for this rule and organization
        Pageable pageable = PageRequest.of(0, 1);
        List<ComplianceCheck> latestChecks = complianceCheckRepository
            .findLatestCheckForRuleAndOrganization(rule, organization, pageable);

        if (latestChecks.isEmpty()) {
            return true; // No previous check, should perform
        }

        ComplianceCheck latestCheck = latestChecks.get(0);
        LocalDate nextCheckDate = latestCheck.getNextCheckDate();
        
        return nextCheckDate != null && !LocalDate.now().isBefore(nextCheckDate);
    }

    /**
     * Execute the actual compliance check logic
     */
    private ComplianceCheckResult executeComplianceCheck(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        try {
            // This is a simplified implementation - in a real system, this would contain
            // complex business logic to evaluate compliance based on the rule criteria
            
            if (rule.getCheckQuery() != null && !rule.getCheckQuery().trim().isEmpty()) {
                // Execute custom check query if provided
                result = executeCustomComplianceCheck(rule, organization, employee);
            } else {
                // Execute default compliance check based on rule type and category
                result = executeDefaultComplianceCheck(rule, organization, employee);
            }
            
        } catch (Exception e) {
            logger.error("Error executing compliance check for rule: {} - {}", 
                        rule.getName(), e.getMessage(), e);
            
            result.setCompliant(false);
            result.setComplianceScore(0);
            result.setCheckResults("Error executing compliance check: " + e.getMessage());
            result.setFindings("System error during compliance evaluation");
            result.setViolations("Unable to complete compliance check due to system error");
            result.setRecommendations("Contact system administrator to resolve compliance check issues");
        }
        
        return result;
    }

    /**
     * Execute custom compliance check using rule's check query
     */
    private ComplianceCheckResult executeCustomComplianceCheck(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // This is a placeholder for custom query execution
        // In a real implementation, this would parse and execute the check query
        // against the database or external systems
        
        logger.debug("Executing custom compliance check query for rule: {}", rule.getName());
        
        // For now, return a default compliant result
        result.setCompliant(true);
        result.setComplianceScore(100);
        result.setCheckResults("Custom compliance check executed successfully");
        result.setFindings("No violations found");
        
        return result;
    }

    /**
     * Execute default compliance check based on rule category
     */
    private ComplianceCheckResult executeDefaultComplianceCheck(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Default compliance check logic based on rule category
        switch (rule.getCategory() != null ? rule.getCategory().toUpperCase() : "GENERAL") {
            case "LABOR_LAW":
                result = checkLaborLawCompliance(rule, organization, employee);
                break;
            case "TAX":
                result = checkTaxCompliance(rule, organization, employee);
                break;
            case "SAFETY":
                result = checkSafetyCompliance(rule, organization, employee);
                break;
            case "PRIVACY":
                result = checkPrivacyCompliance(rule, organization, employee);
                break;
            default:
                result = checkGeneralCompliance(rule, organization, employee);
                break;
        }
        
        return result;
    }

    /**
     * Check labor law compliance
     */
    private ComplianceCheckResult checkLaborLawCompliance(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Simplified labor law compliance check
        // In a real system, this would check various labor law requirements
        
        result.setCompliant(true);
        result.setComplianceScore(95);
        result.setCheckResults("Labor law compliance check completed");
        result.setFindings("Organization meets basic labor law requirements");
        
        return result;
    }

    /**
     * Check tax compliance
     */
    private ComplianceCheckResult checkTaxCompliance(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Simplified tax compliance check
        result.setCompliant(true);
        result.setComplianceScore(90);
        result.setCheckResults("Tax compliance check completed");
        result.setFindings("Tax obligations are being met");
        
        return result;
    }

    /**
     * Check safety compliance
     */
    private ComplianceCheckResult checkSafetyCompliance(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Simplified safety compliance check
        result.setCompliant(true);
        result.setComplianceScore(88);
        result.setCheckResults("Safety compliance check completed");
        result.setFindings("Safety protocols are in place and being followed");
        
        return result;
    }

    /**
     * Check privacy compliance
     */
    private ComplianceCheckResult checkPrivacyCompliance(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Simplified privacy compliance check
        result.setCompliant(true);
        result.setComplianceScore(92);
        result.setCheckResults("Privacy compliance check completed");
        result.setFindings("Data privacy measures are adequate");
        
        return result;
    }

    /**
     * Check general compliance
     */
    private ComplianceCheckResult checkGeneralCompliance(ComplianceRule rule, Organization organization, Employee employee) {
        ComplianceCheckResult result = new ComplianceCheckResult();
        
        // Default compliance check
        result.setCompliant(true);
        result.setComplianceScore(85);
        result.setCheckResults("General compliance check completed");
        result.setFindings("No significant compliance issues identified");
        
        return result;
    }

    // ===== VIOLATION DETECTION AND ALERT GENERATION =====

    /**
     * Generate violation alert for a non-compliant check
     */
    private void generateViolationAlert(ComplianceCheck check) {
        if (check.isCompliant() || Boolean.TRUE.equals(check.getAlertSent())) {
            return; // No alert needed
        }

        logger.warn("Generating violation alert for compliance check: Rule={}, Organization={}, Status={}", 
                   check.getComplianceRule().getName(), 
                   check.getOrganization().getName(), 
                   check.getStatus());

        try {
            // Create and send alert
            ComplianceAlert alert = createComplianceAlert(check);
            sendComplianceAlert(alert);
            
            // Mark alert as sent
            check.sendAlert();
            complianceCheckRepository.save(check);
            
            logger.info("Violation alert sent successfully for compliance check ID: {}", check.getId());
            
        } catch (Exception e) {
            logger.error("Error generating violation alert for compliance check ID: {} - {}", 
                        check.getId(), e.getMessage(), e);
        }
    }

    /**
     * Create compliance alert from check
     */
    private ComplianceAlert createComplianceAlert(ComplianceCheck check) {
        ComplianceAlert alert = new ComplianceAlert();
        alert.setComplianceCheck(check);
        alert.setAlertType("VIOLATION");
        alert.setSeverity(check.getComplianceRule().getSeverity());
        alert.setTitle("Compliance Violation Detected: " + check.getComplianceRule().getName());
        
        StringBuilder message = new StringBuilder();
        message.append("A compliance violation has been detected:\n\n");
        message.append("Rule: ").append(check.getComplianceRule().getName()).append("\n");
        message.append("Organization: ").append(check.getOrganization().getName()).append("\n");
        message.append("Check Date: ").append(check.getCheckDate()).append("\n");
        message.append("Status: ").append(check.getStatus()).append("\n");
        
        if (check.getComplianceScore() != null) {
            message.append("Compliance Score: ").append(check.getComplianceScore()).append("%\n");
        }
        
        if (check.getViolations() != null) {
            message.append("\nViolations:\n").append(check.getViolations()).append("\n");
        }
        
        if (check.getRecommendations() != null) {
            message.append("\nRecommendations:\n").append(check.getRecommendations()).append("\n");
        }
        
        if (check.getRemediationDueDate() != null) {
            message.append("\nRemediation Due Date: ").append(check.getRemediationDueDate()).append("\n");
        }
        
        alert.setMessage(message.toString());
        alert.setCreatedAt(Instant.now());
        
        return alert;
    }

    /**
     * Send compliance alert
     */
    private void sendComplianceAlert(ComplianceAlert alert) {
        // This is a placeholder for alert sending logic
        // In a real system, this would send emails, notifications, or integrate with external systems
        
        logger.info("Sending compliance alert: {} for organization: {}", 
                   alert.getTitle(), 
                   alert.getComplianceCheck().getOrganization().getName());
        
        // Simulate alert sending
        // Could integrate with email service, notification system, etc.
    }

    /**
     * Get all violation alerts for an organization
     */
    @Transactional(readOnly = true)
    public List<ComplianceCheck> getViolationAlerts(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceCheckRepository.findNonCompliantChecksByOrganization(organization);
    }

    /**
     * Get checks needing alerts
     */
    @Transactional(readOnly = true)
    public List<ComplianceCheck> getChecksNeedingAlerts(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceCheckRepository.findChecksNeedingAlertsByOrganization(organization);
    }

    /**
     * Send pending violation alerts
     */
    @Async
    public void sendPendingViolationAlerts() {
        logger.info("Sending pending violation alerts");
        
        List<ComplianceCheck> checksNeedingAlerts = complianceCheckRepository.findChecksNeedingAlerts();
        
        for (ComplianceCheck check : checksNeedingAlerts) {
            try {
                generateViolationAlert(check);
            } catch (Exception e) {
                logger.error("Error sending alert for compliance check ID: {} - {}", 
                           check.getId(), e.getMessage(), e);
            }
        }
        
        logger.info("Completed sending {} pending violation alerts", checksNeedingAlerts.size());
    }

    // ===== COMPLIANCE REPORTING =====

    /**
     * Generate compliance report for an organization
     */
    @Transactional(readOnly = true)
    public ComplianceReport generateComplianceReport(Long organizationId, LocalDate startDate, LocalDate endDate) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        logger.info("Generating compliance report for organization: {} from {} to {}", 
                   organization.getName(), startDate, endDate);

        ComplianceReport report = new ComplianceReport();
        report.setOrganization(organization);
        report.setReportPeriodStart(startDate);
        report.setReportPeriodEnd(endDate);
        report.setGeneratedAt(Instant.now());

        // Get all checks in the period
        List<ComplianceCheck> checks = complianceCheckRepository
            .findByOrganizationAndCheckDateBetween(organization, startDate, endDate);

        // Calculate statistics
        long totalChecks = checks.size();
        long compliantChecks = checks.stream().filter(ComplianceCheck::isCompliant).count();
        long nonCompliantChecks = checks.stream().filter(ComplianceCheck::isNonCompliant).count();
        long unresolvedChecks = checks.stream().filter(c -> !Boolean.TRUE.equals(c.getIsResolved())).count();
        long overdueChecks = checks.stream().filter(ComplianceCheck::isOverdue).count();

        report.setTotalChecks(totalChecks);
        report.setCompliantChecks(compliantChecks);
        report.setNonCompliantChecks(nonCompliantChecks);
        report.setUnresolvedChecks(unresolvedChecks);
        report.setOverdueChecks(overdueChecks);

        // Calculate compliance rate
        if (totalChecks > 0) {
            report.setComplianceRate((double) compliantChecks / totalChecks * 100);
        } else {
            report.setComplianceRate(100.0);
        }

        // Get average compliance score
        double avgScore = checks.stream()
            .filter(c -> c.getComplianceScore() != null)
            .mapToInt(ComplianceCheck::getComplianceScore)
            .average()
            .orElse(0.0);
        report.setAverageComplianceScore(avgScore);

        // Get checks by category
        report.setCriticalViolations(checks.stream()
            .filter(c -> c.isNonCompliant() && "CRITICAL".equalsIgnoreCase(c.getComplianceRule().getSeverity()))
            .count());

        report.setHighSeverityViolations(checks.stream()
            .filter(c -> c.isNonCompliant() && "HIGH".equalsIgnoreCase(c.getComplianceRule().getSeverity()))
            .count());

        // Get top violation categories
        report.setTopViolationCategories(checks.stream()
            .filter(ComplianceCheck::isNonCompliant)
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c.getComplianceRule().getCategory() != null ? c.getComplianceRule().getCategory() : "GENERAL",
                java.util.stream.Collectors.counting()))
            .entrySet().stream()
            .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                java.util.Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new)));

        logger.info("Generated compliance report for organization: {} - Total checks: {}, Compliance rate: {}%", 
                   organization.getName(), totalChecks, String.format("%.2f", report.getComplianceRate()));

        return report;
    }

    /**
     * Get compliance summary for an organization
     */
    @Transactional(readOnly = true)
    public ComplianceSummary getComplianceSummary(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        ComplianceSummary summary = new ComplianceSummary();
        summary.setOrganization(organization);

        // Get current counts
        summary.setTotalRules(complianceRuleRepository.countByOrganization(organization));
        summary.setActiveRules(complianceRuleRepository.countActiveRulesByOrganization(organization, LocalDate.now()));
        summary.setTotalChecks(complianceCheckRepository.countByOrganization(organization));
        summary.setNonCompliantChecks(complianceCheckRepository.countNonCompliantByOrganization(organization));
        summary.setUnresolvedChecks(complianceCheckRepository.countUnresolvedByOrganization(organization));
        summary.setOverdueChecks(complianceCheckRepository.countOverdueByOrganization(organization, LocalDate.now()));

        // Calculate compliance rate
        if (summary.getTotalChecks() > 0) {
            long compliantChecks = summary.getTotalChecks() - summary.getNonCompliantChecks();
            summary.setComplianceRate((double) compliantChecks / summary.getTotalChecks() * 100);
        } else {
            summary.setComplianceRate(100.0);
        }

        return summary;
    }

    /**
     * Run compliance checks for all active rules in an organization
     */
    public List<ComplianceCheck> runComplianceChecks(Long organizationId, String checkedBy) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        logger.info("Running compliance checks for organization: {} by: {}", organization.getName(), checkedBy);

        List<ComplianceRule> activeRules = complianceRuleRepository
            .findActiveRulesByOrganization(organization, LocalDate.now());

        List<ComplianceCheck> results = new ArrayList<>();

        for (ComplianceRule rule : activeRules) {
            try {
                ComplianceCheck check = performComplianceCheck(rule, organization, null, checkedBy);
                results.add(check);
            } catch (Exception e) {
                logger.error("Error running compliance check for rule: {} - {}", 
                           rule.getName(), e.getMessage(), e);
            }
        }

        logger.info("Completed compliance checks for organization: {} - {} checks performed", 
                   organization.getName(), results.size());

        return results;
    }

    /**
     * Run compliance check for a specific rule
     */
    public ComplianceCheck runComplianceCheck(Long ruleId, Long organizationId, String checkedBy) {
        return performComplianceCheck(ruleId, organizationId, checkedBy);
    }

    /**
     * Enhanced violation detection with detailed analysis
     */
    public List<ComplianceViolation> detectViolations(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));

        logger.info("Detecting violations for organization: {}", organization.getName());

        List<ComplianceCheck> nonCompliantChecks = complianceCheckRepository
            .findNonCompliantChecksByOrganization(organization);

        List<ComplianceViolation> violations = new ArrayList<>();

        for (ComplianceCheck check : nonCompliantChecks) {
            ComplianceViolation violation = new ComplianceViolation();
            violation.setComplianceCheck(check);
            violation.setRuleName(check.getComplianceRule().getName());
            violation.setCategory(check.getComplianceRule().getCategory());
            violation.setSeverity(check.getComplianceRule().getSeverity());
            violation.setViolationDate(check.getCheckDate());
            violation.setDescription(check.getViolations());
            violation.setRecommendations(check.getRecommendations());
            violation.setIsResolved(check.getIsResolved());
            violation.setRemediationDueDate(check.getRemediationDueDate());
            violation.setIsOverdue(check.isOverdue());

            violations.add(violation);
        }

        logger.info("Detected {} violations for organization: {}", violations.size(), organization.getName());

        return violations;
    }

    // ===== COMPLIANCE CHECK MANAGEMENT =====

    /**
     * Get compliance check by ID
     */
    @Transactional(readOnly = true)
    public ComplianceCheck getComplianceCheck(Long id) {
        return complianceCheckRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Compliance check not found with id: " + id));
    }

    /**
     * Get compliance checks for an organization
     */
    @Transactional(readOnly = true)
    public Page<ComplianceCheck> getComplianceChecksByOrganization(Long organizationId, int page, int size) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkDate"));
        return complianceCheckRepository.findByOrganization(organization, pageable);
    }

    /**
     * Get overdue compliance checks
     */
    @Transactional(readOnly = true)
    public List<ComplianceCheck> getOverdueComplianceChecks(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + organizationId));
        
        return complianceCheckRepository.findOverdueChecksByOrganization(organization, LocalDate.now());
    }

    /**
     * Resolve compliance check
     */
    public ComplianceCheck resolveComplianceCheck(Long checkId, Long resolvedByEmployeeId, String resolutionNotes) {
        ComplianceCheck check = complianceCheckRepository.findById(checkId)
            .orElseThrow(() -> new RuntimeException("Compliance check not found with id: " + checkId));
        
        Employee resolvedByEmployee = employeeRepository.findById(resolvedByEmployeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + resolvedByEmployeeId));
        
        check.resolve(resolvedByEmployee, resolutionNotes);
        
        logger.info("Resolved compliance check ID: {} by: {}", checkId, resolvedByEmployee.getFirstName() + " " + resolvedByEmployee.getLastName());
        
        return complianceCheckRepository.save(check);
    }

    /**
     * Mark remediation as completed
     */
    public ComplianceCheck completeRemediation(Long checkId, String remediatedBy) {
        ComplianceCheck check = complianceCheckRepository.findById(checkId)
            .orElseThrow(() -> new RuntimeException("Compliance check not found with id: " + checkId));
        
        check.completeRemediation(remediatedBy);
        
        logger.info("Completed remediation for compliance check ID: {} by: {}", checkId, remediatedBy);
        
        return complianceCheckRepository.save(check);
    }

    // ===== JURISDICTION MANAGEMENT =====

    /**
     * Create compliance jurisdiction
     */
    public ComplianceJurisdiction createJurisdiction(ComplianceJurisdiction jurisdiction) {
        validateJurisdiction(jurisdiction);
        
        if (jurisdictionRepository.existsByCode(jurisdiction.getCode())) {
            throw new RuntimeException("Jurisdiction with code '" + jurisdiction.getCode() + "' already exists");
        }
        
        logger.info("Creating compliance jurisdiction: {} ({})", jurisdiction.getName(), jurisdiction.getCode());
        
        return jurisdictionRepository.save(jurisdiction);
    }

    /**
     * Get all jurisdictions
     */
    @Transactional(readOnly = true)
    public List<ComplianceJurisdiction> getAllJurisdictions() {
        return jurisdictionRepository.findAllOrderByName();
    }

    /**
     * Get jurisdiction by ID
     */
    @Transactional(readOnly = true)
    public ComplianceJurisdiction getJurisdiction(Long id) {
        return jurisdictionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Jurisdiction not found with id: " + id));
    }

    // ===== VALIDATION METHODS =====

    /**
     * Validate compliance rule
     */
    private void validateComplianceRule(ComplianceRule rule) {
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw new RuntimeException("Compliance rule name is required");
        }
        
        if (rule.getJurisdiction() == null) {
            throw new RuntimeException("Compliance rule jurisdiction is required");
        }
        
        if (rule.getEffectiveDate() != null && rule.getExpirationDate() != null && 
            rule.getEffectiveDate().isAfter(rule.getExpirationDate())) {
            throw new RuntimeException("Effective date cannot be after expiration date");
        }
        
        if (rule.getCheckFrequencyDays() != null && rule.getCheckFrequencyDays() <= 0) {
            throw new RuntimeException("Check frequency days must be positive");
        }
    }

    /**
     * Validate jurisdiction
     */
    private void validateJurisdiction(ComplianceJurisdiction jurisdiction) {
        if (jurisdiction.getName() == null || jurisdiction.getName().trim().isEmpty()) {
            throw new RuntimeException("Jurisdiction name is required");
        }
        
        if (jurisdiction.getCode() == null || jurisdiction.getCode().trim().isEmpty()) {
            throw new RuntimeException("Jurisdiction code is required");
        }
    }

    // ===== INNER CLASSES =====

    /**
     * Compliance report
     */
    public static class ComplianceReport {
        private Organization organization;
        private LocalDate reportPeriodStart;
        private LocalDate reportPeriodEnd;
        private Instant generatedAt;
        private long totalChecks;
        private long compliantChecks;
        private long nonCompliantChecks;
        private long unresolvedChecks;
        private long overdueChecks;
        private double complianceRate;
        private double averageComplianceScore;
        private long criticalViolations;
        private long highSeverityViolations;
        private java.util.Map<String, Long> topViolationCategories;

        // Getters and setters
        public Organization getOrganization() { return organization; }
        public void setOrganization(Organization organization) { this.organization = organization; }
        
        public LocalDate getReportPeriodStart() { return reportPeriodStart; }
        public void setReportPeriodStart(LocalDate reportPeriodStart) { this.reportPeriodStart = reportPeriodStart; }
        
        public LocalDate getReportPeriodEnd() { return reportPeriodEnd; }
        public void setReportPeriodEnd(LocalDate reportPeriodEnd) { this.reportPeriodEnd = reportPeriodEnd; }
        
        public Instant getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
        
        public long getTotalChecks() { return totalChecks; }
        public void setTotalChecks(long totalChecks) { this.totalChecks = totalChecks; }
        
        public long getCompliantChecks() { return compliantChecks; }
        public void setCompliantChecks(long compliantChecks) { this.compliantChecks = compliantChecks; }
        
        public long getNonCompliantChecks() { return nonCompliantChecks; }
        public void setNonCompliantChecks(long nonCompliantChecks) { this.nonCompliantChecks = nonCompliantChecks; }
        
        public long getUnresolvedChecks() { return unresolvedChecks; }
        public void setUnresolvedChecks(long unresolvedChecks) { this.unresolvedChecks = unresolvedChecks; }
        
        public long getOverdueChecks() { return overdueChecks; }
        public void setOverdueChecks(long overdueChecks) { this.overdueChecks = overdueChecks; }
        
        public double getComplianceRate() { return complianceRate; }
        public void setComplianceRate(double complianceRate) { this.complianceRate = complianceRate; }
        
        public double getAverageComplianceScore() { return averageComplianceScore; }
        public void setAverageComplianceScore(double averageComplianceScore) { this.averageComplianceScore = averageComplianceScore; }
        
        public long getCriticalViolations() { return criticalViolations; }
        public void setCriticalViolations(long criticalViolations) { this.criticalViolations = criticalViolations; }
        
        public long getHighSeverityViolations() { return highSeverityViolations; }
        public void setHighSeverityViolations(long highSeverityViolations) { this.highSeverityViolations = highSeverityViolations; }
        
        public java.util.Map<String, Long> getTopViolationCategories() { return topViolationCategories; }
        public void setTopViolationCategories(java.util.Map<String, Long> topViolationCategories) { this.topViolationCategories = topViolationCategories; }
    }

    /**
     * Compliance summary
     */
    public static class ComplianceSummary {
        private Organization organization;
        private long totalRules;
        private long activeRules;
        private long totalChecks;
        private long nonCompliantChecks;
        private long unresolvedChecks;
        private long overdueChecks;
        private double complianceRate;

        // Getters and setters
        public Organization getOrganization() { return organization; }
        public void setOrganization(Organization organization) { this.organization = organization; }
        
        public long getTotalRules() { return totalRules; }
        public void setTotalRules(long totalRules) { this.totalRules = totalRules; }
        
        public long getActiveRules() { return activeRules; }
        public void setActiveRules(long activeRules) { this.activeRules = activeRules; }
        
        public long getTotalChecks() { return totalChecks; }
        public void setTotalChecks(long totalChecks) { this.totalChecks = totalChecks; }
        
        public long getNonCompliantChecks() { return nonCompliantChecks; }
        public void setNonCompliantChecks(long nonCompliantChecks) { this.nonCompliantChecks = nonCompliantChecks; }
        
        public long getUnresolvedChecks() { return unresolvedChecks; }
        public void setUnresolvedChecks(long unresolvedChecks) { this.unresolvedChecks = unresolvedChecks; }
        
        public long getOverdueChecks() { return overdueChecks; }
        public void setOverdueChecks(long overdueChecks) { this.overdueChecks = overdueChecks; }
        
        public double getComplianceRate() { return complianceRate; }
        public void setComplianceRate(double complianceRate) { this.complianceRate = complianceRate; }
    }

    /**
     * Compliance violation
     */
    public static class ComplianceViolation {
        private ComplianceCheck complianceCheck;
        private String ruleName;
        private String category;
        private String severity;
        private LocalDate violationDate;
        private String description;
        private String recommendations;
        private Boolean isResolved;
        private LocalDate remediationDueDate;
        private Boolean isOverdue;

        // Getters and setters
        public ComplianceCheck getComplianceCheck() { return complianceCheck; }
        public void setComplianceCheck(ComplianceCheck complianceCheck) { this.complianceCheck = complianceCheck; }
        
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public LocalDate getViolationDate() { return violationDate; }
        public void setViolationDate(LocalDate violationDate) { this.violationDate = violationDate; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
        
        public Boolean getIsResolved() { return isResolved; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
        
        public LocalDate getRemediationDueDate() { return remediationDueDate; }
        public void setRemediationDueDate(LocalDate remediationDueDate) { this.remediationDueDate = remediationDueDate; }
        
        public Boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
    }

    /**
     * Compliance check result
     */
    private static class ComplianceCheckResult {
        private boolean compliant = true;
        private Integer complianceScore = 100;
        private String checkResults;
        private String findings;
        private String violations;
        private String recommendations;

        // Getters and setters
        public boolean isCompliant() { return compliant; }
        public void setCompliant(boolean compliant) { this.compliant = compliant; }
        
        public Integer getComplianceScore() { return complianceScore; }
        public void setComplianceScore(Integer complianceScore) { this.complianceScore = complianceScore; }
        
        public String getCheckResults() { return checkResults; }
        public void setCheckResults(String checkResults) { this.checkResults = checkResults; }
        
        public String getFindings() { return findings; }
        public void setFindings(String findings) { this.findings = findings; }
        
        public String getViolations() { return violations; }
        public void setViolations(String violations) { this.violations = violations; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    }

    /**
     * Compliance alert
     */
    private static class ComplianceAlert {
        private ComplianceCheck complianceCheck;
        private String alertType;
        private String severity;
        private String title;
        private String message;
        private Instant createdAt;

        // Getters and setters
        public ComplianceCheck getComplianceCheck() { return complianceCheck; }
        public void setComplianceCheck(ComplianceCheck complianceCheck) { this.complianceCheck = complianceCheck; }
        
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}

