package com.talentx.hrms.integration;

import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.compliance.ComplianceJurisdictionCreateDTO;
import com.talentx.hrms.dto.compliance.ComplianceRuleCreateDTO;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import com.talentx.hrms.entity.compliance.ComplianceRule;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.*;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete compliance workflow integration test
 * Tests Requirements: 7.1, 7.2, 7.5 - Compliance management workflow
 * 
 * **Feature: hrms-frontend-complete-integration, Property 4: Complete Compliance Workflow**
 * Tests the complete compliance workflow: Create rule → Run check → Detect violation → Resolve
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompleteComplianceWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ComplianceJurisdictionRepository complianceJurisdictionRepository;

    @Autowired
    private ComplianceRuleRepository complianceRuleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization testOrganization;
    private Department testDepartment;
    private User adminUser;
    private User complianceOfficerUser;
    private String adminToken;
    private String complianceToken;
    private Employee testEmployee;
    private ComplianceJurisdiction testJurisdiction;

    @BeforeEach
    public void setup() throws Exception {
        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setLegalName("Test Organization LLC");
        testOrganization.setCompanySize(CompanySize.MEDIUM);
        testOrganization.setSubscriptionTier(SubscriptionTier.PROFESSIONAL);
        testOrganization.setActive(true);
        testOrganization = organizationRepository.save(testOrganization);

        // Create test department
        testDepartment = new Department();
        testDepartment.setName("Engineering");
        testDepartment.setCode("ENG");
        testDepartment.setOrganization(testOrganization);
        testDepartment = departmentRepository.save(testDepartment);

        // Create users
        adminUser = createUser("admin", "admin@test.com", "ADMIN");
        complianceOfficerUser = createUser("compliance", "compliance@test.com", "COMPLIANCE_OFFICER");

        // Create test employee
        testEmployee = createTestEmployee();

        // Create test jurisdiction
        testJurisdiction = createTestJurisdiction();

        // Authenticate users
        adminToken = authenticateAndGetToken("admin", "password123");
        complianceToken = authenticateAndGetToken("compliance", "password123");
    }

    @Test
    public void testCompleteComplianceWorkflow() throws Exception {
        // Step 1: Create compliance jurisdiction
        ComplianceJurisdictionCreateDTO jurisdictionRequest = new ComplianceJurisdictionCreateDTO();
        jurisdictionRequest.setCountryCode("US");
        jurisdictionRequest.setStateProvinceCode("CA");
        jurisdictionRequest.setName("California, United States");
        jurisdictionRequest.setJurisdictionType(JurisdictionType.STATE);

        MvcResult jurisdictionResult = mockMvc.perform(post("/api/compliance/jurisdictions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jurisdictionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.countryCode").value("US"))
                .andExpect(jsonPath("$.data.stateProvinceCode").value("CA"))
                .andExpect(jsonPath("$.data.name").value("California, United States"))
                .andExpect(jsonPath("$.data.jurisdictionType").value("STATE"))
                .andReturn();

        Long jurisdictionId = extractJurisdictionId(jurisdictionResult);
        assertThat(jurisdictionId).isNotNull();

        // Step 2: Create compliance rule
        ComplianceRuleCreateDTO ruleRequest = new ComplianceRuleCreateDTO();
        ruleRequest.setJurisdictionId(jurisdictionId);
        ruleRequest.setRuleCategory(RuleCategory.WORKING_HOURS);
        ruleRequest.setRuleName("Maximum Weekly Working Hours");
        ruleRequest.setDescription("Employees cannot work more than 40 hours per week without overtime approval");
        
        // Create rule data for working hours limit
        Map<String, Object> ruleData = new HashMap<>();
        ruleData.put("maxWeeklyHours", 40);
        ruleData.put("requiresOvertimeApproval", true);
        ruleRequest.setRuleData(ruleData);
        
        ruleRequest.setEffectiveDate(LocalDate.now());
        ruleRequest.setSourceUrl("https://www.dir.ca.gov/dlse/faq_overtime.htm");

        MvcResult ruleResult = mockMvc.perform(post("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jurisdictionId").value(jurisdictionId))
                .andExpect(jsonPath("$.data.ruleCategory").value("WORKING_HOURS"))
                .andExpect(jsonPath("$.data.ruleName").value("Maximum Weekly Working Hours"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andReturn();

        Long ruleId = extractRuleId(ruleResult);
        assertThat(ruleId).isNotNull();

        // Step 3: Run compliance check (simulate system running checks)
        Map<String, Object> checkRequest = new HashMap<>();
        checkRequest.put("ruleId", ruleId);
        checkRequest.put("employeeId", testEmployee.getId());
        checkRequest.put("checkDate", LocalDate.now().toString());

        MvcResult checkResult = mockMvc.perform(post("/api/compliance/checks/run")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ruleId").value(ruleId))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andReturn();

        Long checkId = extractCheckId(checkResult);
        String checkStatus = extractCheckStatus(checkResult);
        
        // Step 4: If violation detected, resolve it
        if ("NON_COMPLIANT".equals(checkStatus)) {
            // Simulate violation detected - resolve it
            Map<String, Object> resolveRequest = new HashMap<>();
            resolveRequest.put("resolutionNotes", "Overtime hours approved by manager. Employee compensated with time off.");
            resolveRequest.put("resolvedBy", complianceOfficerUser.getId());

            mockMvc.perform(put("/api/compliance/checks/" + checkId + "/resolve")
                    .header("Authorization", "Bearer " + complianceToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resolveRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.resolved").value(true))
                    .andExpect(jsonPath("$.data.resolvedBy").value(complianceOfficerUser.getId()))
                    .andExpect(jsonPath("$.data.resolutionNotes").value("Overtime hours approved by manager. Employee compensated with time off."));
        }

        // Step 5: Verify complete workflow - check all created entities
        // Verify jurisdiction exists
        mockMvc.perform(get("/api/compliance/jurisdictions")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.id == " + jurisdictionId + ")].name").value("California, United States"));

        // Verify rule exists
        mockMvc.perform(get("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .param("jurisdictionId", jurisdictionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(ruleId))
                .andExpect(jsonPath("$.data[0].ruleName").value("Maximum Weekly Working Hours"));

        // Verify compliance check exists
        mockMvc.perform(get("/api/compliance/checks")
                .header("Authorization", "Bearer " + complianceToken)
                .param("employeeId", testEmployee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(checkId))
                .andExpect(jsonPath("$.data[0].ruleId").value(ruleId));
    }

    @Test
    public void testMultipleComplianceRulesWorkflow() throws Exception {
        // Create multiple compliance rules for different categories
        
        // Rule 1: Minimum wage compliance
        ComplianceRuleCreateDTO minWageRule = new ComplianceRuleCreateDTO();
        minWageRule.setJurisdictionId(testJurisdiction.getId());
        minWageRule.setRuleCategory(RuleCategory.MINIMUM_WAGE);
        minWageRule.setRuleName("California Minimum Wage");
        minWageRule.setDescription("Minimum wage must be at least $16.00 per hour");
        
        Map<String, Object> minWageData = new HashMap<>();
        minWageData.put("minimumHourlyRate", 16.00);
        minWageRule.setRuleData(minWageData);
        minWageRule.setEffectiveDate(LocalDate.now());

        MvcResult minWageResult = mockMvc.perform(post("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minWageRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleCategory").value("MINIMUM_WAGE"))
                .andReturn();

        Long minWageRuleId = extractRuleId(minWageResult);

        // Rule 2: Leave entitlement compliance
        ComplianceRuleCreateDTO leaveRule = new ComplianceRuleCreateDTO();
        leaveRule.setJurisdictionId(testJurisdiction.getId());
        leaveRule.setRuleCategory(RuleCategory.LEAVE_ENTITLEMENT);
        leaveRule.setRuleName("Paid Sick Leave Entitlement");
        leaveRule.setDescription("Employees must accrue at least 1 hour of paid sick leave for every 30 hours worked");
        
        Map<String, Object> leaveData = new HashMap<>();
        leaveData.put("accrualRate", 1.0/30.0); // 1 hour per 30 hours worked
        leaveData.put("minimumAnnualHours", 24);
        leaveRule.setRuleData(leaveData);
        leaveRule.setEffectiveDate(LocalDate.now());

        MvcResult leaveResult = mockMvc.perform(post("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleCategory").value("LEAVE_ENTITLEMENT"))
                .andReturn();

        Long leaveRuleId = extractRuleId(leaveResult);

        // Run checks for both rules
        Map<String, Object> minWageCheckRequest = new HashMap<>();
        minWageCheckRequest.put("ruleId", minWageRuleId);
        minWageCheckRequest.put("employeeId", testEmployee.getId());
        minWageCheckRequest.put("checkDate", LocalDate.now().toString());

        mockMvc.perform(post("/api/compliance/checks/run")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minWageCheckRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleId").value(minWageRuleId));

        Map<String, Object> leaveCheckRequest = new HashMap<>();
        leaveCheckRequest.put("ruleId", leaveRuleId);
        leaveCheckRequest.put("employeeId", testEmployee.getId());
        leaveCheckRequest.put("checkDate", LocalDate.now().toString());

        mockMvc.perform(post("/api/compliance/checks/run")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveCheckRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleId").value(leaveRuleId));

        // Verify all compliance checks
        mockMvc.perform(get("/api/compliance/checks")
                .header("Authorization", "Bearer " + complianceToken)
                .param("employeeId", testEmployee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    public void testComplianceWorkflowValidation() throws Exception {
        // Test 1: Create jurisdiction with invalid data
        ComplianceJurisdictionCreateDTO invalidJurisdictionRequest = new ComplianceJurisdictionCreateDTO();
        invalidJurisdictionRequest.setCountryCode(""); // Empty country code
        invalidJurisdictionRequest.setName("Invalid Jurisdiction");

        mockMvc.perform(post("/api/compliance/jurisdictions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidJurisdictionRequest)))
                .andExpect(status().isBadRequest());

        // Test 2: Create rule with non-existent jurisdiction
        ComplianceRuleCreateDTO invalidRuleRequest = new ComplianceRuleCreateDTO();
        invalidRuleRequest.setJurisdictionId(999L); // Non-existent jurisdiction
        invalidRuleRequest.setRuleCategory(RuleCategory.WORKING_HOURS);
        invalidRuleRequest.setRuleName("Invalid Rule");

        mockMvc.perform(post("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRuleRequest)))
                .andExpect(status().isNotFound());

        // Test 3: Run check with non-existent rule
        Map<String, Object> invalidCheckRequest = new HashMap<>();
        invalidCheckRequest.put("ruleId", 999L); // Non-existent rule
        invalidCheckRequest.put("employeeId", testEmployee.getId());

        mockMvc.perform(post("/api/compliance/checks/run")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCheckRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testComplianceReportingAndAlerts() throws Exception {
        // Create a rule that will likely trigger violations
        ComplianceRuleCreateDTO criticalRule = new ComplianceRuleCreateDTO();
        criticalRule.setJurisdictionId(testJurisdiction.getId());
        criticalRule.setRuleCategory(RuleCategory.SAFETY);
        criticalRule.setRuleName("Safety Training Compliance");
        criticalRule.setDescription("All employees must complete safety training within 30 days of hire");
        
        Map<String, Object> safetyData = new HashMap<>();
        safetyData.put("maxDaysFromHire", 30);
        safetyData.put("requiredTrainingType", "SAFETY");
        criticalRule.setRuleData(safetyData);
        criticalRule.setEffectiveDate(LocalDate.now());

        MvcResult ruleResult = mockMvc.perform(post("/api/compliance/rules")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criticalRule)))
                .andExpect(status().isOk())
                .andReturn();

        Long ruleId = extractRuleId(ruleResult);

        // Run compliance check
        Map<String, Object> checkRequest = new HashMap<>();
        checkRequest.put("ruleId", ruleId);
        checkRequest.put("employeeId", testEmployee.getId());
        checkRequest.put("checkDate", LocalDate.now().toString());

        MvcResult checkResult = mockMvc.perform(post("/api/compliance/checks/run")
                .header("Authorization", "Bearer " + complianceToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Get compliance dashboard/summary
        mockMvc.perform(get("/api/compliance/checks")
                .header("Authorization", "Bearer " + complianceToken)
                .param("status", "NON_COMPLIANT")
                .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Get compliance checks by rule category
        mockMvc.perform(get("/api/compliance/checks")
                .header("Authorization", "Bearer " + complianceToken)
                .param("ruleCategory", "SAFETY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // Helper methods

    private User createUser(String username, String email, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(true);
        user.setOrganization(testOrganization);
        
        Role role = new Role();
        role.setName(roleName);
        role.setDescription(roleName + " role");
        role.setOrganization(testOrganization);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    private Employee createTestEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@test.com");
        employee.setPhoneNumber("+1234567890");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now().minusDays(45)); // Hired 45 days ago
        employee.setJobTitle("Software Engineer");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setDepartment(testDepartment);
        employee.setOrganization(testOrganization);
        return employeeRepository.save(employee);
    }

    private ComplianceJurisdiction createTestJurisdiction() {
        ComplianceJurisdiction jurisdiction = new ComplianceJurisdiction();
        jurisdiction.setCountryCode("US");
        jurisdiction.setStateProvinceCode("CA");
        jurisdiction.setName("California, United States");
        jurisdiction.setJurisdictionType(JurisdictionType.STATE);
        jurisdiction.setActive(true);
        return complianceJurisdictionRepository.save(jurisdiction);
    }

    private String authenticateAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("token")
                .asText();
    }

    private Long extractJurisdictionId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractRuleId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractCheckId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private String extractCheckStatus(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("status")
                .asText();
    }
}

