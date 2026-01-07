package com.talentx.hrms.service.compliance;

import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.repository.ComplianceJurisdictionRepository;
import com.talentx.hrms.repository.ComplianceRuleRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for compliance rule storage.
 * Feature: hrms-database-integration, Property 15: Compliance Rule Storage
 * Validates: Requirements 6.1
 * 
 * Property: For any compliance rule creation, the rule should be stored with correct
 * jurisdiction information and be retrievable for compliance checking.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ComplianceRuleStoragePropertyTest {

    @Autowired
    private ComplianceRuleRepository complianceRuleRepository;

    @Autowired
    private ComplianceJurisdictionRepository jurisdictionRepository;

    @Property(tries = 100)
    @Label("Property 15: Compliance Rule Storage - Rules are stored and retrievable")
    void complianceRulesAreStoredAndRetrievable(
            @ForAll("ruleNames") String ruleName,
            @ForAll("jurisdictionCodes") String jurisdictionCode,
            @ForAll @IntRange(min = 1, max = 10) int severity) {

        // Create jurisdiction
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCode(jurisdictionCode + "_" + System.currentTimeMillis());
        jurisdiction.setName("Jurisdiction " + jurisdictionCode);
        jurisdiction.setCountry("Test Country");
        jurisdiction = jurisdictionRepository.save(jurisdiction);

        // Create compliance rule
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleName(ruleName + "_" + System.currentTimeMillis());
        rule.setRuleCode("RULE_" + System.currentTimeMillis());
        rule.setDescription("Test compliance rule for " + ruleName);
        rule.setJurisdiction(jurisdiction);
        rule.setSeverity(severity);
        rule.setActive(true);
        rule = complianceRuleRepository.save(rule);

        // Verify: Rule is stored with ID
        assertThat(rule.getId()).isNotNull();
        assertThat(rule.getRuleName()).contains(ruleName);
        assertThat(rule.getRuleCode()).isNotBlank();

        // Verify: Rule has correct jurisdiction
        assertThat(rule.getJurisdiction()).isNotNull();
        assertThat(rule.getJurisdiction().getCode()).contains(jurisdictionCode);

        // Verify: Rule is retrievable
        ComplianceRule retrieved = complianceRuleRepository.findById(rule.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getRuleName()).isEqualTo(rule.getRuleName());
        assertThat(retrieved.getJurisdiction().getId()).isEqualTo(jurisdiction.getId());
    }

    @Property(tries = 100)
    @Label("Property 15: Compliance Rule Storage - Rules maintain jurisdiction relationship")
    void complianceRulesMaintainJurisdictionRelationship(
            @ForAll("ruleNames") String ruleName,
            @ForAll("jurisdictionCodes") String jurisdictionCode) {

        // Create jurisdiction
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCode(jurisdictionCode + "_" + System.currentTimeMillis());
        jurisdiction.setName("Jurisdiction " + jurisdictionCode);
        jurisdiction.setCountry("Test Country");
        jurisdiction = jurisdictionRepository.save(jurisdiction);

        Long jurisdictionId = jurisdiction.getId();

        // Create multiple rules for the same jurisdiction
        ComplianceRule rule1 = createRule(ruleName + "_1", jurisdiction);
        ComplianceRule rule2 = createRule(ruleName + "_2", jurisdiction);

        // Verify: Both rules reference the same jurisdiction
        assertThat(rule1.getJurisdiction().getId()).isEqualTo(jurisdictionId);
        assertThat(rule2.getJurisdiction().getId()).isEqualTo(jurisdictionId);

        // Verify: Jurisdiction can be retrieved from rules
        ComplianceRule retrieved1 = complianceRuleRepository.findById(rule1.getId()).orElse(null);
        ComplianceRule retrieved2 = complianceRuleRepository.findById(rule2.getId()).orElse(null);

        assertThat(retrieved1).isNotNull();
        assertThat(retrieved2).isNotNull();
        assertThat(retrieved1.getJurisdiction().getId()).isEqualTo(jurisdictionId);
        assertThat(retrieved2.getJurisdiction().getId()).isEqualTo(jurisdictionId);
    }

    @Property(tries = 100)
    @Label("Property 15: Compliance Rule Storage - Active rules can be filtered")
    void activeRulesCanBeFiltered(
            @ForAll("ruleNames") String ruleName,
            @ForAll("jurisdictionCodes") String jurisdictionCode,
            @ForAll boolean isActive) {

        // Create jurisdiction
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCode(jurisdictionCode + "_" + System.currentTimeMillis());
        jurisdiction.setName("Jurisdiction " + jurisdictionCode);
        jurisdiction.setCountry("Test Country");
        jurisdiction = jurisdictionRepository.save(jurisdiction);

        // Create compliance rule with specified active status
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleName(ruleName + "_" + System.currentTimeMillis());
        rule.setRuleCode("RULE_" + System.currentTimeMillis());
        rule.setDescription("Test compliance rule");
        rule.setJurisdiction(jurisdiction);
        rule.setSeverity(5);
        rule.setActive(isActive);
        rule = complianceRuleRepository.save(rule);

        // Verify: Rule active status is preserved
        ComplianceRule retrieved = complianceRuleRepository.findById(rule.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.isActive()).isEqualTo(isActive);
    }

    @Property(tries = 100)
    @Label("Property 15: Compliance Rule Storage - Rule severity is preserved")
    void ruleSeverityIsPreserved(
            @ForAll("ruleNames") String ruleName,
            @ForAll("jurisdictionCodes") String jurisdictionCode,
            @ForAll @IntRange(min = 1, max = 10) int severity) {

        // Create jurisdiction
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCode(jurisdictionCode + "_" + System.currentTimeMillis());
        jurisdiction.setName("Jurisdiction " + jurisdictionCode);
        jurisdiction.setCountry("Test Country");
        jurisdiction = jurisdictionRepository.save(jurisdiction);

        // Create compliance rule with specified severity
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleName(ruleName + "_" + System.currentTimeMillis());
        rule.setRuleCode("RULE_" + System.currentTimeMillis());
        rule.setDescription("Test compliance rule");
        rule.setJurisdiction(jurisdiction);
        rule.setSeverity(severity);
        rule.setActive(true);
        rule = complianceRuleRepository.save(rule);

        // Verify: Severity is preserved
        ComplianceRule retrieved = complianceRuleRepository.findById(rule.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSeverity()).isEqualTo(severity);
        assertThat(retrieved.getSeverity()).isBetween(1, 10);
    }

    // Arbitraries
    @Provide
    Arbitrary<String> ruleNames() {
        return Arbitraries.of(
                "MinimumWage",
                "OvertimePayment",
                "LeaveEntitlement",
                "WorkingHours",
                "SafetyCompliance",
                "TaxWithholding",
                "BenefitsRequirement",
                "TerminationNotice"
        );
    }

    @Provide
    Arbitrary<String> jurisdictionCodes() {
        return Arbitraries.of(
                "US_CA",
                "US_NY",
                "US_TX",
                "UK",
                "EU_DE",
                "EU_FR",
                "AU",
                "CA"
        );
    }

    // Helper methods
    private ComplianceRule createRule(String ruleName, ComplianceJurisdiction jurisdiction) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleName(ruleName + "_" + System.currentTimeMillis());
        rule.setRuleCode("RULE_" + System.currentTimeMillis());
        rule.setDescription("Test compliance rule");
        rule.setJurisdiction(jurisdiction);
        rule.setSeverity(5);
        rule.setActive(true);
        return complianceRuleRepository.save(rule);
    }
}

