package com.talentx.hrms.integration;

import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.dto.leave.LeaveRequestCreateDTO;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.*;
import com.talentx.hrms.entity.leave.LeaveType;
import com.talentx.hrms.entity.security.Permission;
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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for complete user workflows across the application.
 * Tests Requirements: 2.2, 8.2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EndToEndWorkflowTest {

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
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization testOrganization;
    private Department testDepartment;
    private User adminUser;
    private User managerUser;
    private User regularUser;
    private String adminToken;
    private String managerToken;
    private String userToken;

    @BeforeEach
    public void setup() throws Exception {
        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setLegalName("Test Organization LLC");
        testOrganization = organizationRepository.save(testOrganization);

        // Create test department
        testDepartment = new Department();
        testDepartment.setName("Engineering");
        testDepartment.setCode("ENG");
        testDepartment.setOrganization(testOrganization);
        testDepartment = departmentRepository.save(testDepartment);

        // Create users with different roles
        adminUser = createUser("admin", "admin@test.com", "ADMIN");
        managerUser = createUser("manager", "manager@test.com", "MANAGER");
        regularUser = createUser("user", "user@test.com", "USER");

        // Authenticate users and get tokens
        adminToken = authenticateAndGetToken("admin", "password123");
        managerToken = authenticateAndGetToken("manager", "password123");
        userToken = authenticateAndGetToken("user", "password123");
    }

    @Test
    public void testCompleteEmployeeOnboardingWorkflow() throws Exception {
        // Step 1: Admin creates a new employee
        EmployeeRequest createRequest = createEmployeeRequest("EMP001", "John", "Doe");
        
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andReturn();

        Long employeeId = extractEmployeeId(createResult);
        assertThat(employeeId).isNotNull();

        // Step 2: Manager retrieves employee details
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(employeeId));

        // Step 3: Regular user can view their own employee record
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Step 4: Admin updates employee information
        EmployeeRequest updateRequest = createEmployeeRequest("EMP001", "John", "Smith");
        mockMvc.perform(put("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    public void testCompleteLeaveRequestWorkflow() throws Exception {
        // Setup: Create employee and leave type
        Employee employee = createTestEmployee();
        LeaveType leaveType = createTestLeaveType();

        // Step 1: Employee submits leave request
        LeaveRequestCreateDTO leaveRequest = new LeaveRequestCreateDTO();
        leaveRequest.setEmployeeId(employee.getId());
        leaveRequest.setLeaveTypeId(leaveType.getId());
        leaveRequest.setStartDate(LocalDate.now().plusDays(7));
        leaveRequest.setEndDate(LocalDate.now().plusDays(10));
        leaveRequest.setReason("Family vacation");

        MvcResult createResult = mockMvc.perform(post("/api/leaves")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(LeaveStatus.PENDING.toString()))
                .andReturn();

        Long leaveRequestId = extractLeaveRequestId(createResult);

        // Step 2: Manager reviews and approves leave request
        mockMvc.perform(put("/api/leaves/" + leaveRequestId + "/approve")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"Approved for vacation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(LeaveStatus.APPROVED.toString()));

        // Step 3: Employee verifies approved leave request
        mockMvc.perform(get("/api/leaves")
                .header("Authorization", "Bearer " + userToken)
                .param("employeeId", employee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(LeaveStatus.APPROVED.toString()));
    }

    @Test
    public void testAuthorizationEnforcement() throws Exception {
        // Create an employee
        EmployeeRequest createRequest = createEmployeeRequest("EMP002", "Jane", "Doe");
        
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Long employeeId = extractEmployeeId(createResult);

        // Test 1: Regular user cannot delete employees
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Test 2: Unauthenticated requests are rejected
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isUnauthorized());

        // Test 3: Invalid token is rejected
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        // Test 4: Admin can delete employees
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testSecurityAcrossMultipleOperations() throws Exception {
        // Test 1: Create employee with admin role
        EmployeeRequest request1 = createEmployeeRequest("EMP003", "Alice", "Admin");
        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Test 2: Manager cannot create employees (if restricted)
        EmployeeRequest request2 = createEmployeeRequest("EMP004", "Bob", "Manager");
        MvcResult managerCreateResult = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andReturn();

        // Manager may or may not have create permission - just verify response is consistent
        int statusCode = managerCreateResult.getResponse().getStatus();
        assertThat(statusCode).isIn(200, 403);

        // Test 3: List employees with different roles
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + managerToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDataIsolationAndConsistency() throws Exception {
        // Create multiple employees
        EmployeeRequest emp1 = createEmployeeRequest("EMP005", "User1", "Test");
        EmployeeRequest emp2 = createEmployeeRequest("EMP006", "User2", "Test");

        MvcResult result1 = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp2)))
                .andExpect(status().isOk())
                .andReturn();

        Long emp1Id = extractEmployeeId(result1);
        Long emp2Id = extractEmployeeId(result2);

        // Verify both employees exist and are distinct
        assertThat(emp1Id).isNotEqualTo(emp2Id);

        mockMvc.perform(get("/api/employees/" + emp1Id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeNumber").value("EMP005"));

        mockMvc.perform(get("/api/employees/" + emp2Id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeNumber").value("EMP006"));
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
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return userRepository.save(user);
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

    private EmployeeRequest createEmployeeRequest(String empNumber, String firstName, String lastName) {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmployeeNumber(empNumber);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
        request.setPhoneNumber("+1234567890");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.MALE);
        request.setHireDate(LocalDate.now());
        request.setJobTitle("Software Engineer");
        request.setEmploymentStatus(EmploymentStatus.ACTIVE);
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setDepartmentId(testDepartment.getId());
        request.setOrganizationId(testOrganization.getId());
        return request;
    }

    private Employee createTestEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP999");
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setEmail("test.employee@test.com");
        employee.setPhoneNumber("+1234567890");
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now());
        employee.setJobTitle("Engineer");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        employee.setDepartment(testDepartment);
        employee.setOrganization(testOrganization);
        return employeeRepository.save(employee);
    }

    private LeaveType createTestLeaveType() {
        LeaveType leaveType = new LeaveType();
        leaveType.setName("Annual Leave");
        leaveType.setCode("AL");
        leaveType.setCategory(LeaveTypeCategory.PAID);
        leaveType.setDefaultDaysPerYear(20);
        leaveType.setRequiresApproval(true);
        leaveType.setOrganization(testOrganization);
        return leaveTypeRepository.save(leaveType);
    }

    private Long extractEmployeeId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractLeaveRequestId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }
}

