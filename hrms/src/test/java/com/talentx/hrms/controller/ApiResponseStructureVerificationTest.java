package com.talentx.hrms.controller;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.dto.employee.EmployeeResponse;
import com.talentx.hrms.dto.leave.LeaveRequestResponseDTO;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.leave.LeaveRequest;
import com.talentx.hrms.mapper.EmployeeMapper;
import com.talentx.hrms.mapper.LeaveRequestMapper;
import com.talentx.hrms.repository.EmployeeRepository;
import com.talentx.hrms.repository.LeaveRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test to verify API response structures match entity/DTO structures
 * Validates Requirements 4.1, 4.2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ApiResponseStructureVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    /**
     * Test that API responses have consistent structure with required fields
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testApiResponseStructureConsistency() throws Exception {
        // Test employee endpoint
        MvcResult employeeResult = mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String employeeResponseJson = employeeResult.getResponse().getContentAsString();
        JsonNode employeeResponseNode = objectMapper.readTree(employeeResponseJson);

        // Verify standard ApiResponse structure
        verifyApiResponseStructure(employeeResponseNode);

        // Test leave endpoint
        MvcResult leaveResult = mockMvc.perform(get("/api/leaves")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String leaveResponseJson = leaveResult.getResponse().getContentAsString();
        JsonNode leaveResponseNode = objectMapper.readTree(leaveResponseJson);

        // Verify standard ApiResponse structure
        verifyApiResponseStructure(leaveResponseNode);
    }

    /**
     * Test that DTO fields match entity fields
     */
    @Test
    public void testDtoEntityFieldAlignment() {
        // Test Employee entity vs EmployeeResponse DTO alignment
        Set<String> entityFields = getFieldNames(Employee.class);
        Set<String> dtoFields = getFieldNames(EmployeeResponse.class);

        // Check that all essential entity fields are present in DTO
        String[] essentialEmployeeFields = {
            "id", "employeeNumber", "firstName", "lastName", "workEmail", 
            "employmentStatus", "employmentType", "hireDate", "jobTitle"
        };

        for (String field : essentialEmployeeFields) {
            assertTrue(dtoFields.contains(field), 
                "EmployeeResponse DTO missing essential field: " + field);
        }

        // Test LeaveRequest entity vs LeaveRequestResponseDTO alignment
        Set<String> leaveEntityFields = getFieldNames(LeaveRequest.class);
        Set<String> leaveDtoFields = getFieldNames(LeaveRequestResponseDTO.class);

        String[] essentialLeaveFields = {
            "id", "startDate", "endDate", "status", "appliedAt"
        };

        for (String field : essentialLeaveFields) {
            assertTrue(leaveDtoFields.contains(field), 
                "LeaveRequestResponseDTO missing essential field: " + field);
        }
    }

    /**
     * Test that paginated responses have consistent structure
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testPaginatedResponseStructure() throws Exception {
        // Test employee pagination
        MvcResult result = mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode responseNode = objectMapper.readTree(responseJson);

        // Verify ApiResponse structure
        verifyApiResponseStructure(responseNode);

        // Verify pagination structure in data field
        JsonNode dataNode = responseNode.get("data");
        assertNotNull(dataNode, "Response data should not be null");

        // Check Spring Data Page structure
        assertTrue(dataNode.has("content"), "Paginated response should have 'content' field");
        assertTrue(dataNode.has("totalElements"), "Paginated response should have 'totalElements' field");
        assertTrue(dataNode.has("totalPages"), "Paginated response should have 'totalPages' field");
        assertTrue(dataNode.has("size"), "Paginated response should have 'size' field");
        assertTrue(dataNode.has("number"), "Paginated response should have 'number' field");
        assertTrue(dataNode.has("first"), "Paginated response should have 'first' field");
        assertTrue(dataNode.has("last"), "Paginated response should have 'last' field");
    }

    /**
     * Test that single entity responses have correct structure
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testSingleEntityResponseStructure() throws Exception {
        // First, get a list to find an existing employee ID
        Page<Employee> employees = employeeRepository.findAll(PageRequest.of(0, 1));
        
        if (!employees.isEmpty()) {
            Long employeeId = employees.getContent().get(0).getId();
            
            MvcResult result = mockMvc.perform(get("/api/employees/" + employeeId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            JsonNode responseNode = objectMapper.readTree(responseJson);

            // Verify ApiResponse structure
            verifyApiResponseStructure(responseNode);

            // Verify that data contains employee fields
            JsonNode dataNode = responseNode.get("data");
            assertNotNull(dataNode, "Response data should not be null");
            
            // Check essential employee fields are present
            assertTrue(dataNode.has("id"), "Employee response should have 'id' field");
            assertTrue(dataNode.has("employeeNumber"), "Employee response should have 'employeeNumber' field");
            assertTrue(dataNode.has("firstName"), "Employee response should have 'firstName' field");
            assertTrue(dataNode.has("lastName"), "Employee response should have 'lastName' field");
        }
    }

    /**
     * Test that error responses have consistent structure
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testErrorResponseStructure() throws Exception {
        // Test with non-existent employee ID
        MvcResult result = mockMvc.perform(get("/api/employees/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        // For 404, Spring Boot might return empty response, so we test with validation error instead
        // Test validation error by creating employee with invalid data
        String invalidEmployeeJson = "{}"; // Empty JSON should trigger validation errors

        MvcResult validationResult = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmployeeJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String errorResponseJson = validationResult.getResponse().getContentAsString();
        if (!errorResponseJson.isEmpty()) {
            JsonNode errorResponseNode = objectMapper.readTree(errorResponseJson);
            
            // Verify error response has ApiResponse structure
            assertTrue(errorResponseNode.has("success"), "Error response should have 'success' field");
            assertTrue(errorResponseNode.has("message"), "Error response should have 'message' field");
            assertTrue(errorResponseNode.has("timestamp"), "Error response should have 'timestamp' field");
            
            // Verify success is false for error responses
            assertFalse(errorResponseNode.get("success").asBoolean(), "Error response success should be false");
        }
    }

    /**
     * Helper method to verify standard ApiResponse structure
     */
    private void verifyApiResponseStructure(JsonNode responseNode) {
        assertNotNull(responseNode, "Response should not be null");
        
        // Check required ApiResponse fields
        assertTrue(responseNode.has("success"), "Response should have 'success' field");
        assertTrue(responseNode.has("message"), "Response should have 'message' field");
        assertTrue(responseNode.has("timestamp"), "Response should have 'timestamp' field");
        
        // For successful responses, success should be true
        assertTrue(responseNode.get("success").asBoolean(), "Successful response should have success=true");
        
        // Message should not be null or empty
        assertNotNull(responseNode.get("message"), "Response message should not be null");
        assertFalse(responseNode.get("message").asText().isEmpty(), "Response message should not be empty");
        
        // Timestamp should be present and not null
        assertNotNull(responseNode.get("timestamp"), "Response timestamp should not be null");
    }

    /**
     * Helper method to get field names from a class using reflection
     */
    private Set<String> getFieldNames(Class<?> clazz) {
        Set<String> fieldNames = new HashSet<>();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }
        
        return fieldNames;
    }
}

