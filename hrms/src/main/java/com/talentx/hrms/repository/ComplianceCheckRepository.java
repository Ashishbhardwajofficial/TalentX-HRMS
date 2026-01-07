package com.talentx.hrms.repository;

import com.talentx.hrms.entity.compliance.ComplianceCheck;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, Long> {
    
    // Find by compliance rule
    List<ComplianceCheck> findByComplianceRule(ComplianceRule complianceRule);
    Page<ComplianceCheck> findByComplianceRule(ComplianceRule complianceRule, Pageable pageable);
    
    // Find by organization
    List<ComplianceCheck> findByOrganization(Organization organization);
    Page<ComplianceCheck> findByOrganization(Organization organization, Pageable pageable);
    
    // Find by employee
    List<ComplianceCheck> findByEmployee(Employee employee);
    Page<ComplianceCheck> findByEmployee(Employee employee, Pageable pageable);
    
    // Find by organization and employee
    List<ComplianceCheck> findByOrganizationAndEmployee(Organization organization, Employee employee);
    
    // Find by status
    List<ComplianceCheck> findByStatus(String status);
    List<ComplianceCheck> findByOrganizationAndStatus(Organization organization, String status);
    
    // Find by check type
    List<ComplianceCheck> findByCheckType(String checkType);
    List<ComplianceCheck> findByOrganizationAndCheckType(Organization organization, String checkType);
    
    // Find compliant checks
    @Query("SELECT c FROM ComplianceCheck c WHERE c.status = 'COMPLIANT'")
    List<ComplianceCheck> findCompliantChecks();
    
    // Find non-compliant checks
    @Query("SELECT c FROM ComplianceCheck c WHERE c.status = 'NON_COMPLIANT'")
    List<ComplianceCheck> findNonCompliantChecks();
    
    // Find non-compliant checks by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND c.status = 'NON_COMPLIANT'")
    List<ComplianceCheck> findNonCompliantChecksByOrganization(@Param("organization") Organization organization);
    
    // Find pending checks
    @Query("SELECT c FROM ComplianceCheck c WHERE c.status = 'PENDING'")
    List<ComplianceCheck> findPendingChecks();
    
    // Find pending checks by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND c.status = 'PENDING'")
    List<ComplianceCheck> findPendingChecksByOrganization(@Param("organization") Organization organization);
    
    // Find unresolved checks
    @Query("SELECT c FROM ComplianceCheck c WHERE c.isResolved = false")
    List<ComplianceCheck> findUnresolvedChecks();
    
    // Find unresolved checks by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND c.isResolved = false")
    List<ComplianceCheck> findUnresolvedChecksByOrganization(@Param("organization") Organization organization);
    
    // Find overdue checks (remediation due date passed)
    @Query("SELECT c FROM ComplianceCheck c WHERE c.remediationDueDate IS NOT NULL AND " +
           "c.remediationDueDate < :currentDate AND c.isResolved = false")
    List<ComplianceCheck> findOverdueChecks(@Param("currentDate") LocalDate currentDate);
    
    // Find overdue checks by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.remediationDueDate IS NOT NULL AND c.remediationDueDate < :currentDate AND c.isResolved = false")
    List<ComplianceCheck> findOverdueChecksByOrganization(@Param("organization") Organization organization, 
                                                         @Param("currentDate") LocalDate currentDate);
    
    // Find checks due for remediation soon
    @Query("SELECT c FROM ComplianceCheck c WHERE c.remediationDueDate IS NOT NULL AND " +
           "c.remediationDueDate BETWEEN :currentDate AND :endDate AND c.isResolved = false")
    List<ComplianceCheck> findChecksDueSoon(@Param("currentDate") LocalDate currentDate, 
                                           @Param("endDate") LocalDate endDate);
    
    // Find checks by date range
    @Query("SELECT c FROM ComplianceCheck c WHERE c.checkDate BETWEEN :startDate AND :endDate")
    List<ComplianceCheck> findByCheckDateBetween(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    // Find checks by organization and date range
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.checkDate BETWEEN :startDate AND :endDate")
    List<ComplianceCheck> findByOrganizationAndCheckDateBetween(@Param("organization") Organization organization,
                                                               @Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);
    
    // Find latest check for a rule
    @Query("SELECT c FROM ComplianceCheck c WHERE c.complianceRule = :rule " +
           "ORDER BY c.checkDate DESC, c.checkedAt DESC")
    List<ComplianceCheck> findLatestCheckForRule(@Param("rule") ComplianceRule rule, Pageable pageable);
    
    // Find latest check for a rule and organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.complianceRule = :rule AND c.organization = :organization " +
           "ORDER BY c.checkDate DESC, c.checkedAt DESC")
    List<ComplianceCheck> findLatestCheckForRuleAndOrganization(@Param("rule") ComplianceRule rule, 
                                                               @Param("organization") Organization organization, 
                                                               Pageable pageable);
    
    // Find latest check for a rule and employee
    @Query("SELECT c FROM ComplianceCheck c WHERE c.complianceRule = :rule AND c.employee = :employee " +
           "ORDER BY c.checkDate DESC, c.checkedAt DESC")
    List<ComplianceCheck> findLatestCheckForRuleAndEmployee(@Param("rule") ComplianceRule rule, 
                                                           @Param("employee") Employee employee, 
                                                           Pageable pageable);
    
    // Find checks needing alerts (non-compliant and alert not sent)
    @Query("SELECT c FROM ComplianceCheck c WHERE c.status = 'NON_COMPLIANT' AND c.alertSent = false")
    List<ComplianceCheck> findChecksNeedingAlerts();
    
    // Find checks needing alerts by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.status = 'NON_COMPLIANT' AND c.alertSent = false")
    List<ComplianceCheck> findChecksNeedingAlertsByOrganization(@Param("organization") Organization organization);
    
    // Find checks with violations
    @Query("SELECT c FROM ComplianceCheck c WHERE c.violations IS NOT NULL AND c.violations != ''")
    List<ComplianceCheck> findChecksWithViolations();
    
    // Find checks with violations by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.violations IS NOT NULL AND c.violations != ''")
    List<ComplianceCheck> findChecksWithViolationsByOrganization(@Param("organization") Organization organization);
    
    // Find checks by compliance score range
    @Query("SELECT c FROM ComplianceCheck c WHERE c.complianceScore BETWEEN :minScore AND :maxScore")
    List<ComplianceCheck> findByComplianceScoreBetween(@Param("minScore") Integer minScore, 
                                                      @Param("maxScore") Integer maxScore);
    
    // Find checks with low compliance scores
    @Query("SELECT c FROM ComplianceCheck c WHERE c.complianceScore IS NOT NULL AND c.complianceScore < :threshold")
    List<ComplianceCheck> findChecksWithLowScores(@Param("threshold") Integer threshold);
    
    // Find checks with low compliance scores by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.complianceScore IS NOT NULL AND c.complianceScore < :threshold")
    List<ComplianceCheck> findChecksWithLowScoresByOrganization(@Param("organization") Organization organization,
                                                               @Param("threshold") Integer threshold);
    
    // Find checks due for next check
    @Query("SELECT c FROM ComplianceCheck c WHERE c.nextCheckDate IS NOT NULL AND " +
           "c.nextCheckDate <= :currentDate")
    List<ComplianceCheck> findChecksDueForNextCheck(@Param("currentDate") LocalDate currentDate);
    
    // Find checks due for next check by organization
    @Query("SELECT c FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.nextCheckDate IS NOT NULL AND c.nextCheckDate <= :currentDate")
    List<ComplianceCheck> findChecksDueForNextCheckByOrganization(@Param("organization") Organization organization,
                                                                 @Param("currentDate") LocalDate currentDate);
    
    // Count checks by organization
    long countByOrganization(Organization organization);
    
    // Count checks by status
    long countByStatus(String status);
    long countByOrganizationAndStatus(Organization organization, String status);
    
    // Count non-compliant checks by organization
    @Query("SELECT COUNT(c) FROM ComplianceCheck c WHERE c.organization = :organization AND c.status = 'NON_COMPLIANT'")
    long countNonCompliantByOrganization(@Param("organization") Organization organization);
    
    // Count unresolved checks by organization
    @Query("SELECT COUNT(c) FROM ComplianceCheck c WHERE c.organization = :organization AND c.isResolved = false")
    long countUnresolvedByOrganization(@Param("organization") Organization organization);
    
    // Count overdue checks by organization
    @Query("SELECT COUNT(c) FROM ComplianceCheck c WHERE c.organization = :organization AND " +
           "c.remediationDueDate IS NOT NULL AND c.remediationDueDate < :currentDate AND c.isResolved = false")
    long countOverdueByOrganization(@Param("organization") Organization organization, 
                                   @Param("currentDate") LocalDate currentDate);
    
    // Check if a rule has been checked for an organization on a specific date
    boolean existsByComplianceRuleAndOrganizationAndCheckDate(ComplianceRule complianceRule, 
                                                             Organization organization, 
                                                             LocalDate checkDate);
    
    // Check if a rule has been checked for an employee on a specific date
    boolean existsByComplianceRuleAndEmployeeAndCheckDate(ComplianceRule complianceRule, 
                                                         Employee employee, 
                                                         LocalDate checkDate);
}

