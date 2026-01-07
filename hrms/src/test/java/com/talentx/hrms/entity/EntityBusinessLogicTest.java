package com.talentx.hrms.entity;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.skills.EmployeeSkill;
import com.talentx.hrms.entity.skills.Skill;
import com.talentx.hrms.entity.training.TrainingProgram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Date;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for entity business logic methods.
 * Validates: Requirements 5.1, 5.2, 5.3
 */
@DisplayName("Entity Business Logic Tests")
class EntityBusinessLogicTest {

    private Employee employee;
    private Employee verifier;
    private Organization organization;
    private EmployeeSkill employeeSkill;
    private Skill skill;
    private TrainingProgram trainingProgram;

    @BeforeEach
    void setUp() {
        // Set up organization
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Organization");

        // Set up employee
        employee = new Employee(
            "EMP001",
            "John",
            "Doe",
            new Date(System.currentTimeMillis()),
            EmploymentStatus.ACTIVE,
            EmploymentType.FULL_TIME,
            organization
        );
        employee.setId(1L);

        // Set up verifier
        verifier = new Employee(
            "EMP002",
            "Jane",
            "Smith",
            new Date(System.currentTimeMillis()),
            EmploymentStatus.ACTIVE,
            EmploymentType.FULL_TIME,
            organization
        );
        verifier.setId(2L);

        // Set up skill
        skill = new Skill();
        skill.setId(1L);
        skill.setName("Java Programming");

        // Set up employee skill
        employeeSkill = new EmployeeSkill(employee, skill, EmployeeSkill.ProficiencyLevel.INTERMEDIATE);

        // Set up training program
        trainingProgram = new TrainingProgram(
            organization,
            "Leadership Training",
            TrainingProgram.TrainingType.LEADERSHIP,
            TrainingProgram.DeliveryMethod.IN_PERSON,
            false
        );
    }

    @Test
    @DisplayName("Should verify employee skill with verifier and timestamp")
    void testEmployeeSkillVerify() {
        // Arrange
        assertThat(employeeSkill.getVerifiedBy()).isNull();
        assertThat(employeeSkill.getVerifiedAt()).isNull();

        LocalDateTime beforeVerification = LocalDateTime.now().minusSeconds(1);

        // Act
        employeeSkill.verify(verifier);

        // Assert
        assertThat(employeeSkill.getVerifiedBy()).isEqualTo(verifier);
        assertThat(employeeSkill.getVerifiedAt()).isNotNull();
        assertThat(employeeSkill.getVerifiedAt()).isAfter(beforeVerification);
        assertThat(employeeSkill.getVerifiedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should allow re-verification with different verifier")
    void testEmployeeSkillReVerify() {
        // Arrange
        Employee firstVerifier = verifier;
        Employee secondVerifier = new Employee(
            "EMP003",
            "Bob",
            "Johnson",
            new Date(System.currentTimeMillis()),
            EmploymentStatus.ACTIVE,
            EmploymentType.FULL_TIME,
            organization
        );
        secondVerifier.setId(3L);

        // Act
        employeeSkill.verify(firstVerifier);
        LocalDateTime firstVerificationTime = employeeSkill.getVerifiedAt();
        
        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        employeeSkill.verify(secondVerifier);

        // Assert
        assertThat(employeeSkill.getVerifiedBy()).isEqualTo(secondVerifier);
        assertThat(employeeSkill.getVerifiedAt()).isAfter(firstVerificationTime);
    }

    @Test
    @DisplayName("Should get organization ID from training program")
    void testTrainingProgramGetOrganizationId() {
        // Act
        Long organizationId = trainingProgram.getOrganizationId();

        // Assert
        assertThat(organizationId).isEqualTo(1L);
        assertThat(organizationId).isEqualTo(organization.getId());
    }

    @Test
    @DisplayName("Should return null when training program has no organization")
    void testTrainingProgramGetOrganizationIdWhenNull() {
        // Arrange
        TrainingProgram programWithoutOrg = new TrainingProgram();

        // Act
        Long organizationId = programWithoutOrg.getOrganizationId();

        // Assert
        assertThat(organizationId).isNull();
    }

    @Test
    @DisplayName("Should set training program active status to true")
    void testTrainingProgramSetActiveTrue() {
        // Arrange
        trainingProgram.setIsActive(false);
        assertThat(trainingProgram.getIsActive()).isFalse();

        // Act
        trainingProgram.setActive(true);

        // Assert
        assertThat(trainingProgram.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should set training program active status to false")
    void testTrainingProgramSetActiveFalse() {
        // Arrange
        trainingProgram.setIsActive(true);
        assertThat(trainingProgram.getIsActive()).isTrue();

        // Act
        trainingProgram.setActive(false);

        // Assert
        assertThat(trainingProgram.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should set mobile number on employee")
    void testEmployeeSetMobile() {
        // Arrange
        String mobileNumber = "+91-9876543210";

        // Act
        employee.setMobile(mobileNumber);

        // Assert
        assertThat(employee.getMobile()).isEqualTo(mobileNumber);
        assertThat(employee.getMobileNumber()).isEqualTo(mobileNumber);
    }

    @Test
    @DisplayName("Should update mobile number on employee")
    void testEmployeeUpdateMobile() {
        // Arrange
        String oldMobile = "+91-1111111111";
        String newMobile = "+91-9876543210";
        employee.setMobile(oldMobile);
        assertThat(employee.getMobile()).isEqualTo(oldMobile);

        // Act
        employee.setMobile(newMobile);

        // Assert
        assertThat(employee.getMobile()).isEqualTo(newMobile);
        assertThat(employee.getMobileNumber()).isEqualTo(newMobile);
    }

    @Test
    @DisplayName("Should handle null mobile number")
    void testEmployeeSetMobileNull() {
        // Arrange
        employee.setMobile("+91-9876543210");
        assertThat(employee.getMobile()).isNotNull();

        // Act
        employee.setMobile(null);

        // Assert
        assertThat(employee.getMobile()).isNull();
        assertThat(employee.getMobileNumber()).isNull();
    }
}
