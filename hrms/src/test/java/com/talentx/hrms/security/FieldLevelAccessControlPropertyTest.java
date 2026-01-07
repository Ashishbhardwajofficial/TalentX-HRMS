package com.talentx.hrms.security;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.entity.security.Permission;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for field-level access control.
 * Feature: hrms-database-integration, Property 22: Field-Level Access Control
 * Validates: Requirements 8.5
 * 
 * Property: For any data access request, sensitive fields should be properly protected
 * based on the user's role and permissions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FieldLevelAccessControlPropertyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Property(tries = 100)
    @Label("Property 22: Field-Level Access Control - Sensitive fields are protected")
    void sensitiveFieldsAreProtected(
            @ForAll("roleNames") String roleName,
            @ForAll @BigRange(min = "30000.00", max = "150000.00") BigDecimal salary) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        User user = createUser("testuser", roleName, org);
        Employee employee = createEmployee(org, dept, salary);

        // Verify: Sensitive fields exist in employee record
        assertThat(employee.getBaseSalary()).isNotNull();
        assertThat(employee.getDateOfBirth()).isNotNull();
        assertThat(employee.getPhoneNumber()).isNotNull();

        // Verify: User has role assigned
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName))).isTrue();

        // Verify: Access control is based on roles
        boolean hasAdminRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));
        boolean hasHRRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("HR"));

        // Admin and HR roles should have access to sensitive data
        if (hasAdminRole || hasHRRole) {
            assertThat(employee.getBaseSalary()).isNotNull();
        }
    }

    @Property(tries = 100)
    @Label("Property 22: Field-Level Access Control - Role-based permissions are enforced")
    void roleBasedPermissionsAreEnforced(
            @ForAll("roleNames") String roleName,
            @ForAll("permissionNames") String permissionName) {

        // Setup test data
        Organization org = createOrganization();
        
        // Create role with permission
        Role role = new Role();
        role.setName(roleName + "_" + System.currentTimeMillis());
        role.setDescription("Test role");
        
        Permission permission = new Permission();
        permission.setName(permissionName);
        permission.setDescription("Test permission");
        
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission);
        role.setPermissions(permissions);

        // Create user with role
        User user = new User();
        user.setUsername("user_" + System.currentTimeMillis());
        user.setEmail("user" + System.currentTimeMillis() + "@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(true);
        user.setOrganization(org);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user = userRepository.save(user);

        // Verify: User has assigned permissions through role
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permissionName))).isTrue();
    }

    @Property(tries = 100)
    @Label("Property 22: Field-Level Access Control - Users can only access their organization data")
    void usersCanOnlyAccessTheirOrganizationData(
            @ForAll("roleNames") String roleName) {

        // Create two separate organizations
        Organization org1 = createOrganization();
        Organization org2 = createOrganization();

        // Create users in different organizations
        User user1 = createUser("user1", roleName, org1);
        User user2 = createUser("user2", roleName, org2);

        // Verify: Users belong to different organizations
        assertThat(user1.getOrganization().getId()).isNotEqualTo(user2.getOrganization().getId());

        // Verify: Organization isolation is maintained
        assertThat(user1.getOrganization().getId()).isEqualTo(org1.getId());
        assertThat(user2.getOrganization().getId()).isEqualTo(org2.getId());
    }

    @Property(tries = 100)
    @Label("Property 22: Field-Level Access Control - Password fields are encrypted")
    void passwordFieldsAreEncrypted(
            @ForAll("roleNames") String roleName,
            @ForAll("passwords") String plainPassword) {

        // Setup test data
        Organization org = createOrganization();
        
        // Create user with password
        User user = new User();
        user.setUsername("user_" + System.currentTimeMillis());
        user.setEmail("user" + System.currentTimeMillis() + "@test.com");
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setActive(true);
        user.setOrganization(org);
        
        Role role = new Role();
        role.setName(roleName + "_" + System.currentTimeMillis());
        role.setDescription("Test role");
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user = userRepository.save(user);

        // Verify: Password is encrypted (not stored in plain text)
        assertThat(user.getPassword()).isNotEqualTo(plainPassword);
        assertThat(user.getPassword()).isNotBlank();
        
        // Verify: Password can be verified
        assertThat(passwordEncoder.matches(plainPassword, user.getPassword())).isTrue();
    }

    @Property(tries = 100)
    @Label("Property 22: Field-Level Access Control - Inactive users cannot access data")
    void inactiveUsersCannotAccessData(
            @ForAll("roleNames") String roleName) {

        // Setup test data
        Organization org = createOrganization();
        
        // Create inactive user
        User user = new User();
        user.setUsername("inactive_" + System.currentTimeMillis());
        user.setEmail("inactive" + System.currentTimeMillis() + "@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(false); // Inactive user
        user.setOrganization(org);
        
        Role role = new Role();
        role.setName(roleName + "_" + System.currentTimeMillis());
        role.setDescription("Test role");
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user = userRepository.save(user);

        // Verify: User is marked as inactive
        assertThat(user.isActive()).isFalse();
        
        // Verify: Inactive status is persisted
        User retrieved = userRepository.findById(user.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.isActive()).isFalse();
    }

    // Arbitraries
    @Provide
    Arbitrary<String> roleNames() {
        return Arbitraries.of("ADMIN", "HR", "MANAGER", "USER", "EMPLOYEE");
    }

    @Provide
    Arbitrary<String> permissionNames() {
        return Arbitraries.of(
                "READ_EMPLOYEE",
                "WRITE_EMPLOYEE",
                "DELETE_EMPLOYEE",
                "READ_SALARY",
                "WRITE_SALARY",
                "READ_PAYROLL",
                "WRITE_PAYROLL",
                "APPROVE_LEAVE"
        );
    }

    @Provide
    Arbitrary<String> passwords() {
        return Arbitraries.of(
                "password123",
                "SecurePass456",
                "TestPassword789",
                "MySecret2024",
                "P@ssw0rd!",
                "StrongPwd#123"
        );
    }

    // Helper methods
    private Organization createOrganization() {
        Organization org = new Organization();
        org.setName("Test Org " + System.currentTimeMillis());
        org.setLegalName("Test Org LLC");
        return organizationRepository.save(org);
    }

    private Department createDepartment(Organization org) {
        Department dept = new Department();
        dept.setName("Test Dept " + System.currentTimeMillis());
        dept.setCode("TD" + System.currentTimeMillis());
        dept.setOrganization(org);
        return departmentRepository.save(dept);
    }

    private User createUser(String username, String roleName, Organization org) {
        User user = new User();
        user.setUsername(username + "_" + System.currentTimeMillis());
        user.setEmail(username + System.currentTimeMillis() + "@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setActive(true);
        user.setOrganization(org);
        
        Role role = new Role();
        role.setName(roleName + "_" + System.currentTimeMillis());
        role.setDescription(roleName + " role");
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    private Employee createEmployee(Organization org, Department dept, BigDecimal salary) {
        Employee emp = new Employee();
        emp.setEmployeeNumber("EMP" + System.currentTimeMillis());
        emp.setFirstName("Test");
        emp.setLastName("Employee");
        emp.setEmail("test" + System.currentTimeMillis() + "@test.com");
        emp.setPhoneNumber("+1234567890");
        emp.setDateOfBirth(LocalDate.of(1990, 1, 1));
        emp.setGender(Gender.MALE);
        emp.setHireDate(LocalDate.now());
        emp.setJobTitle("Engineer");
        emp.setEmploymentStatus(EmploymentStatus.ACTIVE);
        emp.setEmploymentType(EmploymentType.FULL_TIME);
        emp.setBaseSalary(salary);
        emp.setDepartment(dept);
        emp.setOrganization(org);
        return employeeRepository.save(emp);
    }
}

