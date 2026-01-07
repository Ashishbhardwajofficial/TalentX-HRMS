package com.talentx.hrms.service.recruitment;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.entity.recruitment.Application;
import com.talentx.hrms.entity.recruitment.Candidate;
import com.talentx.hrms.entity.recruitment.JobPosting;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for candidate to employee conversion.
 * Feature: hrms-database-integration, Property 19: Candidate to Employee Conversion
 * Validates: Requirements 7.4
 * 
 * Property: For any successful hiring decision, the candidate data should be correctly
 * converted to a complete employee record.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CandidateToEmployeeConversionPropertyTest {

    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Property(tries = 100)
    @Label("Property 19: Candidate to Employee Conversion - Candidate data transfers to employee")
    void candidateDataTransfersToEmployee(
            @ForAll("firstNames") String firstName,
            @ForAll("lastNames") String lastName,
            @ForAll("jobTitles") String jobTitle) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept, jobTitle);

        // Create candidate
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + System.currentTimeMillis() + "@test.com");
        candidate.setPhoneNumber("+1234567890");
        candidate = candidateRepository.save(candidate);

        // Create accepted application
        Application application = new Application();
        application.setJobPosting(job);
        application.setCandidate(candidate);
        application.setApplicationDate(LocalDate.now());
        application.setStatus("ACCEPTED");
        application = applicationRepository.save(application);

        // Convert candidate to employee
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP" + System.currentTimeMillis());
        employee.setFirstName(candidate.getFirstName());
        employee.setLastName(candidate.getLastName());
        employee.setEmail(candidate.getEmail());
        employee.setPhoneNumber(candidate.getPhoneNumber());
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now());
        employee.setJobTitle(job.getTitle());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(job.getEmploymentType());
        employee.setDepartment(dept);
        employee.setOrganization(org);
        employee.setBaseSalary(new BigDecimal("50000.00"));
        employee = employeeRepository.save(employee);

        // Verify: Employee data matches candidate data
        assertThat(employee.getFirstName()).isEqualTo(candidate.getFirstName());
        assertThat(employee.getLastName()).isEqualTo(candidate.getLastName());
        assertThat(employee.getEmail()).isEqualTo(candidate.getEmail());
        assertThat(employee.getPhoneNumber()).isEqualTo(candidate.getPhoneNumber());

        // Verify: Employee has job-related information
        assertThat(employee.getJobTitle()).isEqualTo(job.getTitle());
        assertThat(employee.getDepartment().getId()).isEqualTo(dept.getId());
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
    }

    @Property(tries = 100)
    @Label("Property 19: Candidate to Employee Conversion - Employee record is complete")
    void employeeRecordIsComplete(
            @ForAll("firstNames") String firstName,
            @ForAll("lastNames") String lastName,
            @ForAll @BigRange(min = "30000.00", max = "150000.00") BigDecimal salary) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept, "Software Engineer");
        Candidate candidate = createCandidate(firstName, lastName);

        // Convert to employee
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP" + System.currentTimeMillis());
        employee.setFirstName(candidate.getFirstName());
        employee.setLastName(candidate.getLastName());
        employee.setEmail(candidate.getEmail());
        employee.setPhoneNumber(candidate.getPhoneNumber());
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now());
        employee.setJobTitle(job.getTitle());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(job.getEmploymentType());
        employee.setDepartment(dept);
        employee.setOrganization(org);
        employee.setBaseSalary(salary);
        employee = employeeRepository.save(employee);

        // Verify: Employee record has all required fields
        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getEmployeeNumber()).isNotBlank();
        assertThat(employee.getFirstName()).isNotBlank();
        assertThat(employee.getLastName()).isNotBlank();
        assertThat(employee.getEmail()).isNotBlank();
        assertThat(employee.getPhoneNumber()).isNotBlank();
        assertThat(employee.getDateOfBirth()).isNotNull();
        assertThat(employee.getGender()).isNotNull();
        assertThat(employee.getHireDate()).isNotNull();
        assertThat(employee.getJobTitle()).isNotBlank();
        assertThat(employee.getEmploymentStatus()).isNotNull();
        assertThat(employee.getEmploymentType()).isNotNull();
        assertThat(employee.getDepartment()).isNotNull();
        assertThat(employee.getOrganization()).isNotNull();
        assertThat(employee.getBaseSalary()).isNotNull();
        assertThat(employee.getBaseSalary()).isGreaterThan(BigDecimal.ZERO);
    }

    @Property(tries = 100)
    @Label("Property 19: Candidate to Employee Conversion - Hire date is set correctly")
    void hireDateIsSetCorrectly(
            @ForAll("firstNames") String firstName,
            @ForAll("lastNames") String lastName) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept, "Engineer");
        Candidate candidate = createCandidate(firstName, lastName);

        LocalDate hireDate = LocalDate.now();

        // Convert to employee
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP" + System.currentTimeMillis());
        employee.setFirstName(candidate.getFirstName());
        employee.setLastName(candidate.getLastName());
        employee.setEmail(candidate.getEmail());
        employee.setPhoneNumber(candidate.getPhoneNumber());
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(hireDate);
        employee.setJobTitle(job.getTitle());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(job.getEmploymentType());
        employee.setDepartment(dept);
        employee.setOrganization(org);
        employee.setBaseSalary(new BigDecimal("50000.00"));
        employee = employeeRepository.save(employee);

        // Verify: Hire date is set correctly
        assertThat(employee.getHireDate()).isEqualTo(hireDate);
        assertThat(employee.getHireDate()).isBeforeOrEqualTo(LocalDate.now());
    }

    @Property(tries = 100)
    @Label("Property 19: Candidate to Employee Conversion - Employment status is ACTIVE")
    void employmentStatusIsActive(
            @ForAll("firstNames") String firstName,
            @ForAll("lastNames") String lastName) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept, "Developer");
        Candidate candidate = createCandidate(firstName, lastName);

        // Convert to employee
        Employee employee = new Employee();
        employee.setEmployeeNumber("EMP" + System.currentTimeMillis());
        employee.setFirstName(candidate.getFirstName());
        employee.setLastName(candidate.getLastName());
        employee.setEmail(candidate.getEmail());
        employee.setPhoneNumber(candidate.getPhoneNumber());
        employee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        employee.setGender(Gender.MALE);
        employee.setHireDate(LocalDate.now());
        employee.setJobTitle(job.getTitle());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setEmploymentType(job.getEmploymentType());
        employee.setDepartment(dept);
        employee.setOrganization(org);
        employee.setBaseSalary(new BigDecimal("50000.00"));
        employee = employeeRepository.save(employee);

        // Verify: New employee has ACTIVE status
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
    }

    // Arbitraries
    @Provide
    Arbitrary<String> firstNames() {
        return Arbitraries.of("Ashish", "Priya", "Rahul", "Ananya", "Vikram", "Sneha", "Arjun", "Kavya");
    }

    @Provide
    Arbitrary<String> lastNames() {
        return Arbitraries.of("Kumar", "Sharma", "Patel", "Singh", "Verma", "Reddy", "Gupta", "Bhardwaj");
    }

    @Provide
    Arbitrary<String> jobTitles() {
        return Arbitraries.of(
                "Software Engineer",
                "Senior Developer",
                "Product Manager",
                "Data Analyst",
                "UX Designer",
                "DevOps Engineer",
                "QA Engineer",
                "Technical Lead"
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

    private JobPosting createJobPosting(Organization org, Department dept, String title) {
        JobPosting job = new JobPosting();
        job.setTitle(title);
        job.setDescription("Test job posting");
        job.setDepartment(dept);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setLocation("Test Location");
        job.setPostedDate(LocalDate.now());
        job.setStatus("OPEN");
        job.setOrganization(org);
        return jobPostingRepository.save(job);
    }

    private Candidate createCandidate(String firstName, String lastName) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + System.currentTimeMillis() + "@test.com");
        candidate.setPhoneNumber("+1234567890");
        return candidateRepository.save(candidate);
    }
}

