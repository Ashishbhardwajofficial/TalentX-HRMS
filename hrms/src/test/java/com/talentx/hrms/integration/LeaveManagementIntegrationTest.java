package com.talentx.hrms.integration;

import com.talentx.hrms.dto.leave.LeaveRequestCreateDTO;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.entity.enums.LeaveStatus;
import com.talentx.hrms.entity.enums.LeaveTypeCategory;
import com.talentx.hrms.entity.leave.LeaveType;
import com.talentx.hrms.repository.DepartmentRepository;
import com.talentx.hrms.repository.EmployeeRepository;
import com.talentx.hrms.repository.LeaveTypeRepository;
import com.talentx.hrms.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for leave management workflow.
 * Tests Requirements: 3.1, 3.2, 3.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LeaveManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    private Employee testEmployee;
    private LeaveType testLeaveType;

    @BeforeEach
    public void setup() {
        // Create test organization
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setLegalName("Test Org LLC");
        organization = organizationRepository.save(organization);

        // Create test department
        Department department = new Department();
        department.setName("Engineering");
        department.setCode("ENG");
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@test.com");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee.setGender(Gender.MALE);
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setJobTitle("Engineer");
        testEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee.setDepartment(department);
        testEmployee.setOrganization(organization);
        testEmployee = employeeRepository.save(testEmployee);

        // Create test leave type
        testLeaveType = new LeaveType();
        testLeaveType.setName("Annual Leave");
        testLeaveType.setCode("AL");
        testLeaveType.setCategory(LeaveTypeCategory.PAID);
        testLeaveType.setDefaultDaysPerYear(20);
        testLeaveType.setRequiresApproval(true);
        testLeaveType.setOrganization(organization);
        testLeaveType = leaveTypeRepository.save(testLeaveType);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testLeaveRequestWorkflow() throws Exception {
        // Step 1: Create leave request
        LeaveRequestCreateDTO createRequest = new LeaveRequestCreateDTO();
        createRequest.setEmployeeId(testEmployee.getId());
        createRequest.setLeaveTypeId(testLeaveType.getId());
        createRequest.setStartDate(LocalDate.now().plusDays(7));
        createRequest.setEndDate(LocalDate.now().plusDays(10));
        createRequest.setReason("Vacation");

        mockMvc.perform(post("/api/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(LeaveStatus.PENDING.toString()));

        // Step 2: List leave requests
        mockMvc.perform(get("/api/leaves")
                .param("employeeId", testEmployee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testLeaveApprovalWorkflow() throws Exception {
        // Create leave request
        LeaveRequestCreateDTO createRequest = new LeaveRequestCreateDTO();
        createRequest.setEmployeeId(testEmployee.getId());
        createRequest.setLeaveTypeId(testLeaveType.getId());
        createRequest.setStartDate(LocalDate.now().plusDays(7));
        createRequest.setEndDate(LocalDate.now().plusDays(10));
        createRequest.setReason("Vacation");

        String createResponse = mockMvc.perform(post("/api/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long leaveRequestId = objectMapper.readTree(createResponse)
                .get("data")
                .get("id")
                .asLong();

        // Approve leave request
        mockMvc.perform(put("/api/leaves/" + leaveRequestId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"Approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(LeaveStatus.APPROVED.toString()));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testInvalidLeaveRequest() throws Exception {
        // Test with invalid date range (end before start)
        LeaveRequestCreateDTO invalidRequest = new LeaveRequestCreateDTO();
        invalidRequest.setEmployeeId(testEmployee.getId());
        invalidRequest.setLeaveTypeId(testLeaveType.getId());
        invalidRequest.setStartDate(LocalDate.now().plusDays(10));
        invalidRequest.setEndDate(LocalDate.now().plusDays(7));
        invalidRequest.setReason("Invalid dates");

        mockMvc.perform(post("/api/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

