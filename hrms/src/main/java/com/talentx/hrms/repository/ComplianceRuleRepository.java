package com.talentx.hrms.repository;

import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, Long> {
    
    // Find by rule code
    Optional<ComplianceRule> findByRuleCode(String ruleCode);
    
    // Find by organization
    List<ComplianceRule> findByOrganization(Organization organization);
    Page<ComplianceRule> findByOrganization(Organization organization, Pageable pageable);
    
    // Find by jurisdiction
    List<ComplianceRule> findByJurisdiction(ComplianceJurisdiction jurisdiction);
    Page<ComplianceRule> findByJurisdiction(ComplianceJurisdiction jurisdiction, Pageable pageable);
    
    // Find by organization and jurisdiction
    List<ComplianceRule> findByOrganizationAndJurisdiction(Organization organization, ComplianceJurisdiction jurisdiction);
    
    // Find by category
    List<ComplianceRule> findByCategory(String category);
    List<ComplianceRule> findByOrganizationAndCategory(Organization organization, String category);
    
    // Find by rule type
    List<ComplianceRule> findByRuleType(String ruleType);
    List<ComplianceRule> findByOrganizationAndRuleType(Organization organization, String ruleType);
    
    // Find by severity
    List<ComplianceRule> findBySeverity(String severity);
    List<ComplianceRule> findByOrganizationAndSeverity(Organization organization, String severity);
    
    // Find active rules (within effective date range)
    @Query("SELECT r FROM ComplianceRule r WHERE " +
           "(r.effectiveDate IS NULL OR r.effectiveDate <= :currentDate) AND " +
           "(r.expirationDate IS NULL OR r.expirationDate >= :currentDate)")
    List<ComplianceRule> findActiveRules(@Param("currentDate") LocalDate currentDate);
    
    // Find active rules by organization
    @Query("SELECT r FROM ComplianceRule r WHERE r.organization = :organization AND " +
           "(r.effectiveDate IS NULL OR r.effectiveDate <= :currentDate) AND " +
           "(r.expirationDate IS NULL OR r.expirationDate >= :currentDate)")
    List<ComplianceRule> findActiveRulesByOrganization(@Param("organization") Organization organization, 
                                                      @Param("currentDate") LocalDate currentDate);
    
    // Find active rules by jurisdiction
    @Query("SELECT r FROM ComplianceRule r WHERE r.jurisdiction = :jurisdiction AND " +
           "(r.effectiveDate IS NULL OR r.effectiveDate <= :currentDate) AND " +
           "(r.expirationDate IS NULL OR r.expirationDate >= :currentDate)")
    List<ComplianceRule> findActiveRulesByJurisdiction(@Param("jurisdiction") ComplianceJurisdiction jurisdiction, 
                                                      @Param("currentDate") LocalDate currentDate);
    
    // Find rules with auto-check enabled
    List<ComplianceRule> findByAutoCheckEnabledTrue();
    List<ComplianceRule> findByOrganizationAndAutoCheckEnabledTrue(Organization organization);
    
    // Find mandatory rules
    @Query("SELECT r FROM ComplianceRule r WHERE r.ruleType = 'MANDATORY'")
    List<ComplianceRule> findMandatoryRules();
    
    // Find mandatory rules by organization
    @Query("SELECT r FROM ComplianceRule r WHERE r.organization = :organization AND r.ruleType = 'MANDATORY'")
    List<ComplianceRule> findMandatoryRulesByOrganization(@Param("organization") Organization organization);
    
    // Find critical rules
    @Query("SELECT r FROM ComplianceRule r WHERE r.severity = 'CRITICAL'")
    List<ComplianceRule> findCriticalRules();
    
    // Find critical rules by organization
    @Query("SELECT r FROM ComplianceRule r WHERE r.organization = :organization AND r.severity = 'CRITICAL'")
    List<ComplianceRule> findCriticalRulesByOrganization(@Param("organization") Organization organization);
    
    // Find rules needing periodic check
    @Query("SELECT r FROM ComplianceRule r WHERE r.autoCheckEnabled = true AND r.checkFrequencyDays IS NOT NULL AND r.checkFrequencyDays > 0")
    List<ComplianceRule> findRulesNeedingPeriodicCheck();
    
    // Find rules by organization needing periodic check
    @Query("SELECT r FROM ComplianceRule r WHERE r.organization = :organization AND " +
           "r.autoCheckEnabled = true AND r.checkFrequencyDays IS NOT NULL AND r.checkFrequencyDays > 0")
    List<ComplianceRule> findRulesNeedingPeriodicCheckByOrganization(@Param("organization") Organization organization);
    
    // Find system rules
    List<ComplianceRule> findByIsSystemRuleTrue();
    
    // Find custom rules (non-system rules)
    List<ComplianceRule> findByIsSystemRuleFalse();
    List<ComplianceRule> findByOrganizationAndIsSystemRuleFalse(Organization organization);
    
    // Search rules by name or description
    @Query("SELECT r FROM ComplianceRule r WHERE " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ComplianceRule> findByNameOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, 
                                                                    Pageable pageable);
    
    // Search rules by organization and name or description
    @Query("SELECT r FROM ComplianceRule r WHERE r.organization = :organization AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ComplianceRule> findByOrganizationAndNameOrDescriptionContainingIgnoreCase(@Param("organization") Organization organization,
                                                                                   @Param("searchTerm") String searchTerm, 
                                                                                   Pageable pageable);
    
    // Find rules expiring soon
    @Query("SELECT r FROM ComplianceRule r WHERE r.expirationDate IS NOT NULL AND " +
           "r.expirationDate BETWEEN :currentDate AND :endDate")
    List<ComplianceRule> findRulesExpiringSoon(@Param("currentDate") LocalDate currentDate, 
                                              @Param("endDate") LocalDate endDate);
    
    // Count rules by organization
    long countByOrganization(Organization organization);
    
    // Count active rules by organization
    @Query("SELECT COUNT(r) FROM ComplianceRule r WHERE r.organization = :organization AND " +
           "(r.effectiveDate IS NULL OR r.effectiveDate <= :currentDate) AND " +
           "(r.expirationDate IS NULL OR r.expirationDate >= :currentDate)")
    long countActiveRulesByOrganization(@Param("organization") Organization organization, 
                                       @Param("currentDate") LocalDate currentDate);
    
    // Check if rule code exists
    boolean existsByRuleCode(String ruleCode);
}

