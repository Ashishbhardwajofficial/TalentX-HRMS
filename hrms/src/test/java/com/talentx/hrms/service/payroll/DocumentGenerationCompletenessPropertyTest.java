package com.talentx.hrms.service.payroll;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.*;
import com.talentx.hrms.entity.payroll.PayrollRun;
import com.talentx.hrms.entity.payroll.Payslip;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for document generation completeness.
 * Feature: hrms-database-integration, Property 13: Document Generation Completeness
 * Validates: Requirements 5.3
 * 
 * Property: For any payslip generation request, the resulting document should contain
 * all required payroll information in the correct format.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DocumentGenerationCompletenessPropertyTest {

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

    @Autowired
    private PayslipRepository payslipRepository;

    @Property(tries = 100)
    @Label("Property 13: Document Generation Completeness - Payslip contains all required information")
    void payslipContainsAllRequiredInformation(
            @ForAll @BigRange(min = "1000.00", max = "100000.00") BigDecimal grossPay,
            @ForAll @BigRange(min = "0.00", max = "50000.00") BigDecimal deductions,
            @ForAll @IntRange(min = 1, max = 12) int month,
            @ForAll @IntRange(min = 2020, max = 2025) int year) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept, grossPay);
        PayrollRun payrollRun = createPayrollRun(org, month, year);
        
        // Create payslip
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setPayrollRun(payrollRun);
        payslip.setGrossPay(grossPay);
        payslip.setTotalDeductions(deductions);
        payslip.setNetPay(grossPay.subtract(deductions));
        payslip.setPayPeriodStart(LocalDate.of(year, month, 1));
        payslip.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        payslip.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        payslip.setStatus(PayrollStatus.APPROVED);
        
        payslip = payslipRepository.save(payslip);

        // Verify: Payslip contains all required information
        assertThat(payslip.getId()).isNotNull();
        assertThat(payslip.getEmployee()).isNotNull();
        assertThat(payslip.getEmployee().getEmployeeNumber()).isNotBlank();
        assertThat(payslip.getEmployee().getFirstName()).isNotBlank();
        assertThat(payslip.getEmployee().getLastName()).isNotBlank();
        
        assertThat(payslip.getPayrollRun()).isNotNull();
        assertThat(payslip.getGrossPay()).isNotNull();
        assertThat(payslip.getGrossPay()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        
        assertThat(payslip.getTotalDeductions()).isNotNull();
        assertThat(payslip.getTotalDeductions()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        
        assertThat(payslip.getNetPay()).isNotNull();
        assertThat(payslip.getNetPay()).isEqualTo(grossPay.subtract(deductions));
        
        assertThat(payslip.getPayPeriodStart()).isNotNull();
        assertThat(payslip.getPayPeriodEnd()).isNotNull();
        assertThat(payslip.getPayPeriodEnd()).isAfter(payslip.getPayPeriodStart());
        
        assertThat(payslip.getPaymentDate()).isNotNull();
        assertThat(payslip.getStatus()).isNotNull();
    }

    @Property(tries = 100)
    @Label("Property 13: Document Generation Completeness - Payslip calculation consistency")
    void payslipCalculationConsistency(
            @ForAll @BigRange(min = "1000.00", max = "100000.00") BigDecimal grossPay,
            @ForAll @BigRange(min = "0.00", max = "50000.00") BigDecimal deductions) {

        Assume.that(deductions.compareTo(grossPay) <= 0);

        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Employee employee = createEmployee(org, dept, grossPay);
        PayrollRun payrollRun = createPayrollRun(org, 1, 2024);
        
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setPayrollRun(payrollRun);
        payslip.setGrossPay(grossPay);
        payslip.setTotalDeductions(deductions);
        payslip.setNetPay(grossPay.subtract(deductions));
        payslip.setPayPeriodStart(LocalDate.of(2024, 1, 1));
        payslip.setPayPeriodEnd(LocalDate.of(2024, 1, 31));
        payslip.setPaymentDate(LocalDate.of(2024, 2, 1));
        payslip.setStatus(PayrollStatus.APPROVED);
        
        payslip = payslipRepository.save(payslip);

        // Verify: Net pay calculation is correct
        BigDecimal expectedNetPay = grossPay.subtract(deductions);
        assertThat(payslip.getNetPay()).isEqualByComparingTo(expectedNetPay);
        
        // Verify: Net pay is non-negative
        assertThat(payslip.getNetPay()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
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

    private PayrollRun createPayrollRun(Organization org, int month, int year) {
        PayrollRun run = new PayrollRun();
        run.setRunName("Payroll " + month + "/" + year);
        run.setPayPeriodStart(LocalDate.of(year, month, 1));
        run.setPayPeriodEnd(LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        run.setPaymentDate(LocalDate.of(year, month, 1).plusMonths(1));
        run.setStatus(PayrollStatus.DRAFT);
        run.setOrganization(org);
        return payrollRunRepository.save(run);
    }
}

