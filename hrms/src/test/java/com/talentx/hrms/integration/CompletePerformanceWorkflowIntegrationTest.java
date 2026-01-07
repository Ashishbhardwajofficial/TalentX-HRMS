package com.talentx.hrms.integration;

import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.dto.performance.GoalCreateDTO;
import com.talentx.hrms.dto.performance.PerformanceReviewCreateDTO;
import com.talentx.hrms.dto.performance.PerformanceReviewCycleCreateDTO;
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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete performance workflow integration test
 * Tests Requirements: 8.1, 8.3, 8.5 - Performance management workflow
 * 
 * **Feature: hrms-frontend-complete-integration, Property 2: Complete
 * Performance Workflow**
 * Tests the complete performance workflow: Create review cycle → Submit reviews
 * → Create goals → Track progress
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompletePerformanceWorkflowIntegrationTest {

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
        private PasswordEncoder passwordEncoder;

        private Organization testOrganization;
        private Department testDepartment;
        private User adminUser;
        private User managerUser;
        private String adminToken;
        private String managerToken;
        private Employee testEmployee;

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
                managerUser = createUser("manager", "manager@test.com", "MANAGER");

                // Create test employee
                testEmployee = createTestEmployee();

                // Authenticate users
                adminToken = authenticateAndGetToken("admin", "password123");
                managerToken = authenticateAndGetToken("manager", "password123");
        }

        @Test
        public void testCompletePerformanceWorkflow() throws Exception {
                // Step 1: Create performance review cycle
                PerformanceReviewCycleCreateDTO cycleRequest = new PerformanceReviewCycleCreateDTO();
                cycleRequest.setName("Annual Review 2024");
                cycleRequest.setReviewType(PerformanceReviewCycle.ReviewType.ANNUAL);
                cycleRequest.setStartDate(LocalDate.now());
                cycleRequest.setEndDate(LocalDate.now().plusDays(30));
                cycleRequest.setSelfReviewDeadline(LocalDate.now().plusDays(15));
                cycleRequest.setManagerReviewDeadline(LocalDate.now().plusDays(25));
                cycleRequest.setOrganizationId(testOrganization.getId());

                MvcResult cycleResult = mockMvc.perform(post("/api/performance/cycles")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cycleRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Annual Review 2024"))
                                .andExpect(jsonPath("$.data.reviewType").value("ANNUAL"))
                                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                                .andReturn();

                Long cycleId = extractCycleId(cycleResult);
                assertThat(cycleId).isNotNull();

                // Step 2: Employee submits self-review
                PerformanceReviewCreateDTO selfReviewRequest = new PerformanceReviewCreateDTO();
                selfReviewRequest.setReviewCycleId(cycleId);
                selfReviewRequest.setEmployeeId(testEmployee.getId());
                selfReviewRequest.setReviewerId(testEmployee.getId()); // Self-review
                selfReviewRequest.setReviewType(PerformanceReview.ReviewType.SELF);
                selfReviewRequest.setOverallRating(4);
                selfReviewRequest.setStrengths("Strong technical skills, good problem-solving abilities");
                selfReviewRequest.setAreasForImprovement("Communication skills, time management");
                selfReviewRequest.setAchievements("Completed 3 major projects, mentored 2 junior developers");
                selfReviewRequest.setGoalsNextPeriod("Learn new technologies, improve leadership skills");

                MvcResult selfReviewResult = mockMvc.perform(post("/api/performance/reviews")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(selfReviewRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.reviewType").value("SELF"))
                                .andExpect(jsonPath("$.data.overallRating").value(4))
                                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                                .andReturn();

                Long selfReviewId = extractReviewId(selfReviewResult);

                // Step 3: Submit self-review
                mockMvc.perform(put("/api/performance/reviews/" + selfReviewId + "/submit")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

                // Step 4: Manager submits manager review
                PerformanceReviewCreateDTO managerReviewRequest = new PerformanceReviewCreateDTO();
                managerReviewRequest.setReviewCycleId(cycleId);
                managerReviewRequest.setEmployeeId(testEmployee.getId());
                managerReviewRequest.setReviewerId(managerUser.getId());
                managerReviewRequest.setReviewType(PerformanceReview.ReviewType.MANAGER);
                managerReviewRequest.setOverallRating(4);
                managerReviewRequest.setStrengths("Excellent technical delivery, reliable team member");
                managerReviewRequest.setAreasForImprovement("Could take more initiative in cross-team collaboration");
                managerReviewRequest.setAchievements("Delivered all projects on time, helped onboard new team members");
                managerReviewRequest.setGoalsNextPeriod("Lead a major project, develop presentation skills");

                MvcResult managerReviewResult = mockMvc.perform(post("/api/performance/reviews")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(managerReviewRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.reviewType").value("MANAGER"))
                                .andReturn();

                Long managerReviewId = extractReviewId(managerReviewResult);

                // Step 5: Submit manager review
                mockMvc.perform(put("/api/performance/reviews/" + managerReviewId + "/submit")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

                // Step 6: Create performance goals based on review feedback
                GoalCreateDTO goal1Request = new GoalCreateDTO();
                goal1Request.setEmployeeId(testEmployee.getId());
                goal1Request.setTitle("Improve Communication Skills");
                goal1Request.setDescription("Attend communication workshop and practice presentation skills");
                goal1Request.setGoalType(Goal.GoalType.INDIVIDUAL);
                goal1Request.setCategory(Goal.GoalCategory.DEVELOPMENT);
                goal1Request.setStartDate(LocalDate.now());
                goal1Request.setTargetDate(LocalDate.now().plusMonths(6));
                goal1Request.setProgressPercentage(0);
                goal1Request.setWeight(30);
                goal1Request.setMeasurementCriteria("Complete workshop and deliver 2 presentations");

                MvcResult goal1Result = mockMvc.perform(post("/api/performance/goals")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(goal1Request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Improve Communication Skills"))
                                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                                .andExpect(jsonPath("$.data.progressPercentage").value(0))
                                .andReturn();

                Long goal1Id = extractGoalId(goal1Result);

                // Step 7: Create second goal
                GoalCreateDTO goal2Request = new GoalCreateDTO();
                goal2Request.setEmployeeId(testEmployee.getId());
                goal2Request.setTitle("Lead Major Project");
                goal2Request.setDescription("Successfully lead and deliver a major software project");
                goal2Request.setGoalType(Goal.GoalType.INDIVIDUAL);
                goal2Request.setCategory(Goal.GoalCategory.PERFORMANCE);
                goal2Request.setStartDate(LocalDate.now());
                goal2Request.setTargetDate(LocalDate.now().plusMonths(9));
                goal2Request.setProgressPercentage(0);
                goal2Request.setWeight(70);
                goal2Request.setMeasurementCriteria("Project delivered on time and within budget");

                MvcResult goal2Result = mockMvc.perform(post("/api/performance/goals")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(goal2Request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Lead Major Project"))
                                .andReturn();

                Long goal2Id = extractGoalId(goal2Result);

                // Step 8: Track progress on goals
                // Update goal 1 progress
                mockMvc.perform(put("/api/performance/goals/" + goal1Id)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"progressPercentage\": 25, \"status\": \"IN_PROGRESS\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.progressPercentage").value(25))
                                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

                // Update goal 2 progress
                mockMvc.perform(put("/api/performance/goals/" + goal2Id)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"progressPercentage\": 15, \"status\": \"IN_PROGRESS\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.progressPercentage").value(15));

                // Step 9: Verify complete workflow - check all created entities
                // Verify review cycle
                mockMvc.perform(get("/api/performance/cycles")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(cycleId))
                                .andExpect(jsonPath("$.data[0].name").value("Annual Review 2024"));

                // Verify employee reviews
                mockMvc.perform(get("/api/performance/reviews/employee/" + testEmployee.getId())
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2)); // Self and manager review

                // Verify employee goals
                mockMvc.perform(get("/api/performance/goals/employee/" + testEmployee.getId())
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"))
                                .andExpect(jsonPath("$.data[1].status").value("IN_PROGRESS"));
        }

        @Test
        public void testPerformanceWorkflowValidation() throws Exception {
                // Test 1: Create review cycle with invalid dates
                PerformanceReviewCycleCreateDTO invalidCycleRequest = new PerformanceReviewCycleCreateDTO();
                invalidCycleRequest.setName("Invalid Cycle");
                invalidCycleRequest.setReviewType(PerformanceReviewCycle.ReviewType.ANNUAL);
                invalidCycleRequest.setStartDate(LocalDate.now().plusDays(10));
                invalidCycleRequest.setEndDate(LocalDate.now()); // End date before start date

                mockMvc.perform(post("/api/performance/cycles")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidCycleRequest)))
                                .andExpect(status().isBadRequest());

                // Test 2: Create review without valid cycle
                PerformanceReviewCreateDTO invalidReviewRequest = new PerformanceReviewCreateDTO();
                invalidReviewRequest.setReviewCycleId(999L); // Non-existent cycle
                invalidReviewRequest.setEmployeeId(testEmployee.getId());
                invalidReviewRequest.setReviewerId(testEmployee.getId());
                invalidReviewRequest.setReviewType(PerformanceReview.ReviewType.SELF);

                mockMvc.perform(post("/api/performance/reviews")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidReviewRequest)))
                                .andExpect(status().isNotFound());

                // Test 3: Create goal with invalid target date
                GoalCreateDTO invalidGoalRequest = new GoalCreateDTO();
                invalidGoalRequest.setEmployeeId(testEmployee.getId());
                invalidGoalRequest.setTitle("Invalid Goal");
                invalidGoalRequest.setStartDate(LocalDate.now());
                invalidGoalRequest.setTargetDate(LocalDate.now().minusDays(1)); // Target date before start date

                mockMvc.perform(post("/api/performance/goals")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidGoalRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testPerformanceWorkflowPermissions() throws Exception {
                // Create a review cycle as admin
                PerformanceReviewCycleCreateDTO cycleRequest = new PerformanceReviewCycleCreateDTO();
                cycleRequest.setName("Permission Test Cycle");
                cycleRequest.setReviewType(PerformanceReviewCycle.ReviewType.QUARTERLY);
                cycleRequest.setStartDate(LocalDate.now());
                cycleRequest.setEndDate(LocalDate.now().plusDays(30));
                cycleRequest.setOrganizationId(testOrganization.getId());

                MvcResult cycleResult = mockMvc.perform(post("/api/performance/cycles")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cycleRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                Long cycleId = extractCycleId(cycleResult);

                // Manager should be able to create reviews for their employees
                PerformanceReviewCreateDTO reviewRequest = new PerformanceReviewCreateDTO();
                reviewRequest.setReviewCycleId(cycleId);
                reviewRequest.setEmployeeId(testEmployee.getId());
                reviewRequest.setReviewerId(managerUser.getId());
                reviewRequest.setReviewType(PerformanceReview.ReviewType.MANAGER);
                reviewRequest.setOverallRating(3);

                mockMvc.perform(post("/api/performance/reviews")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reviewRequest)))
                                .andExpect(status().isOk());

                // Manager should be able to create goals for their employees
                GoalCreateDTO goalRequest = new GoalCreateDTO();
                goalRequest.setEmployeeId(testEmployee.getId());
                goalRequest.setTitle("Manager Assigned Goal");
                goalRequest.setGoalType(Goal.GoalType.INDIVIDUAL);
                goalRequest.setCategory(Goal.GoalCategory.PERFORMANCE);
                goalRequest.setStartDate(LocalDate.now());
                goalRequest.setTargetDate(LocalDate.now().plusMonths(3));
                goalRequest.setProgressPercentage(0);

                mockMvc.perform(post("/api/performance/goals")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(goalRequest)))
                                .andExpect(status().isOk());
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
                employee.setHireDate(LocalDate.now());
                employee.setJobTitle("Software Engineer");
                employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
                employee.setEmploymentType(EmploymentType.FULL_TIME);
                employee.setDepartment(testDepartment);
                employee.setOrganization(testOrganization);
                return employeeRepository.save(employee);
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

        private Long extractCycleId(MvcResult result) throws Exception {
                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data")
                                .get("id")
                                .asLong();
        }

        private Long extractReviewId(MvcResult result) throws Exception {
                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data")
                                .get("id")
                                .asLong();
        }

        private Long extractGoalId(MvcResult result) throws Exception {
                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data")
                                .get("id")
                                .asLong();
        }
}
