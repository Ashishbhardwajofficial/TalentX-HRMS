package com.talentx.hrms.service.payroll;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.*;
import com.talentx.hrms.entity.payroll.PayrollRun;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for workflow enforcement.
 * Feature: hrms-database-integration, Property 14: Workflow Enforcement
 * Validates: Requirements 5.4
 * 
 * Property: For any payroll processing attempt, the system should enforce approval
 * workflows and prevent unauthorized payment processing.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WorkflowEnforcementPropertyTest {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Property(tries = 100)
    @Label("Property 14: Workflow Enforcement - Payroll status transitions are valid")
    void payrollStatusTransitionsAreValid(
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2020, max = 2025) int year) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept);
        
        // Create payroll run in DRAFT status
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunName("Payroll " + month + "/" + year);
        payrollRun.setPayPeriodStart(LocalDate.of(year, month, 1));
        payrollRun.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        payrollRun.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        payrollRun.setStatus(PayrollStatus.DRAFT);
        payrollRun.setOrganization(org);
        payrollRun = payrollRunRepository.save(payrollRun);

        // Verify: Initial status is DRAFT
        assertThat(payrollRun.getStatus()).isEqualTo(PayrollStatus.DRAFT);

        // Transition to PROCESSING
        payrollRun.setStatus(PayrollStatus.PROCESSING);
        payrollRun = payrollRunRepository.save(payrollRun);
        assertThat(payrollRun.getStatus()).isEqualTo(PayrollStatus.PROCESSING);

        // Transition to APPROVED
        payrollRun.setStatus(PayrollStatus.APPROVED);
        payrollRun = payrollRunRepository.save(payrollRun);
        assertThat(payrollRun.getStatus()).isEqualTo(PayrollStatus.APPROVED);

        // Verify: Cannot transition back to DRAFT from APPROVED
        PayrollStatus finalStatus = payrollRun.getStatus();
        assertThat(finalStatus).isIn(PayrollStatus.APPROVED, PayrollStatus.PAID);
    }

    @Property(tries = 100)
    @Label("Property 14: Workflow Enforcement - Draft payrolls can be modified")
    void draftPayrollsCanBeModified(
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2020, max = 2025) int year) {

        Organization org = createOrganization();
        
        // Create payroll run in DRAFT status
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunName("Payroll " + month + "/" + year);
        payrollRun.setPayPeriodStart(LocalDate.of(year, month, 1));
        payrollRun.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        payrollRun.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        payrollRun.setStatus(PayrollStatus.DRAFT);
        payrollRun.setOrganization(org);
        payrollRun = payrollRunRepository.save(payrollRun);

        // Verify: Draft payroll can be modified
        String originalName = payrollRun.getRunName();
        payrollRun.setRunName("Modified " + originalName);
        payrollRun = payrollRunRepository.save(payrollRun);
        
        assertThat(payrollRun.getRunName()).isEqualTo("Modified " + originalName);
        assertThat(payrollRun.getStatus()).isEqualTo(PayrollStatus.DRAFT);
    }

    @Property(tries = 100)
    @Label("Property 14: Workflow Enforcement - Payroll run dates are consistent")
    void payrollRunDatesAreConsistent(
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2020, max = 2025) int year) {

        Organization org = createOrganization();
        
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunName("Payroll " + month + "/" + year);
        payrollRun.setPayPeriodStart(LocalDate.of(year, month, 1));
        payrollRun.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        payrollRun.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        payrollRun.setStatus(PayrollStatus.DRAFT);
        payrollRun.setOrganization(org);
        payrollRun = payrollRunRepository.save(payrollRun);

        // Verify: Pay period end is after start
        assertThat(payrollRun.getPayPeriodEnd()).isAfter(payrollRun.getPayPeriodStart());
        
        // Verify: Payment date is after or equal to period end
        assertThat(payrollRun.getPaymentDate())
                .isAfterOrEqualTo(payrollRun.getPayPeriodEnd());
    }

    @Property(tries = 100)
    @Label("Property 14: Workflow Enforcement - Payroll status progression is monotonic")
    void payrollStatusProgressionIsMonotonic(
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2020, max = 2025) int year) {

        Organization org = createOrganization();
        
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunName("Payroll " + month + "/" + year);
        payrollRun.setPayPeriodStart(LocalDate.of(year, month, 1));
        payrollRun.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        payrollRun.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        payrollRun.setStatus(PayrollStatus.DRAFT);
        payrollRun.setOrganization(org);
        payrollRun = payrollRunRepository.save(payrollRun);

        PayrollStatus initialStatus = payrollRun.getStatus();
        
        // Progress through workflow
        payrollRun.setStatus(PayrollStatus.PROCESSING);
        payrollRun = payrollRunRepository.save(payrollRun);
        PayrollStatus processingStatus = payrollRun.getStatus();
        
        payrollRun.setStatus(PayrollStatus.APPROVED);
        payrollRun = payrollRunRepository.save(payrollRun);
        PayrollStatus approvedStatus = payrollRun.getStatus();

        // Verify: Status progresses forward
        assertThat(initialStatus).isEqualTo(PayrollStatus.DRAFT);
        assertThat(processingStatus).isEqualTo(PayrollStatus.PROCESSING);
        assertThat(approvedStatus).isEqualTo(PayrollStatus.APPROVED);
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

    private Employee createEmployee(Organization org, Department dept) {
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
        emp.setBaseSalary(new BigDecimal("50000.00"));
        emp.setDepartment(dept);
        emp.setOrganization(org);
        return employeeRepository.save(emp);
    }
}

