package com.talentx.hrms.repository;

import com.talentx.hrms.entity.attendance.AttendanceRecord;
import com.talentx.hrms.entity.attendance.Shift;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

       // Find attendance record by employee and date
       Optional<AttendanceRecord> findByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

       // Find all attendance records by employee
       List<AttendanceRecord> findByEmployee(Employee employee);

       // Find all attendance records by employee with pagination
       Page<AttendanceRecord> findByEmployee(Employee employee, Pageable pageable);

       // Find attendance records by employee and date range
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate ORDER BY ar.attendanceDate")
       List<AttendanceRecord> findByEmployeeAndDateRange(@Param("employee") Employee employee,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Find attendance records by organization and date range
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate")
       Page<AttendanceRecord> findByOrganizationAndDateRange(@Param("organization") Organization organization,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     Pageable pageable);

       // Find attendance records by status
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND ar.status = :status")
       Page<AttendanceRecord> findByOrganizationAndStatus(@Param("organization") Organization organization,
                     @Param("status") AttendanceStatus status,
                     Pageable pageable);

       // Find attendance records by employee and status
       List<AttendanceRecord> findByEmployeeAndStatus(Employee employee, AttendanceStatus status);

       // Find attendance records by date
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND ar.attendanceDate = :date")
       List<AttendanceRecord> findByOrganizationAndDate(@Param("organization") Organization organization,
                     @Param("date") LocalDate date);

       // Find present employees for a date
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "ar.attendanceDate = :date AND ar.status IN ('PRESENT', 'LATE', 'HALF_DAY', 'WORK_FROM_HOME')")
       List<AttendanceRecord> findPresentByOrganizationAndDate(@Param("organization") Organization organization,
                     @Param("date") LocalDate date);

       // Find absent employees for a date
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "ar.attendanceDate = :date AND ar.status = 'ABSENT'")
       List<AttendanceRecord> findAbsentByOrganizationAndDate(@Param("organization") Organization organization,
                     @Param("date") LocalDate date);

       // Find late employees for a date
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "ar.attendanceDate = :date AND ar.status = 'LATE'")
       List<AttendanceRecord> findLateByOrganizationAndDate(@Param("organization") Organization organization,
                     @Param("date") LocalDate date);

       // Find attendance records with overtime
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "ar.overtimeHours > 0 AND ar.attendanceDate BETWEEN :startDate AND :endDate")
       List<AttendanceRecord> findWithOvertimeByOrganizationAndDateRange(
                     @Param("organization") Organization organization,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Find attendance records by shift
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee IN " +
                     "(SELECT es.employee FROM EmployeeShift es WHERE es.shift = :shift)")
       List<AttendanceRecord> findByShift(@Param("shift") Shift shift);

       // Find attendance records by department and date range
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.department.id = :departmentId AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate")
       List<AttendanceRecord> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Count attendance records by employee and status in date range
       @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "ar.status = :status AND ar.attendanceDate BETWEEN :startDate AND :endDate")
       long countByEmployeeAndStatusAndDateRange(@Param("employee") Employee employee,
                     @Param("status") AttendanceStatus status,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Count present days by employee in date range
       @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "ar.status IN ('PRESENT', 'LATE', 'HALF_DAY', 'WORK_FROM_HOME') AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate")
       long countPresentDaysByEmployeeAndDateRange(@Param("employee") Employee employee,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Get total hours worked by employee in date range
       @Query("SELECT SUM(ar.totalHours) FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate")
       BigDecimal getTotalHoursByEmployeeAndDateRange(@Param("employee") Employee employee,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Get total overtime hours by employee in date range
       @Query("SELECT SUM(ar.overtimeHours) FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate")
       BigDecimal getTotalOvertimeHoursByEmployeeAndDateRange(@Param("employee") Employee employee,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Find employees with perfect attendance in date range
       @Query("SELECT DISTINCT ar.employee FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND "
                     +
                     "ar.attendanceDate BETWEEN :startDate AND :endDate AND " +
                     "ar.employee NOT IN (SELECT ar2.employee FROM AttendanceRecord ar2 WHERE ar2.status = 'ABSENT' AND "
                     +
                     "ar2.attendanceDate BETWEEN :startDate AND :endDate)")
       List<Employee> findEmployeesWithPerfectAttendance(@Param("organization") Organization organization,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);

       // Find attendance records with comprehensive search
       @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employee.organization = :organization AND " +
                     "(:employeeName IS NULL OR LOWER(ar.employee.firstName) LIKE LOWER(CONCAT('%', :employeeName, '%')) OR "
                     +
                     "LOWER(ar.employee.lastName) LIKE LOWER(CONCAT('%', :employeeName, '%'))) AND " +
                     "(:status IS NULL OR ar.status = :status) AND " +
                     "(:startDate IS NULL OR ar.attendanceDate >= :startDate) AND " +
                     "(:endDate IS NULL OR ar.attendanceDate <= :endDate)")
       Page<AttendanceRecord> findBySearchCriteria(@Param("organization") Organization organization,
                     @Param("employeeName") String employeeName,
                     @Param("status") AttendanceStatus status,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     Pageable pageable);

       // Check if attendance record exists for employee and date
       boolean existsByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

       // Get attendance summary by employee and month
       @Query("SELECT ar.status, COUNT(ar) FROM AttendanceRecord ar WHERE ar.employee = :employee AND " +
                     "YEAR(ar.attendanceDate) = :year AND MONTH(ar.attendanceDate) = :month GROUP BY ar.status")
       List<Object[]> getAttendanceSummaryByEmployeeAndMonth(@Param("employee") Employee employee,
                     @Param("year") int year,
                     @Param("month") int month);

       // Get daily attendance summary for organization
       @Query("SELECT ar.attendanceDate, ar.status, COUNT(ar) FROM AttendanceRecord ar " +
                     "WHERE ar.employee.organization = :organization AND ar.attendanceDate BETWEEN :startDate AND :endDate "
                     +
                     "GROUP BY ar.attendanceDate, ar.status ORDER BY ar.attendanceDate")
       List<Object[]> getDailyAttendanceSummaryByOrganization(@Param("organization") Organization organization,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate);
}

