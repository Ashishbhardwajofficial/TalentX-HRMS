package com.talentx.hrms.entity;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.CompanySize;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.PersistenceException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * **Feature: hrms-database-integration, Property 2: Referential Integrity Enforcement**
 * 
 * Test that validates referential integrity is properly enforced across all HRMS entities.
 * This test ensures that for any database operation that would violate foreign key constraints, 
 * the system properly rejects the operation and maintains data consistency.
 */
@DataJpaTest
@ActiveProfiles("test")
public class ReferentialIntegrityPropertyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldEnforceOrganizationReferentialIntegrity() {
        // Test that departments cannot exist without a valid organization
        Department department = new Department();
        department.setName("Test Department");
        department.setCode("TEST");
        // Intentionally not setting organization
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(department);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceUserOrganizationReferentialIntegrity() {
        // Test that users cannot exist without a valid organization
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        // Intentionally not setting organization
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceEmployeeOrganizationReferentialIntegrity() {
        // Test that employees cannot exist without a valid organization
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setHireDate(LocalDate.now());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(EmploymentType.FULL_TIME);
        // Intentionally not setting organization
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(employee);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldMaintainReferentialIntegrityOnValidRelationships() {
        // Test that valid relationships are maintained correctly
        
        // Create organization
        Organization organization = new Organization("Test Corp");
        organization.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg = entityManager.persistAndFlush(organization);
        
        // Create department with valid organization reference
        Department department = new Department("Engineering", "ENG", savedOrg);
        Department savedDept = entityManager.persistAndFlush(department);
        
        // Create user with valid organization reference
        User user = new User("john_doe", "john@test.com", "hashedPassword", savedOrg);
        User savedUser = entityManager.persistAndFlush(user);
        
        // Create employee with valid organization reference
        Employee employee = new Employee("EMP001", "John", "Doe", 
                                        LocalDate.now(), EmploymentStatus.ACTIVE, 
                                        EmploymentType.FULL_TIME, savedOrg);
        employee.setDepartment(savedDept);
        employee.setUser(savedUser);
        Employee savedEmployee = entityManager.persistAndFlush(employee);
        
        // Verify all relationships are maintained
        assertThat(savedEmployee.getOrganization().getId()).isEqualTo(savedOrg.getId());
        assertThat(savedEmployee.getDepartment().getId()).isEqualTo(savedDept.getId());
        assertThat(savedEmployee.getUser().getId()).isEqualTo(savedUser.getId());
        
        // Verify bidirectional relationships
        assertThat(savedDept.getOrganization().getId()).isEqualTo(savedOrg.getId());
        assertThat(savedUser.getOrganization().getId()).isEqualTo(savedOrg.getId());
    }

    @Test
    void shouldPreventDeletionOfReferencedEntities() {
        // Create organization with dependent entities
        Organization organization = new Organization("Test Corp");
        organization.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg = entityManager.persistAndFlush(organization);
        
        // Create department that references the organization
        Department department = new Department("Engineering", "ENG", savedOrg);
        entityManager.persistAndFlush(department);
        
        // Try to delete the organization while it's still referenced
        // This should fail due to foreign key constraint
        assertThatThrownBy(() -> {
            entityManager.remove(savedOrg);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowDeletionWhenNoReferences() {
        // Create organization
        Organization organization = new Organization("Test Corp");
        organization.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg = entityManager.persistAndFlush(organization);
        
        // Create and then delete department
        Department department = new Department("Engineering", "ENG", savedOrg);
        Department savedDept = entityManager.persistAndFlush(department);
        
        // Delete the department first
        entityManager.remove(savedDept);
        entityManager.flush();
        
        // Now we should be able to delete the organization
        entityManager.remove(savedOrg);
        entityManager.flush();
        
        // Verify both are deleted
        assertThat(entityManager.find(Department.class, savedDept.getId())).isNull();
        assertThat(entityManager.find(Organization.class, savedOrg.getId())).isNull();
    }

    @Test
    void shouldEnforceUniqueConstraints() {
        // Test unique constraints are enforced
        Organization org1 = new Organization("Test Corp");
        org1.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg = entityManager.persistAndFlush(org1);
        
        // Try to create a user with duplicate username
        User user1 = new User("john_doe", "john1@test.com", "password", savedOrg);
        entityManager.persistAndFlush(user1);
        
        User user2 = new User("john_doe", "john2@test.com", "password", savedOrg);
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user2);
        }).isInstanceOf(Exception.class);
        
        // Try to create a user with duplicate email
        User user3 = new User("jane_doe", "john1@test.com", "password", savedOrg);
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user3);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldEnforceEmployeeNumberUniquenessWithinOrganization() {
        // Create two organizations
        Organization org1 = new Organization("Corp 1");
        org1.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg1 = entityManager.persistAndFlush(org1);
        
        Organization org2 = new Organization("Corp 2");
        org2.setCompanySize(CompanySize.MEDIUM);
        Organization savedOrg2 = entityManager.persistAndFlush(org2);
        
        // Create employee in first organization
        Employee emp1 = new Employee("EMP001", "John", "Doe", 
                                   LocalDate.now(), EmploymentStatus.ACTIVE, 
                                   EmploymentType.FULL_TIME, savedOrg1);
        entityManager.persistAndFlush(emp1);
        
        // Should be able to create employee with same number in different organization
        Employee emp2 = new Employee("EMP001", "Jane", "Smith", 
                                   LocalDate.now(), EmploymentStatus.ACTIVE, 
                                   EmploymentType.FULL_TIME, savedOrg2);
        Employee savedEmp2 = entityManager.persistAndFlush(emp2);
        assertThat(savedEmp2.getId()).isNotNull();
        
        // Should NOT be able to create employee with same number in same organization
        Employee emp3 = new Employee("EMP001", "Bob", "Johnson", 
                                   LocalDate.now(), EmploymentStatus.ACTIVE, 
                                   EmploymentType.FULL_TIME, savedOrg1);
        
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(emp3);
        }).isInstanceOf(Exception.class);
    }
}

