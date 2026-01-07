package com.talentx.hrms.entity;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Location;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 1: Entity CRUD Operations Completeness**
 * 
 * Test that validates CRUD operations work correctly for all HRMS entities.
 * This test ensures that for any HRMS entity type (Employee, Department, Organization, etc.), 
 * all basic CRUD operations work correctly with proper database persistence and retrieval.
 */
@DataJpaTest
@ActiveProfiles("test")
public class EntityCrudOperationsPropertyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void organizationCrudOperations() {
        // Test with multiple different organization configurations
        String[] names = {"TechCorp", "Global Industries", "StartupXYZ"};
        CompanySize[] sizes = {CompanySize.STARTUP, CompanySize.MEDIUM, CompanySize.ENTERPRISE};
        
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            CompanySize companySize = sizes[i];
            
            // CREATE
            Organization organization = new Organization(name);
            organization.setCompanySize(companySize);
            organization.setLegalName(name + " LLC");
            
            Organization saved = entityManager.persistAndFlush(organization);
            
            Long id = saved.getId();
            assertThat(id).isNotNull();
            
            // READ
            Organization retrieved = entityManager.find(Organization.class, id);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo(name);
            assertThat(retrieved.getCompanySize()).isEqualTo(companySize);
            
            // UPDATE
            String updatedName = name + " Updated";
            retrieved.setName(updatedName);
            Organization updated = entityManager.persistAndFlush(retrieved);
            
            assertThat(updated).isNotNull();
            assertThat(updated.getName()).isEqualTo(updatedName);
            
            // DELETE
            entityManager.remove(updated);
            entityManager.flush();
            
            Organization deleted = entityManager.find(Organization.class, id);
            assertThat(deleted).isNull();
        }
    }

    @Test
    void departmentCrudOperations() {
        // Test with multiple different department configurations
        String[] names = {"Engineering", "Marketing", "Sales"};
        String[] codes = {"ENG", "MKT", "SAL"};
        
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String code = codes[i];
            
            // Create test organization for this test
            Organization testOrg = new Organization("Test Org for " + name);
            testOrg.setCompanySize(CompanySize.MEDIUM);
            Organization savedOrg = entityManager.persistAndFlush(testOrg);
            
            // CREATE
            Department department = new Department(name, code, savedOrg);
            department.setDescription("Test department for " + name);
            
            Department saved = entityManager.persistAndFlush(department);
            
            Long id = saved.getId();
            assertThat(id).isNotNull();
            
            // READ
            Department retrieved = entityManager.find(Department.class, id);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo(name);
            assertThat(retrieved.getCode()).isEqualTo(code);
            assertThat(retrieved.getOrganization().getId()).isEqualTo(savedOrg.getId());
            
            // UPDATE
            String updatedName = name + " Updated";
            retrieved.setName(updatedName);
            Department updated = entityManager.persistAndFlush(retrieved);
            
            assertThat(updated).isNotNull();
            assertThat(updated.getName()).isEqualTo(updatedName);
            
            // DELETE
            entityManager.remove(updated);
            entityManager.flush();
            
            Department deleted = entityManager.find(Department.class, id);
            assertThat(deleted).isNull();
        }
    }

    @Test
    void locationCrudOperations() {
        // Test with multiple different location configurations
        String[] names = {"Headquarters", "Branch Office", "Remote Office"};
        String[] cities = {"New York", "Los Angeles", "Chicago"};
        String[] countries = {"USA", "Canada", "UK"};
        
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String city = cities[i];
            String country = countries[i];
            
            // Create test organization for this test
            Organization testOrg = new Organization("Test Org for " + name);
            testOrg.setCompanySize(CompanySize.MEDIUM);
            Organization savedOrg = entityManager.persistAndFlush(testOrg);
            
            // CREATE
            Location location = new Location(name, savedOrg);
            location.setCity(city);
            location.setCountry(country);
            location.setAddressLine1("123 Test Street");
            
            Location saved = entityManager.persistAndFlush(location);
            
            Long id = saved.getId();
            assertThat(id).isNotNull();
            
            // READ
            Location retrieved = entityManager.find(Location.class, id);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo(name);
            assertThat(retrieved.getCity()).isEqualTo(city);
            assertThat(retrieved.getCountry()).isEqualTo(country);
            
            // UPDATE
            String updatedCity = city + " Updated";
            retrieved.setCity(updatedCity);
            Location updated = entityManager.persistAndFlush(retrieved);
            
            assertThat(updated).isNotNull();
            assertThat(updated.getCity()).isEqualTo(updatedCity);
            
            // DELETE
            entityManager.remove(updated);
            entityManager.flush();
            
            Location deleted = entityManager.find(Location.class, id);
            assertThat(deleted).isNull();
        }
    }

    @Test
    void userCrudOperations() {
        // Test with multiple different user configurations
        String[] usernames = {"john_doe", "jane_smith", "admin_user"};
        String[] emails = {"john@test.com", "jane@test.com", "admin@test.com"};
        
        for (int i = 0; i < usernames.length; i++) {
            String username = usernames[i];
            String email = emails[i];
            
            // Create test organization for this test
            Organization testOrg = new Organization("Test Org for " + username);
            testOrg.setCompanySize(CompanySize.MEDIUM);
            Organization savedOrg = entityManager.persistAndFlush(testOrg);
            
            // CREATE
            User user = new User(username, email, "hashedPassword123", savedOrg);
            user.setFirstName("Test");
            user.setLastName("User");
            
            User saved = entityManager.persistAndFlush(user);
            
            Long id = saved.getId();
            assertThat(id).isNotNull();
            
            // READ
            User retrieved = entityManager.find(User.class, id);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUsername()).isEqualTo(username);
            assertThat(retrieved.getEmail()).isEqualTo(email);
            assertThat(retrieved.getOrganization().getId()).isEqualTo(savedOrg.getId());
            
            // UPDATE
            String updatedFirstName = "Updated";
            retrieved.setFirstName(updatedFirstName);
            User updated = entityManager.persistAndFlush(retrieved);
            
            assertThat(updated).isNotNull();
            assertThat(updated.getFirstName()).isEqualTo(updatedFirstName);
            
            // DELETE
            entityManager.remove(updated);
            entityManager.flush();
            
            User deleted = entityManager.find(User.class, id);
            assertThat(deleted).isNull();
        }
    }

    @Test
    void employeeCrudOperations() {
        // Test with multiple different employee configurations
        String[] employeeNumbers = {"EMP001", "EMP002", "EMP003"};
        String[] firstNames = {"John", "Jane", "Bob"};
        String[] lastNames = {"Smith", "Doe", "Johnson"};
        EmploymentStatus[] statuses = {EmploymentStatus.ACTIVE, EmploymentStatus.PROBATION, EmploymentStatus.ACTIVE};
        EmploymentType[] types = {EmploymentType.FULL_TIME, EmploymentType.PART_TIME, EmploymentType.CONTRACT};
        
        for (int i = 0; i < employeeNumbers.length; i++) {
            String employeeNumber = employeeNumbers[i];
            String firstName = firstNames[i];
            String lastName = lastNames[i];
            EmploymentStatus employmentStatus = statuses[i];
            EmploymentType employmentType = types[i];
            
            // Create test organization for this test
            Organization testOrg = new Organization("Test Org for " + employeeNumber);
            testOrg.setCompanySize(CompanySize.MEDIUM);
            Organization savedOrg = entityManager.persistAndFlush(testOrg);
            
            // CREATE
            Employee employee = new Employee(employeeNumber, firstName, lastName, 
                                           LocalDate.now(), employmentStatus, employmentType, savedOrg);
            employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.com");
            
            Employee saved = entityManager.persistAndFlush(employee);
            
            Long id = saved.getId();
            assertThat(id).isNotNull();
            
            // READ
            Employee retrieved = entityManager.find(Employee.class, id);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getEmployeeNumber()).isEqualTo(employeeNumber);
            assertThat(retrieved.getFirstName()).isEqualTo(firstName);
            assertThat(retrieved.getLastName()).isEqualTo(lastName);
            assertThat(retrieved.getEmploymentStatus()).isEqualTo(employmentStatus);
            assertThat(retrieved.getEmploymentType()).isEqualTo(employmentType);
            
            // UPDATE
            String updatedJobTitle = "Senior " + firstName;
            retrieved.setJobTitle(updatedJobTitle);
            Employee updated = entityManager.persistAndFlush(retrieved);
            
            assertThat(updated).isNotNull();
            assertThat(updated.getJobTitle()).isEqualTo(updatedJobTitle);
            
            // DELETE
            entityManager.remove(updated);
            entityManager.flush();
            
            Employee deleted = entityManager.find(Employee.class, id);
            assertThat(deleted).isNull();
        }
    }

}

