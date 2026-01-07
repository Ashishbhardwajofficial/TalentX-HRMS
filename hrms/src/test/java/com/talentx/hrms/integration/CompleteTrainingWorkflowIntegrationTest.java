package com.talentx.hrms.integration;

import com.talentx.hrms.dto.auth.LoginRequest;
import com.talentx.hrms.dto.employee.EmployeeRequest;
import com.talentx.hrms.dto.skill.EmployeeSkillCreateDTO;
import com.talentx.hrms.dto.skill.SkillCreateDTO;
import com.talentx.hrms.dto.training.TrainingEnrollmentCreateDTO;
import com.talentx.hrms.dto.training.TrainingProgramCreateDTO;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.*;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.entity.skills.Skill;
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
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Complete training workflow integration test
 * Tests Requirements: 9.1, 10.1, 10.2 - Training and skills management workflow
 * 
 * **Feature: hrms-frontend-complete-integration, Property 3: Complete Training
 * Workflow**
 * Tests the complete training workflow: Create program → Enroll employee →
 * Complete training → Verify skills
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompleteTrainingWorkflowIntegrationTest {

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
        private SkillRepository skillRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private Organization testOrganization;
        private Department testDepartment;
        private User adminUser;
        private User trainerUser;
        private String adminToken;
        private String trainerToken;
        private Employee testEmployee;
        private Skill testSkill;

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
                trainerUser = createUser("trainer", "trainer@test.com", "TRAINER");

                // Create test employee
                testEmployee = createTestEmployee();

                // Create test skill
                testSkill = createTestSkill();

                // Authenticate users
                adminToken = authenticateAndGetToken("admin", "password123");
                trainerToken = authenticateAndGetToken("trainer", "password123");
        }

        @Test
        public void testCompleteTrainingWorkflow() throws Exception {
                // Step 1: Create training program
                TrainingProgramCreateDTO programRequest = new TrainingProgramCreateDTO();
                programRequest.setTitle("Advanced Java Programming");
                programRequest.setDescription("Comprehensive training on advanced Java concepts and frameworks");
                programRequest.setTrainingType(com.talentx.hrms.entity.training.TrainingProgram.TrainingType.TECHNICAL);
                programRequest.setDeliveryMethod(
                                com.talentx.hrms.entity.training.TrainingProgram.DeliveryMethod.ONLINE);
                programRequest.setDurationHours(40);
                programRequest.setCostPerParticipant(new BigDecimal("500.00"));
                programRequest.setMaxParticipants(20);
                programRequest.setProvider("Tech Training Institute");
                programRequest.setMandatory(false);
                programRequest.setOrganizationId(testOrganization.getId());

                MvcResult programResult = mockMvc.perform(post("/api/training/programs")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(programRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Advanced Java Programming"))
                                .andExpect(jsonPath("$.data.trainingType").value("TECHNICAL"))
                                .andExpect(jsonPath("$.data.deliveryMethod").value("ONLINE"))
                                .andExpect(jsonPath("$.data.durationHours").value(40))
                                .andExpect(jsonPath("$.data.isActive").value(true))
                                .andReturn();

                Long programId = extractProgramId(programResult);
                assertThat(programId).isNotNull();

                // Step 2: Enroll employee in training program
                TrainingEnrollmentCreateDTO enrollmentRequest = new TrainingEnrollmentCreateDTO();
                enrollmentRequest.setTrainingProgramId(programId);
                enrollmentRequest.setEmployeeId(testEmployee.getId());
                enrollmentRequest.setEnrolledDate(LocalDate.now());
                enrollmentRequest.setStartDate(LocalDate.now().plusDays(7));
                enrollmentRequest.setDueDate(LocalDate.now().plusDays(30));
                enrollmentRequest.setAssignedBy(adminUser.getId());

                MvcResult enrollmentResult = mockMvc.perform(post("/api/training/enrollments")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enrollmentRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.trainingProgramId").value(programId))
                                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                                .andExpect(jsonPath("$.data.status").value("ENROLLED"))
                                .andReturn();

                Long enrollmentId = extractEnrollmentId(enrollmentResult);
                assertThat(enrollmentId).isNotNull();

                // Step 3: Start training (update status to IN_PROGRESS)
                mockMvc.perform(put("/api/training/enrollments/" + enrollmentId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\": \"IN_PROGRESS\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

                // Step 4: Complete training with score
                mockMvc.perform(put("/api/training/enrollments/" + enrollmentId + "/complete")
                                .header("Authorization", "Bearer " + trainerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"score\": 85, \"passingScore\": 70, \"certificateUrl\": \"https://certificates.example.com/cert123\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.data.score").value(85))
                                .andExpect(jsonPath("$.data.certificateUrl")
                                                .value("https://certificates.example.com/cert123"));

                // Step 5: Add skill to employee based on completed training
                EmployeeSkillCreateDTO skillRequest = new EmployeeSkillCreateDTO();
                skillRequest.setEmployeeId(testEmployee.getId());
                skillRequest.setSkillId(testSkill.getId());
                skillRequest.setProficiencyLevel(
                                com.talentx.hrms.entity.skills.EmployeeSkill.ProficiencyLevel.INTERMEDIATE);
                skillRequest.setYearsOfExperience(2);
                skillRequest.setLastUsedYear(LocalDate.now().getYear());

                MvcResult skillResult = mockMvc.perform(post("/api/skills/employee")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                                .andExpect(jsonPath("$.data.skillId").value(testSkill.getId()))
                                .andExpect(jsonPath("$.data.proficiencyLevel").value("INTERMEDIATE"))
                                .andReturn();

                Long employeeSkillId = extractEmployeeSkillId(skillResult);

                // Step 6: Verify skill (manager/trainer verification)
                mockMvc.perform(put("/api/skills/employee/" + employeeSkillId + "/verify")
                                .header("Authorization", "Bearer " + trainerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"verificationComments\": \"Skill verified through training completion and assessment\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.verifiedBy").value(trainerUser.getId()))
                                .andExpect(jsonPath("$.data.verifiedAt").exists());

                // Step 7: Verify complete workflow - check all created entities
                // Verify training program exists
                mockMvc.perform(get("/api/training/programs")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(programId))
                                .andExpect(jsonPath("$.data[0].title").value("Advanced Java Programming"));

                // Verify employee enrollment
                mockMvc.perform(get("/api/training/enrollments/employee/" + testEmployee.getId())
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(enrollmentId))
                                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                                .andExpect(jsonPath("$.data[0].score").value(85));

                // Verify employee skills
                mockMvc.perform(get("/api/skills/employee/" + testEmployee.getId())
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].skillId").value(testSkill.getId()))
                                .andExpect(jsonPath("$.data[0].proficiencyLevel").value("INTERMEDIATE"))
                                .andExpect(jsonPath("$.data[0].verifiedBy").value(trainerUser.getId()));
        }

        @Test
        public void testMandatoryTrainingWorkflow() throws Exception {
                // Step 1: Create mandatory training program
                TrainingProgramCreateDTO mandatoryProgramRequest = new TrainingProgramCreateDTO();
                mandatoryProgramRequest.setTitle("Safety Training");
                mandatoryProgramRequest.setDescription("Mandatory workplace safety training");
                mandatoryProgramRequest
                                .setTrainingType(com.talentx.hrms.entity.training.TrainingProgram.TrainingType.SAFETY);
                mandatoryProgramRequest.setDeliveryMethod(
                                com.talentx.hrms.entity.training.TrainingProgram.DeliveryMethod.IN_PERSON);
                mandatoryProgramRequest.setDurationHours(8);
                mandatoryProgramRequest.setMandatory(true);
                mandatoryProgramRequest.setOrganizationId(testOrganization.getId());

                MvcResult programResult = mockMvc.perform(post("/api/training/programs")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(mandatoryProgramRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.isMandatory").value(true))
                                .andReturn();

                Long programId = extractProgramId(programResult);

                // Step 2: System should auto-enroll eligible employees (simulated by manual
                // enrollment)
                TrainingEnrollmentCreateDTO autoEnrollmentRequest = new TrainingEnrollmentCreateDTO();
                autoEnrollmentRequest.setTrainingProgramId(programId);
                autoEnrollmentRequest.setEmployeeId(testEmployee.getId());
                autoEnrollmentRequest.setEnrolledDate(LocalDate.now());
                autoEnrollmentRequest.setDueDate(LocalDate.now().plusDays(14)); // Shorter deadline for mandatory
                                                                                // training

                MvcResult enrollmentResult = mockMvc.perform(post("/api/training/enrollments")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(autoEnrollmentRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("ENROLLED"))
                                .andReturn();

                Long enrollmentId = extractEnrollmentId(enrollmentResult);

                // Step 3: Complete mandatory training
                mockMvc.perform(put("/api/training/enrollments/" + enrollmentId + "/complete")
                                .header("Authorization", "Bearer " + trainerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"score\": 95, \"passingScore\": 80}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

                // Verify mandatory training completion
                mockMvc.perform(get("/api/training/enrollments/employee/" + testEmployee.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .param("mandatory", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
        }

        @Test
        public void testTrainingWorkflowValidation() throws Exception {
                // Test 1: Create training program with invalid data
                TrainingProgramCreateDTO invalidProgramRequest = new TrainingProgramCreateDTO();
                invalidProgramRequest.setTitle(""); // Empty title
                invalidProgramRequest.setDurationHours(-5); // Negative duration

                mockMvc.perform(post("/api/training/programs")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidProgramRequest)))
                                .andExpect(status().isBadRequest());

                // Test 2: Enroll in non-existent training program
                TrainingEnrollmentCreateDTO invalidEnrollmentRequest = new TrainingEnrollmentCreateDTO();
                invalidEnrollmentRequest.setTrainingProgramId(999L); // Non-existent program
                invalidEnrollmentRequest.setEmployeeId(testEmployee.getId());

                mockMvc.perform(post("/api/training/enrollments")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidEnrollmentRequest)))
                                .andExpect(status().isNotFound());

                // Test 3: Complete training with failing score
                // First create valid enrollment
                TrainingProgramCreateDTO validProgramRequest = new TrainingProgramCreateDTO();
                validProgramRequest.setTitle("Test Program");
                validProgramRequest.setTrainingType(
                                com.talentx.hrms.entity.training.TrainingProgram.TrainingType.TECHNICAL);
                validProgramRequest.setDeliveryMethod(
                                com.talentx.hrms.entity.training.TrainingProgram.DeliveryMethod.ONLINE);
                validProgramRequest.setOrganizationId(testOrganization.getId());

                MvcResult programResult = mockMvc.perform(post("/api/training/programs")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validProgramRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                Long programId = extractProgramId(programResult);

                TrainingEnrollmentCreateDTO enrollmentRequest = new TrainingEnrollmentCreateDTO();
                enrollmentRequest.setTrainingProgramId(programId);
                enrollmentRequest.setEmployeeId(testEmployee.getId());
                enrollmentRequest.setEnrolledDate(LocalDate.now());

                MvcResult enrollmentResult = mockMvc.perform(post("/api/training/enrollments")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(enrollmentRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                Long enrollmentId = extractEnrollmentId(enrollmentResult);

                // Complete with failing score
                mockMvc.perform(put("/api/training/enrollments/" + enrollmentId + "/complete")
                                .header("Authorization", "Bearer " + trainerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"score\": 45, \"passingScore\": 70}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("FAILED"));
        }

        @Test
        public void testSkillManagementWorkflow() throws Exception {
                // Test 1: Create new skill
                SkillCreateDTO skillRequest = new SkillCreateDTO();
                skillRequest.setName("React Development");
                skillRequest.setCategory("Frontend");
                skillRequest.setDescription("React.js framework development skills");

                MvcResult skillResult = mockMvc.perform(post("/api/skills")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skillRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("React Development"))
                                .andExpect(jsonPath("$.data.category").value("Frontend"))
                                .andReturn();

                Long skillId = extractSkillId(skillResult);

                // Test 2: Add skill to employee
                EmployeeSkillCreateDTO employeeSkillRequest = new EmployeeSkillCreateDTO();
                employeeSkillRequest.setEmployeeId(testEmployee.getId());
                employeeSkillRequest.setSkillId(skillId);
                employeeSkillRequest.setProficiencyLevel(
                                com.talentx.hrms.entity.skills.EmployeeSkill.ProficiencyLevel.BEGINNER);
                employeeSkillRequest.setYearsOfExperience(1);

                mockMvc.perform(post("/api/skills/employee")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employeeSkillRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.proficiencyLevel").value("BEGINNER"));

                // Test 3: Search employees by skill
                mockMvc.perform(get("/api/employees")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("skillId", skillId.toString()))
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
                employee.setHireDate(LocalDate.now());
                employee.setJobTitle("Software Engineer");
                employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
                employee.setEmploymentType(EmploymentType.FULL_TIME);
                employee.setDepartment(testDepartment);
                employee.setOrganization(testOrganization);
                return employeeRepository.save(employee);
        }

        private Skill createTestSkill() {
                Skill skill = new Skill();
                skill.setName("Java Programming");
                skill.setCategory("Programming");
                skill.setDescription("Java programming language skills");
                return skillRepository.save(skill);
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

        private Long extractProgramId(MvcResult result) throws Exception {
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

        private Long extractEmployeeSkillId(MvcResult result) throws Exception {
                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data")
                                .get("id")
                                .asLong();
        }

        private Long extractSkillId(MvcResult result) throws Exception {
                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data")
                                .get("id")
                                .asLong();
        }
}
