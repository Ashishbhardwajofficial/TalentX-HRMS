package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.payroll.PayrollRun;
import com.talentx.hrms.entity.payroll.Payslip;
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
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    
    // Find payslip by payroll run and employee
    Optional<Payslip> findByPayrollRunAndEmployee(PayrollRun payrollRun, Employee employee);
    
    // Find payslips by employee
    List<Payslip> findByEmployee(Employee employee);
    
    // Find payslips by employee with pagination
    Page<Payslip> findByEmployee(Employee employee, Pageable pageable);
    
    // Find payslips by payroll run
    List<Payslip> findByPayrollRun(PayrollRun payrollRun);
    
    // Find payslips by payroll run with pagination
    Page<Payslip> findByPayrollRun(PayrollRun payrollRun, Pageable pageable);
    
    // Find payslips by organization
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization")
    Page<Payslip> findByOrganization(@Param("organization") Organization organization, Pageable pageable);
    
    // Find payslips by employee and date range
    @Query("SELECT p FROM Payslip p WHERE p.employee = :employee AND " +
           "p.payrollRun.payDate BETWEEN :startDate AND :endDate")
    List<Payslip> findByEmployeeAndPayDateBetween(@Param("employee") Employee employee,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    // Find payslips by organization and date range
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "p.payrollRun.payDate BETWEEN :startDate AND :endDate")
    List<Payslip> findByOrganizationAndPayDateBetween(@Param("organization") Organization organization,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    // Find finalized payslips
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.isFinalized = true")
    List<Payslip> findFinalizedByOrganization(@Param("organization") Organization organization);
    
    // Find non-finalized payslips
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.isFinalized = false")
    List<Payslip> findNonFinalizedByOrganization(@Param("organization") Organization organization);
    
    // Find payslips with PDF generated
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.pdfPath IS NOT NULL")
    List<Payslip> findWithPdfByOrganization(@Param("organization") Organization organization);
    
    // Find payslips without PDF
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.pdfPath IS NULL")
    List<Payslip> findWithoutPdfByOrganization(@Param("organization") Organization organization);
    
    // Find payslips by department
    @Query("SELECT p FROM Payslip p WHERE p.employee.department.id = :departmentId")
    List<Payslip> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    // Find payslips by department and date range
    @Query("SELECT p FROM Payslip p WHERE p.employee.department.id = :departmentId AND " +
           "p.payrollRun.payDate BETWEEN :startDate AND :endDate")
    List<Payslip> findByDepartmentIdAndPayDateBetween(@Param("departmentId") Long departmentId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    // Find payslips with net pay greater than amount
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.netPay > :amount")
    List<Payslip> findByOrganizationAndNetPayGreaterThan(@Param("organization") Organization organization,
                                                        @Param("amount") BigDecimal amount);
    
    // Find payslips with gross pay between amounts
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "p.grossPay BETWEEN :minAmount AND :maxAmount")
    List<Payslip> findByOrganizationAndGrossPayBetween(@Param("organization") Organization organization,
                                                      @Param("minAmount") BigDecimal minAmount,
                                                      @Param("maxAmount") BigDecimal maxAmount);
    
    // Find payslips with overtime pay
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.overtimePay > 0")
    List<Payslip> findWithOvertimePayByOrganization(@Param("organization") Organization organization);
    
    // Find payslips with bonus
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.bonus > 0")
    List<Payslip> findWithBonusByOrganization(@Param("organization") Organization organization);
    
    // Find payslips with commission
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND p.commission > 0")
    List<Payslip> findWithCommissionByOrganization(@Param("organization") Organization organization);
    
    // Get total net pay by organization and year
    @Query("SELECT SUM(p.netPay) FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "YEAR(p.payrollRun.payDate) = :year")
    BigDecimal getTotalNetPayByOrganizationAndYear(@Param("organization") Organization organization,
                                                  @Param("year") int year);
    
    // Get total gross pay by organization and year
    @Query("SELECT SUM(p.grossPay) FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "YEAR(p.payrollRun.payDate) = :year")
    BigDecimal getTotalGrossPayByOrganizationAndYear(@Param("organization") Organization organization,
                                                    @Param("year") int year);
    
    // Get total taxes by organization and year
    @Query("SELECT SUM(p.totalTaxes) FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "YEAR(p.payrollRun.payDate) = :year")
    BigDecimal getTotalTaxesByOrganizationAndYear(@Param("organization") Organization organization,
                                                 @Param("year") int year);
    
    // Get total deductions by organization and year
    @Query("SELECT SUM(p.totalDeductions) FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "YEAR(p.payrollRun.payDate) = :year")
    BigDecimal getTotalDeductionsByOrganizationAndYear(@Param("organization") Organization organization,
                                                      @Param("year") int year);
    
    // Count payslips by organization
    @Query("SELECT COUNT(p) FROM Payslip p WHERE p.employee.organization = :organization")
    long countByOrganization(@Param("organization") Organization organization);
    
    // Count finalized payslips by organization
    @Query("SELECT COUNT(p) FROM Payslip p WHERE p.employee.organization = :organization AND p.isFinalized = true")
    long countFinalizedByOrganization(@Param("organization") Organization organization);
    
    // Find payslips by employee and year
    @Query("SELECT p FROM Payslip p WHERE p.employee = :employee AND " +
           "YEAR(p.payrollRun.payDate) = :year ORDER BY p.payrollRun.payDate")
    List<Payslip> findByEmployeeAndYear(@Param("employee") Employee employee, @Param("year") int year);
    
    // Find latest payslip by employee
    @Query("SELECT p FROM Payslip p WHERE p.employee = :employee ORDER BY p.payrollRun.payDate DESC")
    Optional<Payslip> findLatestByEmployee(@Param("employee") Employee employee, Pageable pageable);
    
    // Check if payslip exists for payroll run and employee
    boolean existsByPayrollRunAndEmployee(PayrollRun payrollRun, Employee employee);
    
    // Find payslips with payroll items
    @Query("SELECT DISTINCT p FROM Payslip p LEFT JOIN FETCH p.payrollItems WHERE p.id = :id")
    Optional<Payslip> findByIdWithPayrollItems(@Param("id") Long id);
    
    // Find payslips with comprehensive search
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "(:employeeName IS NULL OR LOWER(p.employee.firstName) LIKE LOWER(CONCAT('%', :employeeName, '%')) OR " +
           "LOWER(p.employee.lastName) LIKE LOWER(CONCAT('%', :employeeName, '%'))) AND " +
           "(:payrollRunName IS NULL OR LOWER(p.payrollRun.name) LIKE LOWER(CONCAT('%', :payrollRunName, '%'))) AND " +
           "(:isFinalized IS NULL OR p.isFinalized = :isFinalized) AND " +
           "(:startDate IS NULL OR p.payrollRun.payDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.payrollRun.payDate <= :endDate)")
    Page<Payslip> findBySearchCriteria(@Param("organization") Organization organization,
                                      @Param("employeeName") String employeeName,
                                      @Param("payrollRunName") String payrollRunName,
                                      @Param("isFinalized") Boolean isFinalized,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);
    
    // Get payslip statistics by organization
    @Query("SELECT COUNT(p), SUM(p.grossPay), SUM(p.netPay), SUM(p.totalTaxes), SUM(p.totalDeductions) " +
           "FROM Payslip p WHERE p.employee.organization = :organization")
    List<Object[]> getPayslipStatsByOrganization(@Param("organization") Organization organization);
    
    // Find payslips by year and month
    @Query("SELECT p FROM Payslip p WHERE p.employee.organization = :organization AND " +
           "YEAR(p.payrollRun.payDate) = :year AND MONTH(p.payrollRun.payDate) = :month")
    List<Payslip> findByOrganizationAndYearAndMonth(@Param("organization") Organization organization,
                                                   @Param("year") int year,
                                                   @Param("month") int month);
    
    // Get average salary by organization
    @Query("SELECT AVG(p.grossPay) FROM Payslip p WHERE p.employee.organization = :organization")
    BigDecimal getAverageGrossPayByOrganization(@Param("organization") Organization organization);
    
    // Get median salary by organization (approximate using percentile)
    @Query(value = "SELECT PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY gross_pay) " +
                   "FROM payslips p JOIN employees e ON p.employee_id = e.id " +
                   "WHERE e.organization_id = :organizationId", nativeQuery = true)
    BigDecimal getMedianGrossPayByOrganization(@Param("organizationId") Long organizationId);
}

