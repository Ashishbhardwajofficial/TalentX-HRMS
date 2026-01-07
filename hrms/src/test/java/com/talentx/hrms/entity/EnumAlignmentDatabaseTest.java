package com.talentx.hrms.entity;

import com.talentx.hrms.entity.attendance.AttendanceRecord;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.AttendanceStatus;
import com.talentx.hrms.entity.enums.CompanySize;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.LeaveStatus;
import com.talentx.hrms.entity.enums.PayrollStatus;
import com.talentx.hrms.entity.leave.LeaveRequest;
import com.talentx.hrms.entity.leave.LeaveType;
import com.talentx.hrms.entity.payroll.PayrollRun;
import com.talentx.hrms.repository.AttendanceRecordRepository;
import com.talentx.hrms.repository.EmployeeRepository;
import com.talentx.hrms.repository.LeaveRequestRepository;
import com.talentx.hrms.repository.PayrollRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: three-layer-enum-alignment, Task 4: Test Database Operations with New Enum Values**
 * 
 * This test validates that the database can properly store and retrieve records
 * with the new enum values added in the alignment migration:
 * - EmploymentStatus: PROBATION, NOTICE_PERIOD
 * - PayrollStatus: CALCULATED, REJECTED, ERROR
 * - AttendanceStatus: WORK_FROM_HOME, OVERTIME, COMP_OFF
 * - LeaveStatus: WITHDRAWN, EXPIRED
 * 
 * Requirements: 1.4, 1.5, 2.4, 2.5, 2.6, 3.4, 3.5, 3.6, 4.4, 4.5, 8.1, 8.4
 */
@DataJpaTest
@ActiveProfiles("test")
public class EnumAlignmentDatabaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private Organization testOrganization;
    private Employee testEmployee;
    private LeaveType testLeaveType;

    @BeforeEach
    void setUp() {
        // Create test organization
        testOrganization = new Organization("Test Organization");
        testOrganization.setCompanySize(CompanySize.MEDIUM);
        testOrganization = entityManager.persistAndFlush(testOrganization);

        // Create test employee for relationships
        testEmployee = new Employee(
            "EMP001",
            "Test",
            "Employee",
            LocalDate.now(),
            EmploymentStatus.ACTIVE,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        testEmployee = entityManager.persistAndFlush(testEmployee);

        // Create test leave type for leave requests
        testLeaveType = new LeaveType("Annual Leave", testOrganization);
        testLeaveType.setDefaultDays(20);
        testLeaveType.setRequiresApproval(true);
        testLeaveType = entityManager.persistAndFlush(testLeaveType);
    }

    // ========== Task 4.1: Test creating records with new enum values ==========

    @Test
    void testCreateEmployeeWithProbationStatus() {
        // Create employee with PROBATION status
        Employee employee = new Employee(
            "EMP_PROB_001",
            "John",
            "Probation",
            LocalDate.now(),
            EmploymentStatus.PROBATION,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        employee.setProbationEndDate(LocalDate.now().plusMonths(3));

        Employee saved = entityManager.persistAndFlush(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(saved.getEmployeeNumber()).isEqualTo("EMP_PROB_001");
    }

    @Test
    void testCreateEmployeeWithNoticePeriodStatus() {
        // Create employee with NOTICE_PERIOD status
        Employee employee = new Employee(
            "EMP_NOTICE_001",
            "Jane",
            "Notice",
            LocalDate.now().minusYears(2),
            EmploymentStatus.NOTICE_PERIOD,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        employee.setTerminationDate(LocalDate.now().plusMonths(1));

        Employee saved = entityManager.persistAndFlush(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmploymentStatus()).isEqualTo(EmploymentStatus.NOTICE_PERIOD);
        assertThat(saved.getEmployeeNumber()).isEqualTo("EMP_NOTICE_001");
    }

    @Test
    void testCreatePayrollRunWithCalculatedStatus() {
        // Create payroll run with CALCULATED status
        PayrollRun payrollRun = new PayrollRun(
            "Payroll Run - Calculated",
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(15),
            LocalDate.now().plusDays(5),
            testOrganization
        );
        payrollRun.setStatus(PayrollStatus.CALCULATED);
        payrollRun.setTotalGrossPay(new BigDecimal("50000.00"));
        payrollRun.setEmployeeCount(10);

        PayrollRun saved = entityManager.persistAndFlush(payrollRun);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(PayrollStatus.CALCULATED);
        assertThat(saved.getName()).isEqualTo("Payroll Run - Calculated");
    }

    @Test
    void testCreatePayrollRunWithRejectedStatus() {
        // Create payroll run with REJECTED status
        PayrollRun payrollRun = new PayrollRun(
            "Payroll Run - Rejected",
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(15),
            LocalDate.now().plusDays(5),
            testOrganization
        );
        payrollRun.setStatus(PayrollStatus.REJECTED);
        payrollRun.setNotes("Rejected due to calculation errors");

        PayrollRun saved = entityManager.persistAndFlush(payrollRun);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(PayrollStatus.REJECTED);
        assertThat(saved.getName()).isEqualTo("Payroll Run - Rejected");
    }

    @Test
    void testCreatePayrollRunWithErrorStatus() {
        // Create payroll run with ERROR status
        PayrollRun payrollRun = new PayrollRun(
            "Payroll Run - Error",
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(15),
            LocalDate.now().plusDays(5),
            testOrganization
        );
        payrollRun.setStatus(PayrollStatus.ERROR);
        payrollRun.setNotes("System error during processing");

        PayrollRun saved = entityManager.persistAndFlush(payrollRun);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(PayrollStatus.ERROR);
        assertThat(saved.getName()).isEqualTo("Payroll Run - Error");
    }

    @Test
    void testCreateAttendanceRecordWithWorkFromHomeStatus() {
        // Create attendance record with WORK_FROM_HOME status
        AttendanceRecord record = new AttendanceRecord(
            testEmployee,
            LocalDate.now(),
            AttendanceStatus.WORK_FROM_HOME
        );
        record.setTotalHours(new BigDecimal("8.00"));
        record.setNotes("Working from home today");

        AttendanceRecord saved = entityManager.persistAndFlush(record);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.WORK_FROM_HOME);
        assertThat(saved.getEmployee().getId()).isEqualTo(testEmployee.getId());
    }

    @Test
    void testCreateAttendanceRecordWithOvertimeStatus() {
        // Create attendance record with OVERTIME status
        AttendanceRecord record = new AttendanceRecord(
            testEmployee,
            LocalDate.now().minusDays(1),
            AttendanceStatus.OVERTIME
        );
        record.setTotalHours(new BigDecimal("10.00"));
        record.setOvertimeHours(new BigDecimal("2.00"));
        record.setNotes("Overtime work for project deadline");

        AttendanceRecord saved = entityManager.persistAndFlush(record);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.OVERTIME);
        assertThat(saved.getOvertimeHours()).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    @Test
    void testCreateAttendanceRecordWithCompOffStatus() {
        // Create attendance record with COMP_OFF status
        AttendanceRecord record = new AttendanceRecord(
            testEmployee,
            LocalDate.now().minusDays(2),
            AttendanceStatus.COMP_OFF
        );
        record.setNotes("Compensatory off for weekend work");

        AttendanceRecord saved = entityManager.persistAndFlush(record);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AttendanceStatus.COMP_OFF);
        assertThat(saved.getEmployee().getId()).isEqualTo(testEmployee.getId());
    }

    @Test
    void testCreateLeaveRequestWithWithdrawnStatus() {
        // Create leave request with WITHDRAWN status
        LeaveRequest leaveRequest = new LeaveRequest(
            testEmployee,
            testLeaveType,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(12),
            new BigDecimal("3.00")
        );
        leaveRequest.setStatus(LeaveStatus.WITHDRAWN);
        leaveRequest.setReason("Personal reasons");
        leaveRequest.setReviewComments("Withdrawn by employee");

        LeaveRequest saved = entityManager.persistAndFlush(leaveRequest);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(LeaveStatus.WITHDRAWN);
        assertThat(saved.getEmployee().getId()).isEqualTo(testEmployee.getId());
    }

    @Test
    void testCreateLeaveRequestWithExpiredStatus() {
        // Create leave request with EXPIRED status
        LeaveRequest leaveRequest = new LeaveRequest(
            testEmployee,
            testLeaveType,
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(28),
            new BigDecimal("3.00")
        );
        leaveRequest.setStatus(LeaveStatus.EXPIRED);
        leaveRequest.setReason("Vacation");
        leaveRequest.setReviewComments("Request expired without action");

        LeaveRequest saved = entityManager.persistAndFlush(leaveRequest);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(LeaveStatus.EXPIRED);
        assertThat(saved.getEmployee().getId()).isEqualTo(testEmployee.getId());
    }

    // ========== Task 4.2: Test retrieving records with new enum values ==========

    @Test
    void testQueryEmployeeByProbationStatus() {
        // Create multiple employees with different statuses
        Employee probationEmployee = new Employee(
            "EMP_PROB_002",
            "Alice",
            "Probation",
            LocalDate.now(),
            EmploymentStatus.PROBATION,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        entityManager.persistAndFlush(probationEmployee);

        Employee activeEmployee = new Employee(
            "EMP_ACTIVE_001",
            "Bob",
            "Active",
            LocalDate.now().minusYears(1),
            EmploymentStatus.ACTIVE,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        entityManager.persistAndFlush(activeEmployee);

        // Query by PROBATION status
        List<Employee> probationEmployees = employeeRepository.findByOrganizationAndEmploymentStatus(
            testOrganization,
            EmploymentStatus.PROBATION
        );

        assertThat(probationEmployees).isNotEmpty();
        assertThat(probationEmployees).hasSize(1);
        assertThat(probationEmployees.get(0).getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(probationEmployees.get(0).getEmployeeNumber()).isEqualTo("EMP_PROB_002");
    }

    @Test
    void testQueryPayrollRunByCalculatedStatus() {
        // Create multiple payroll runs with different statuses
        PayrollRun calculatedRun = new PayrollRun(
            "Calculated Payroll",
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(15),
            LocalDate.now().plusDays(5),
            testOrganization
        );
        calculatedRun.setStatus(PayrollStatus.CALCULATED);
        entityManager.persistAndFlush(calculatedRun);

        PayrollRun draftRun = new PayrollRun(
            "Draft Payroll",
            LocalDate.now().withDayOfMonth(16),
            LocalDate.now().withDayOfMonth(30),
            LocalDate.now().plusDays(10),
            testOrganization
        );
        draftRun.setStatus(PayrollStatus.DRAFT);
        entityManager.persistAndFlush(draftRun);

        // Query by CALCULATED status
        List<PayrollRun> calculatedRuns = payrollRunRepository.findByOrganizationAndStatus(
            testOrganization,
            PayrollStatus.CALCULATED
        );

        assertThat(calculatedRuns).isNotEmpty();
        assertThat(calculatedRuns).hasSize(1);
        assertThat(calculatedRuns.get(0).getStatus()).isEqualTo(PayrollStatus.CALCULATED);
        assertThat(calculatedRuns.get(0).getName()).isEqualTo("Calculated Payroll");
    }

    @Test
    void testQueryAttendanceRecordByWorkFromHomeStatus() {
        // Create multiple attendance records with different statuses
        AttendanceRecord wfhRecord = new AttendanceRecord(
            testEmployee,
            LocalDate.now().minusDays(3),
            AttendanceStatus.WORK_FROM_HOME
        );
        wfhRecord.setTotalHours(new BigDecimal("8.00"));
        entityManager.persistAndFlush(wfhRecord);

        AttendanceRecord presentRecord = new AttendanceRecord(
            testEmployee,
            LocalDate.now().minusDays(4),
            AttendanceStatus.PRESENT
        );
        presentRecord.setTotalHours(new BigDecimal("8.00"));
        entityManager.persistAndFlush(presentRecord);

        // Query by WORK_FROM_HOME status
        List<AttendanceRecord> wfhRecords = attendanceRecordRepository.findByEmployeeAndStatus(
            testEmployee,
            AttendanceStatus.WORK_FROM_HOME
        );

        assertThat(wfhRecords).isNotEmpty();
        assertThat(wfhRecords).hasSize(1);
        assertThat(wfhRecords.get(0).getStatus()).isEqualTo(AttendanceStatus.WORK_FROM_HOME);
        assertThat(wfhRecords.get(0).getAttendanceDate()).isEqualTo(LocalDate.now().minusDays(3));
    }

    @Test
    void testQueryLeaveRequestByWithdrawnStatus() {
        // Create multiple leave requests with different statuses
        LeaveRequest withdrawnRequest = new LeaveRequest(
            testEmployee,
            testLeaveType,
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(7),
            new BigDecimal("3.00")
        );
        withdrawnRequest.setStatus(LeaveStatus.WITHDRAWN);
        withdrawnRequest.setReason("Changed plans");
        entityManager.persistAndFlush(withdrawnRequest);

        LeaveRequest pendingRequest = new LeaveRequest(
            testEmployee,
            testLeaveType,
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(17),
            new BigDecimal("3.00")
        );
        pendingRequest.setStatus(LeaveStatus.PENDING);
        pendingRequest.setReason("Vacation");
        entityManager.persistAndFlush(pendingRequest);

        // Query by WITHDRAWN status
        List<LeaveRequest> withdrawnRequests = leaveRequestRepository.findByEmployeeAndStatus(
            testEmployee,
            LeaveStatus.WITHDRAWN
        );

        assertThat(withdrawnRequests).isNotEmpty();
        assertThat(withdrawnRequests).hasSize(1);
        assertThat(withdrawnRequests.get(0).getStatus()).isEqualTo(LeaveStatus.WITHDRAWN);
        assertThat(withdrawnRequests.get(0).getReason()).isEqualTo("Changed plans");
    }

    @Test
    void testVerifyAllQueriesReturnCorrectResults() {
        // Create one record of each new enum type
        Employee probationEmp = new Employee(
            "EMP_VERIFY_001",
            "Verify",
            "Test",
            LocalDate.now(),
            EmploymentStatus.PROBATION,
            EmploymentType.FULL_TIME,
            testOrganization
        );
        entityManager.persistAndFlush(probationEmp);

        PayrollRun errorRun = new PayrollRun(
            "Error Payroll",
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(15),
            LocalDate.now().plusDays(5),
            testOrganization
        );
        errorRun.setStatus(PayrollStatus.ERROR);
        entityManager.persistAndFlush(errorRun);

        AttendanceRecord overtimeRecord = new AttendanceRecord(
            testEmployee,
            LocalDate.now().minusDays(5),
            AttendanceStatus.OVERTIME
        );
        overtimeRecord.setOvertimeHours(new BigDecimal("3.00"));
        entityManager.persistAndFlush(overtimeRecord);

        LeaveRequest expiredRequest = new LeaveRequest(
            testEmployee,
            testLeaveType,
            LocalDate.now().minusDays(60),
            LocalDate.now().minusDays(58),
            new BigDecimal("3.00")
        );
        expiredRequest.setStatus(LeaveStatus.EXPIRED);
        entityManager.persistAndFlush(expiredRequest);

        // Verify all queries return correct results
        List<Employee> probationEmps = employeeRepository.findByOrganizationAndEmploymentStatus(
            testOrganization,
            EmploymentStatus.PROBATION
        );
        assertThat(probationEmps).hasSizeGreaterThanOrEqualTo(1);

        List<PayrollRun> errorRuns = payrollRunRepository.findByOrganizationAndStatus(
            testOrganization,
            PayrollStatus.ERROR
        );
        assertThat(errorRuns).hasSize(1);

        List<AttendanceRecord> overtimeRecords = attendanceRecordRepository.findByEmployeeAndStatus(
            testEmployee,
            AttendanceStatus.OVERTIME
        );
        assertThat(overtimeRecords).hasSize(1);

        List<LeaveRequest> expiredRequests = leaveRequestRepository.findByEmployeeAndStatus(
            testEmployee,
            LeaveStatus.EXPIRED
        );
        assertThat(expiredRequests).hasSize(1);
    }
}
