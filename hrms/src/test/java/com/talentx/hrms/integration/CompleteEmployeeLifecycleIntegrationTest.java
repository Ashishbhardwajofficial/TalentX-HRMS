package com.talentx.hrms.integration;

import com.talentx.hrms.dto.attendance.AttendanceCheckInDTO;
import com.talentx.hrms.dto.attendance.AttendanceCheckOutDTO;
import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.dto.exit.EmployeeExitCreateDTO;
import com.talentx.hrms.dto.expense.ExpenseCreateDTO;
import com.talentx.hrms.entity.attendance.Shift;
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
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete employee lifecycle integration test
 * Tests Requirements: All - Complete workflow from employee creation to exit
 * 
 * **Feature: hrms-frontend-complete-integration, Property 1: Complete Employee Lifecycle Workflow**
 * Tests the complete employee lifecycle: Create employee → Assign shift → Track attendance → Submit expense → Initiate exit
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompleteEmployeeLifecycleIntegrationTest {

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
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization testOrganization;
    private Department testDepartment;
    private User adminUser;
    private String adminToken;
    private Shift testShift;

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

        // Create admin user
        adminUser = createUser("admin", "admin@test.com", "ADMIN");

        // Create test shift
        testShift = new Shift();
        testShift.setName("Day Shift");
        testShift.setStartTime(LocalTime.of(9, 0));
        testShift.setEndTime(LocalTime.of(17, 0));
        testShift.setBreakMinutes(60);
        testShift.setNightShift(false);
        testShift.setOrganization(testOrganization);
        testShift = shiftRepository.save(testShift);

        // Authenticate admin user
        adminToken = authenticateAndGetToken("admin", "password123");
    }

    @Test
    public void testCompleteEmployeeLifecycleWorkflow() throws Exception {
        // Step 1: Create employee
        EmployeeRequest createRequest = createEmployeeRequest("EMP001", "John", "Doe");
        
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.employeeNumber").value("EMP001"))
                .andReturn();

        Long employeeId = extractEmployeeId(createResult);
        assertThat(employeeId).isNotNull();

        // Step 2: Assign shift to employee
        mockMvc.perform(post("/api/shifts/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createShiftAssignmentRequest(employeeId, testShift.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(employeeId))
                .andExpect(jsonPath("$.data.shiftId").value(testShift.getId()));

        // Step 3: Employee checks in (track attendance)
        AttendanceCheckInDTO checkInRequest = new AttendanceCheckInDTO();
        checkInRequest.setEmployeeId(employeeId);
        checkInRequest.setCheckInLocation("Office");

        MvcResult checkInResult = mockMvc.perform(post("/api/attendance/check-in")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(employeeId))
                .andExpect(jsonPath("$.data.status").value("PRESENT"))
                .andReturn();

        Long attendanceId = extractAttendanceId(checkInResult);

        // Step 4: Employee checks out
        AttendanceCheckOutDTO checkOutRequest = new AttendanceCheckOutDTO();
        checkOutRequest.setEmployeeId(employeeId);
        checkOutRequest.setCheckOutLocation("Office");

        mockMvc.perform(post("/api/attendance/check-out")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalHours").exists());

        // Step 5: Employee submits expense
        ExpenseCreateDTO expenseRequest = new ExpenseCreateDTO();
        expenseRequest.setEmployeeId(employeeId);
        expenseRequest.setExpenseType(ExpenseType.TRAVEL);
        expenseRequest.setAmount(new BigDecimal("150.00"));
        expenseRequest.setExpenseDate(LocalDate.now());
        expenseRequest.setDescription("Business travel to client site");

        MvcResult expenseResult = mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.amount").value(150.00))
                .andReturn();

        Long expenseId = extractExpenseId(expenseResult);

        // Step 6: Approve expense
        mockMvc.perform(put("/api/expenses/" + expenseId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"Approved for business travel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Step 7: Initiate employee exit
        EmployeeExitCreateDTO exitRequest = new EmployeeExitCreateDTO();
        exitRequest.setEmployeeId(employeeId);
        exitRequest.setResignationDate(LocalDate.now());
        exitRequest.setLastWorkingDay(LocalDate.now().plusDays(14));
        exitRequest.setExitReason("Better opportunity");

        MvcResult exitResult = mockMvc.perform(post("/api/exits")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INITIATED"))
                .andExpect(jsonPath("$.data.employeeId").value(employeeId))
                .andReturn();

        Long exitId = extractExitId(exitResult);

        // Step 8: Approve exit
        mockMvc.perform(put("/api/exits/" + exitId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"Exit approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Step 9: Verify complete workflow - check employee status and history
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(employeeId))
                .andExpect(jsonPath("$.data.employmentStatus").value("ACTIVE")); // Still active until exit completion

        // Verify attendance record exists
        mockMvc.perform(get("/api/attendance/employee/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].employeeId").value(employeeId));

        // Verify expense record exists
        mockMvc.perform(get("/api/expenses/employee/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].employeeId").value(employeeId))
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));

        // Verify exit record exists
        mockMvc.perform(get("/api/exits/employee/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeId").value(employeeId))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testEmployeeLifecycleWithValidationErrors() throws Exception {
        // Test 1: Create employee with invalid data
        EmployeeRequest invalidRequest = new EmployeeRequest();
        invalidRequest.setEmployeeNumber(""); // Empty employee number
        invalidRequest.setFirstName("John");
        invalidRequest.setLastName("Doe");

        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test 2: Try to assign non-existent shift
        Long employeeId = createValidEmployee();

        mockMvc.perform(post("/api/shifts/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createShiftAssignmentRequest(employeeId, 999L))))
                .andExpect(status().isNotFound());

        // Test 3: Try to check in without shift assignment
        AttendanceCheckInDTO checkInRequest = new AttendanceCheckInDTO();
        checkInRequest.setEmployeeId(employeeId);

        mockMvc.perform(post("/api/attendance/check-in")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isBadRequest());
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

    private Object createShiftAssignmentRequest(Long employeeId, Long shiftId) {
        return new Object() {
            public Long getEmployeeId() { return employeeId; }
            public Long getShiftId() { return shiftId; }
            public LocalDate getEffectiveFrom() { return LocalDate.now(); }
        };
    }

    private Long createValidEmployee() throws Exception {
        EmployeeRequest request = createEmployeeRequest("EMP999", "Test", "Employee");
        
        MvcResult result = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return extractEmployeeId(result);
    }

    private Long extractEmployeeId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractAttendanceId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractExpenseId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }

    private Long extractExitId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
                .get("data")
                .get("id")
                .asLong();
    }
}

