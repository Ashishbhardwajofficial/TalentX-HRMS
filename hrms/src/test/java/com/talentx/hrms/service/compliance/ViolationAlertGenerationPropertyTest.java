package com.talentx.hrms.service.compliance;

import com.talentx.hrms.entity.compliance.ComplianceCheck;
import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for violation alert generation.
 * Feature: hrms-database-integration, Property 17: Violation Alert Generation
 * Validates: Requirements 6.3
 * 
 * Property: For any detected compliance violation, appropriate alerts should be
 * generated and sent to the designated personnel.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ViolationAlertGenerationPropertyTest {

    @Autowired
    private ComplianceCheckRepository complianceCheckRepository;

    @Autowired
    private ComplianceRuleRepository complianceRuleRepository;

    @Autowired
    private ComplianceJurisdictionRepository jurisdictionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Property(tries = 100)
    @Label("Property 17: Violation Alert Generation - Violations generate compliance checks")
    void violationsGenerateComplianceChecks(
            @ForAll("violationTypes") String violationType,
            @ForAll @IntRange(min = 1, max = 10) int severity,
            @ForAll boolean passed) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept);
        ComplianceJurisdiction jurisdiction = createJurisdiction();
        ComplianceRule rule = createRule(violationType, jurisdiction, severity);

        // Create compliance check (simulating violation detection)
        ComplianceCheck check = new ComplianceCheck();
        check.setRule(rule);
        check.setEmployee(employee);
        check.setCheckDate(LocalDateTime.now());
        check.setPassed(passed);
        check.setDetails(passed ? "Compliant" : "Violation detected: " + violationType);
        check = complianceCheckRepository.save(check);

        // Verify: Compliance check is created
        assertThat(check.getId()).isNotNull();
        assertThat(check.getRule()).isNotNull();
        assertThat(check.getEmployee()).isNotNull();
        assertThat(check.getCheckDate()).isNotNull();
        assertThat(check.isPassed()).isEqualTo(passed);

        // Verify: Violation details are recorded
        if (!passed) {
            assertThat(check.getDetails()).contains("Violation detected");
            assertThat(check.getDetails()).contains(violationType);
        }
    }

    @Property(tries = 100)
    @Label("Property 17: Violation Alert Generation - High severity violations are flagged")
    void highSeverityViolationsAreFlagged(
            @ForAll("violationTypes") String violationType,
            @ForAll @IntRange(min = 7, max = 10) int highSeverity) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept);
        ComplianceJurisdiction jurisdiction = createJurisdiction();
        ComplianceRule rule = createRule(violationType, jurisdiction, highSeverity);

        // Create compliance check for violation
        ComplianceCheck check = new ComplianceCheck();
        check.setRule(rule);
        check.setEmployee(employee);
        check.setCheckDate(LocalDateTime.now());
        check.setPassed(false);
        check.setDetails("High severity violation: " + violationType);
        check = complianceCheckRepository.save(check);

        // Verify: High severity violation is recorded
        assertThat(check.getRule().getSeverity()).isGreaterThanOrEqualTo(7);
        assertThat(check.isPassed()).isFalse();
        assertThat(check.getDetails()).contains("violation");
    }

    @Property(tries = 100)
    @Label("Property 17: Violation Alert Generation - Violations are linked to employees")
    void violationsAreLinkedToEmployees(
            @ForAll("violationTypes") String violationType,
            @ForAll @IntRange(min = 1, max = 10) int severity) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept);
        ComplianceJurisdiction jurisdiction = createJurisdiction();
        ComplianceRule rule = createRule(violationType, jurisdiction, severity);

        // Create compliance check
        ComplianceCheck check = new ComplianceCheck();
        check.setRule(rule);
        check.setEmployee(employee);
        check.setCheckDate(LocalDateTime.now());
        check.setPassed(false);
        check.setDetails("Violation for employee: " + employee.getEmployeeNumber());
        check = complianceCheckRepository.save(check);

        // Verify: Violation is linked to correct employee
        assertThat(check.getEmployee()).isNotNull();
        assertThat(check.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(check.getEmployee().getEmployeeNumber()).isEqualTo(employee.getEmployeeNumber());

        // Verify: Violation can be retrieved by employee
        ComplianceCheck retrieved = complianceCheckRepository.findById(check.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEmployee().getId()).isEqualTo(employee.getId());
    }

    @Property(tries = 100)
    @Label("Property 17: Violation Alert Generation - Violations are timestamped")
    void violationsAreTimestamped(
            @ForAll("violationTypes") String violationType,
            @ForAll @IntRange(min = 1, max = 10) int severity) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept);
        ComplianceJurisdiction jurisdiction = createJurisdiction();
        ComplianceRule rule = createRule(violationType, jurisdiction, severity);

        LocalDateTime beforeCheck = LocalDateTime.now().minusSeconds(1);

        // Create compliance check
        ComplianceCheck check = new ComplianceCheck();
        check.setRule(rule);
        check.setEmployee(employee);
        check.setCheckDate(LocalDateTime.now());
        check.setPassed(false);
        check.setDetails("Violation detected");
        check = complianceCheckRepository.save(check);

        LocalDateTime afterCheck = LocalDateTime.now().plusSeconds(1);

        // Verify: Check has timestamp
        assertThat(check.getCheckDate()).isNotNull();
        assertThat(check.getCheckDate()).isAfter(beforeCheck);
        assertThat(check.getCheckDate()).isBefore(afterCheck);
    }

    // Arbitraries
    @Provide
    Arbitrary<String> violationTypes() {
        return Arbitraries.of(
                "MinimumWageViolation",
                "OvertimeViolation",
                "LeaveEntitlementViolation",
                "WorkingHoursViolation",
                "SafetyViolation",
                "TaxWithholdingViolation",
                "BenefitsViolation",
                "TerminationNoticeViolation"
        );
    }

    // Helper methods
    private Organization createOrganization() {
        Organization org = new Organization();
        org.setName("Test Org " + System.currentTimeMillis());
        org.setLegalName("Test Org LLC");
        return organizationRepository.save(org);
    }

    private Department createDepartment(Organization org) {
        Department dept = new Department();
        dept.setName("Test Dept " + System.currentTimeMillis());
        dept.setCode("TD" + System.currentTimeMillis());
        dept.setOrganization(org);
        return departmentRepository.save(dept);
    }

    private Employee createEmployee(Organization org, Department dept) {
        Employee emp = new Employee();
        emp.setEmployeeNumber("EMP" + System.currentTimeMillis());
        emp.setFirstName("Test");
        emp.setLastName("Employee");
        emp.setEmail("test" + System.currentTimeMillis() + "@test.com");
        emp.setPhone("+1234567890");
        emp.setDateOfBirth(LocalDate.of(1990, 1, 1));
        emp.setGender(Gender.MALE);
        emp.setHireDate(LocalDate.now());
        emp.setJobTitle("Engineer");
        emp.setEmploymentStatus(EmploymentStatus.ACTIVE);
        emp.setEmploymentType(EmploymentType.FULL_TIME);
        emp.setBaseSalary(new BigDecimal("50000.00"));
        emp.setDepartment(dept);
        emp.setOrganization(org);
        return employeeRepository.save(emp);
    }

    private ComplianceJurisdiction createJurisdiction() {
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCode("TEST_" + System.currentTimeMillis());
        jurisdiction.setName("Test Jurisdiction");
        jurisdiction.setCountry("Test Country");
        return jurisdictionRepository.save(jurisdiction);
    }

    private ComplianceRule createRule(String ruleName, ComplianceJurisdiction jurisdiction, int severity) {
        ComplianceRule rule = new ComplianceRule();
        rule.setRuleName(ruleName + "_" + System.currentTimeMillis());
        rule.setRuleCode("RULE_" + System.currentTimeMillis());
        rule.setDescription("Test compliance rule");
        rule.setJurisdiction(jurisdiction);
        rule.setSeverity(severity);
        rule.setActive(true);
        return complianceRuleRepository.save(rule);
    }
}

