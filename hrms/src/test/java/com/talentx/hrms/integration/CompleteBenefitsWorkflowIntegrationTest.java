package com.talentx.hrms.integration;

import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.benefits.BenefitPlanCreateDTO;
import com.talentx.hrms.dto.benefits.EmployeeBenefitCreateDTO;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.entity.benefits.BenefitPlan;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete benefits workflow integration test
 * Tests Requirements: 11.1, 11.2 - Benefits administration workflow
 * 
 * **Feature: hrms-frontend-complete-integration, Property 5: Complete Benefits Workflow**
 * Tests the complete benefits workflow: Create plan → Enroll employee → Manage coverage
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompleteBenefitsWorkflowIntegrationTest {

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
    private BenefitPlanRepository benefitPlanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization testOrganization;
    private Department testDepartment;
    private User adminUser;
    private User hrUser;
    private String adminToken;
    private String hrToken;
    private Employee testEmployee;
    private Employee testEmployee2;

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
        hrUser = createUser("hr", "hr@test.com", "HR_MANAGER");

        // Create test employees
        testEmployee = createTestEmployee("EMP001", "John", "Doe");
        testEmployee2 = createTestEmployee("EMP002", "Jane", "Smith");

        // Authenticate users
        adminToken = authenticateAndGetToken("admin", "password123");
        hrToken = authenticateAndGetToken("hr", "password123");
    }

    @Test
    public void testCompleteBenefitsWorkflow() throws Exception {
        // Step 1: Create health insurance benefit plan
        BenefitPlanCreateDTO healthPlanRequest = new BenefitPlanCreateDTO();
        healthPlanRequest.setName("Premium Health Insurance");
        healthPlanRequest.setPlanType(BenefitPlanType.HEALTH_INSURANCE);
        healthPlanRequest.setDescription("Comprehensive health insurance coverage with dental and vision");
        healthPlanRequest.setProvider("Blue Cross Blue Shield");
        healthPlanRequest.setEmployeeCost(new BigDecimal("150.00"));
        healthPlanRequest.setEmployerCost(new BigDecimal("450.00"));
        healthPlanRequest.setCostFrequency(CostFrequency.MONTHLY);
        healthPlanRequest.setEffectiveDate(LocalDate.now());
        healthPlanRequest.setExpiryDate(LocalDate.now().plusYears(1));
        healthPlanRequest.setOrganizationId(testOrganization.getId());

        MvcResult healthPlanResult = mockMvc.perform(post("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(healthPlanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Premium Health Insurance"))
                .andExpect(jsonPath("$.data.planType").value("HEALTH_INSURANCE"))
                .andExpect(jsonPath("$.data.provider").value("Blue Cross Blue Shield"))
                .andExpect(jsonPath("$.data.employeeCost").value(150.00))
                .andExpect(jsonPath("$.data.employerCost").value(450.00))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andReturn();

        Long healthPlanId = extractPlanId(healthPlanResult);
        assertThat(healthPlanId).isNotNull();

        // Step 2: Create retirement benefit plan
        BenefitPlanCreateDTO retirementPlanRequest = new BenefitPlanCreateDTO();
        retirementPlanRequest.setName("401(k) Retirement Plan");
        retirementPlanRequest.setPlanType(BenefitPlanType.RETIREMENT);
        retirementPlanRequest.setDescription("401(k) retirement savings plan with company matching");
        retirementPlanRequest.setProvider("Fidelity Investments");
        retirementPlanRequest.setEmployeeCost(new BigDecimal("0.00")); // No employee cost for basic plan
        retirementPlanRequest.setEmployerCost(new BigDecimal("200.00")); // Company matching contribution
        retirementPlanRequest.setCostFrequency(CostFrequency.MONTHLY);
        retirementPlanRequest.setEffectiveDate(LocalDate.now());
        retirementPlanRequest.setOrganizationId(testOrganization.getId());

        MvcResult retirementPlanResult = mockMvc.perform(post("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(retirementPlanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planType").value("RETIREMENT"))
                .andReturn();

        Long retirementPlanId = extractPlanId(retirementPlanResult);

        // Step 3: Enroll employee in health insurance with family coverage
        EmployeeBenefitCreateDTO healthEnrollmentRequest = new EmployeeBenefitCreateDTO();
        healthEnrollmentRequest.setEmployeeId(testEmployee.getId());
        healthEnrollmentRequest.setBenefitPlanId(healthPlanId);
        healthEnrollmentRequest.setEnrollmentDate(LocalDate.now());
        healthEnrollmentRequest.setEffectiveDate(LocalDate.now().plusDays(1));
        healthEnrollmentRequest.setCoverageLevel(CoverageLevel.FAMILY);
        
        // Add beneficiaries
        Map<String, Object> beneficiaries = new HashMap<>();
        beneficiaries.put("spouse", Map.of("name", "Jane Doe", "relationship", "Spouse", "dateOfBirth", "1992-05-15"));
        beneficiaries.put("child1", Map.of("name", "Jimmy Doe", "relationship", "Child", "dateOfBirth", "2015-08-20"));
        healthEnrollmentRequest.setBeneficiaries(beneficiaries);

        MvcResult healthEnrollmentResult = mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(healthEnrollmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.benefitPlanId").value(healthPlanId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.coverageLevel").value("FAMILY"))
                .andReturn();

        Long healthEnrollmentId = extractEnrollmentId(healthEnrollmentResult);

        // Step 4: Enroll employee in retirement plan
        EmployeeBenefitCreateDTO retirementEnrollmentRequest = new EmployeeBenefitCreateDTO();
        retirementEnrollmentRequest.setEmployeeId(testEmployee.getId());
        retirementEnrollmentRequest.setBenefitPlanId(retirementPlanId);
        retirementEnrollmentRequest.setEnrollmentDate(LocalDate.now());
        retirementEnrollmentRequest.setEffectiveDate(LocalDate.now().plusDays(1));
        retirementEnrollmentRequest.setCoverageLevel(CoverageLevel.EMPLOYEE_ONLY);

        MvcResult retirementEnrollmentResult = mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(retirementEnrollmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverageLevel").value("EMPLOYEE_ONLY"))
                .andReturn();

        Long retirementEnrollmentId = extractEnrollmentId(retirementEnrollmentResult);

        // Step 5: Enroll second employee in health insurance with employee-only coverage
        EmployeeBenefitCreateDTO employee2EnrollmentRequest = new EmployeeBenefitCreateDTO();
        employee2EnrollmentRequest.setEmployeeId(testEmployee2.getId());
        employee2EnrollmentRequest.setBenefitPlanId(healthPlanId);
        employee2EnrollmentRequest.setEnrollmentDate(LocalDate.now());
        employee2EnrollmentRequest.setEffectiveDate(LocalDate.now().plusDays(1));
        employee2EnrollmentRequest.setCoverageLevel(CoverageLevel.EMPLOYEE_ONLY);

        mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee2EnrollmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee2.getId()));

        // Step 6: Update coverage level (change from family to employee+spouse)
        mockMvc.perform(put("/api/benefits/enrollments/" + healthEnrollmentId)
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"coverageLevel\": \"EMPLOYEE_SPOUSE\", \"beneficiaries\": {\"spouse\": {\"name\": \"Jane Doe\", \"relationship\": \"Spouse\", \"dateOfBirth\": \"1992-05-15\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.coverageLevel").value("EMPLOYEE_SPOUSE"));

        // Step 7: Suspend benefit enrollment (temporary suspension)
        mockMvc.perform(put("/api/benefits/enrollments/" + retirementEnrollmentId)
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        // Step 8: Reactivate suspended benefit
        mockMvc.perform(put("/api/benefits/enrollments/" + retirementEnrollmentId)
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Step 9: Verify complete workflow - check all created entities
        // Verify benefit plans exist
        mockMvc.perform(get("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.id == " + healthPlanId + ")].name").value("Premium Health Insurance"))
                .andExpect(jsonPath("$.data[?(@.id == " + retirementPlanId + ")].name").value("401(k) Retirement Plan"));

        // Verify employee enrollments
        mockMvc.perform(get("/api/benefits/enrollments/employee/" + testEmployee.getId())
                .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)) // Health and retirement
                .andExpect(jsonPath("$.data[?(@.benefitPlanId == " + healthPlanId + ")].coverageLevel").value("EMPLOYEE_SPOUSE"))
                .andExpect(jsonPath("$.data[?(@.benefitPlanId == " + retirementPlanId + ")].status").value("ACTIVE"));

        // Verify second employee enrollment
        mockMvc.perform(get("/api/benefits/enrollments/employee/" + testEmployee2.getId())
                .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1)) // Only health insurance
                .andExpect(jsonPath("$.data[0].coverageLevel").value("EMPLOYEE_ONLY"));

        // Verify benefit plan enrollment statistics
        mockMvc.perform(get("/api/benefits/plans/" + healthPlanId + "/enrollments")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)); // Two employees enrolled
    }

    @Test
    public void testEmployeeTerminationBenefitsWorkflow() throws Exception {
        // Step 1: Create benefit plan
        BenefitPlanCreateDTO planRequest = new BenefitPlanCreateDTO();
        planRequest.setName("Life Insurance");
        planRequest.setPlanType(BenefitPlanType.LIFE_INSURANCE);
        planRequest.setDescription("Basic life insurance coverage");
        planRequest.setProvider("MetLife");
        planRequest.setEmployeeCost(new BigDecimal("25.00"));
        planRequest.setEmployerCost(new BigDecimal("75.00"));
        planRequest.setCostFrequency(CostFrequency.MONTHLY);
        planRequest.setEffectiveDate(LocalDate.now());
        planRequest.setOrganizationId(testOrganization.getId());

        MvcResult planResult = mockMvc.perform(post("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Long planId = extractPlanId(planResult);

        // Step 2: Enroll employee
        EmployeeBenefitCreateDTO enrollmentRequest = new EmployeeBenefitCreateDTO();
        enrollmentRequest.setEmployeeId(testEmployee.getId());
        enrollmentRequest.setBenefitPlanId(planId);
        enrollmentRequest.setEnrollmentDate(LocalDate.now());
        enrollmentRequest.setEffectiveDate(LocalDate.now());
        enrollmentRequest.setCoverageLevel(CoverageLevel.EMPLOYEE_ONLY);

        MvcResult enrollmentResult = mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        Long enrollmentId = extractEnrollmentId(enrollmentResult);

        // Step 3: Terminate employee benefits (simulate employee termination)
        mockMvc.perform(put("/api/benefits/enrollments/" + enrollmentId + "/terminate")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"terminationDate\": \"" + LocalDate.now().toString() + "\", \"reason\": \"Employee termination\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TERMINATED"))
                .andExpect(jsonPath("$.data.terminationDate").value(LocalDate.now().toString()));

        // Verify terminated enrollment
        mockMvc.perform(get("/api/benefits/enrollments/employee/" + testEmployee.getId())
                .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("TERMINATED"));
    }

    @Test
    public void testBenefitsWorkflowValidation() throws Exception {
        // Test 1: Create benefit plan with invalid data
        BenefitPlanCreateDTO invalidPlanRequest = new BenefitPlanCreateDTO();
        invalidPlanRequest.setName(""); // Empty name
        invalidPlanRequest.setEmployeeCost(new BigDecimal("-50.00")); // Negative cost

        mockMvc.perform(post("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPlanRequest)))
                .andExpect(status().isBadRequest());

        // Test 2: Enroll in non-existent benefit plan
        EmployeeBenefitCreateDTO invalidEnrollmentRequest = new EmployeeBenefitCreateDTO();
        invalidEnrollmentRequest.setEmployeeId(testEmployee.getId());
        invalidEnrollmentRequest.setBenefitPlanId(999L); // Non-existent plan
        invalidEnrollmentRequest.setEnrollmentDate(LocalDate.now());

        mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEnrollmentRequest)))
                .andExpect(status().isNotFound());

        // Test 3: Enroll non-existent employee
        BenefitPlan validPlan = createTestBenefitPlan();
        
        EmployeeBenefitCreateDTO invalidEmployeeRequest = new EmployeeBenefitCreateDTO();
        invalidEmployeeRequest.setEmployeeId(999L); // Non-existent employee
        invalidEmployeeRequest.setBenefitPlanId(validPlan.getId());
        invalidEmployeeRequest.setEnrollmentDate(LocalDate.now());

        mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployeeRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testBenefitsCostCalculation() throws Exception {
        // Create benefit plan with different costs for different coverage levels
        BenefitPlanCreateDTO planRequest = new BenefitPlanCreateDTO();
        planRequest.setName("Flexible Health Plan");
        planRequest.setPlanType(BenefitPlanType.HEALTH_INSURANCE);
        planRequest.setDescription("Health plan with variable costs based on coverage");
        planRequest.setProvider("Aetna");
        planRequest.setEmployeeCost(new BigDecimal("100.00")); // Base cost for employee only
        planRequest.setEmployerCost(new BigDecimal("300.00"));
        planRequest.setCostFrequency(CostFrequency.MONTHLY);
        planRequest.setEffectiveDate(LocalDate.now());
        planRequest.setOrganizationId(testOrganization.getId());

        MvcResult planResult = mockMvc.perform(post("/api/benefits/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Long planId = extractPlanId(planResult);

        // Enroll with family coverage (should calculate higher costs)
        EmployeeBenefitCreateDTO familyEnrollmentRequest = new EmployeeBenefitCreateDTO();
        familyEnrollmentRequest.setEmployeeId(testEmployee.getId());
        familyEnrollmentRequest.setBenefitPlanId(planId);
        familyEnrollmentRequest.setEnrollmentDate(LocalDate.now());
        familyEnrollmentRequest.setEffectiveDate(LocalDate.now());
        familyEnrollmentRequest.setCoverageLevel(CoverageLevel.FAMILY);

        mockMvc.perform(post("/api/benefits/enrollments")
                .header("Authorization", "Bearer " + hrToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(familyEnrollmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverageLevel").value("FAMILY"));

        // Verify cost calculation endpoint
        mockMvc.perform(get("/api/benefits/enrollments/employee/" + testEmployee.getId() + "/costs")
                .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].employeeCost").exists())
                .andExpect(jsonPath("$.data[0].employerCost").exists());
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

    private Employee createTestEmployee(String empNumber, String firstName, String lastName) {
        Employee employee = new Employee();
        employee.setEmployeeNumber(empNumber);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
        employee.setPhoneNumber("+1234567890");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now().minusDays(30));
        employee.setJobTitle("Software Engineer");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setDepartment(testDepartment);
        employee.setOrganization(testOrganization);
        return employeeRepository.save(employee);
    }

    private BenefitPlan createTestBenefitPlan() {
        BenefitPlan plan = new BenefitPlan();
        plan.setName("Test Benefit Plan");
        plan.setPlanType(BenefitPlanType.HEALTH_INSURANCE);
        plan.setDescription("Test plan for validation");
        plan.setProvider("Test Provider");
        plan.setEmployeeCost(new BigDecimal("50.00"));
        plan.setEmployerCost(new BigDecimal("150.00"));
        plan.setCostFrequency(CostFrequency.MONTHLY);
        plan.setEffectiveDate(LocalDate.now());
        plan.setActive(true);
        plan.setOrganization(testOrganization);
        return benefitPlanRepository.save(plan);
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

    private Long extractPlanId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractEnrollmentId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }
}

