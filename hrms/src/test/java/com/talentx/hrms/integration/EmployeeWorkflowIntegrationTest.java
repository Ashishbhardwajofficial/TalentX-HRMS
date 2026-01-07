package com.talentx.hrms.integration;

import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.dto.employee.EmployeeResponse;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.repository.DepartmentRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for complete employee management workflow.
 * Tests Requirements: 3.1, 3.2, 3.3
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EmployeeWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Organization testOrganization;
    private Department testDepartment;

    @BeforeEach
    public void setup() {
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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCompleteEmployeeLifecycle() throws Exception {
        // Step 1: Create employee
        EmployeeRequest createRequest = createEmployeeRequest();
        
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        EmployeeResponse createdEmployee = objectMapper.readTree(responseBody)
                .get("data")
                .traverse(objectMapper)
                .readValueAs(EmployeeResponse.class);
        
        Long employeeId = createdEmployee.getId();
        assertThat(employeeId).isNotNull();

        // Step 2: Retrieve employee
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(employeeId))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        // Step 3: Update employee
        EmployeeRequest updateRequest = createEmployeeRequest();
        updateRequest.setFirstName("Jane");
        
        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));

        // Step 4: List employees (verify it appears in list)
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // Step 5: Delete employee
        mockMvc.perform(delete("/api/employees/" + employeeId))
                .andExpect(status().isOk());

        // Step 6: Verify deletion
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEmployeeSearchAndFilter() throws Exception {
        // Create multiple employees
        for (int i = 0; i < 3; i++) {
            EmployeeRequest request = createEmployeeRequest();
            request.setFirstName("Employee" + i);
            request.setEmployeeNumber("EMP00" + i);
            
            mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // Test pagination
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUnauthorizedAccess() throws Exception {
        // Users without ADMIN role should not be able to delete
        EmployeeRequest createRequest = createEmployeeRequest();
        
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        if (createResult.getResponse().getStatus() == 200) {
            String responseBody = createResult.getResponse().getContentAsString();
            EmployeeResponse createdEmployee = objectMapper.readTree(responseBody)
                    .get("data")
                    .traverse(objectMapper)
                    .readValueAs(EmployeeResponse.class);
            
            Long employeeId = createdEmployee.getId();

            // Attempt to delete should fail or be restricted
            mockMvc.perform(delete("/api/employees/" + employeeId))
                    .andExpect(status().isForbidden());
        }
    }

    private EmployeeRequest createEmployeeRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmployeeNumber("EMP001");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");
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
}

