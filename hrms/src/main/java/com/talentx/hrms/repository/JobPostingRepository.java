package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Location;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.recruitment.JobPosting;
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
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    
    // Find job postings by organization
    List<JobPosting> findByOrganization(Organization organization);
    
    // Find job postings by organization with pagination
    Page<JobPosting> findByOrganization(Organization organization, Pageable pageable);
    
    // Find job postings by status
    List<JobPosting> findByOrganizationAndStatus(Organization organization, String status);
    
    // Find job postings by status with pagination
    Page<JobPosting> findByOrganizationAndStatus(Organization organization, String status, Pageable pageable);
    
    // Find job postings by department
    List<JobPosting> findByDepartment(Department department);
    
    // Find job postings by department with pagination
    Page<JobPosting> findByDepartment(Department department, Pageable pageable);
    
    // Find job postings by location
    List<JobPosting> findByLocation(Location location);
    
    // Find job postings by employment type
    List<JobPosting> findByOrganizationAndEmploymentType(Organization organization, EmploymentType employmentType);
    
    // Find job postings by hiring manager
    List<JobPosting> findByHiringManager(Employee hiringManager);
    
    // Find job postings by recruiter
    List<JobPosting> findByRecruiter(Employee recruiter);
    
    // Search job postings by job title
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "LOWER(jp.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))")
    Page<JobPosting> findByOrganizationAndJobTitleContainingIgnoreCase(@Param("organization") Organization organization,
                                                                      @Param("jobTitle") String jobTitle,
                                                                      Pageable pageable);
    
    // Find job postings by job code
    Optional<JobPosting> findByOrganizationAndJobCode(Organization organization, String jobCode);
    
    // Find job postings posted between dates
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.postedDate BETWEEN :startDate AND :endDate")
    List<JobPosting> findByOrganizationAndPostedDateBetween(@Param("organization") Organization organization,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    // Find job postings with application deadline between dates
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.applicationDeadline BETWEEN :startDate AND :endDate")
    List<JobPosting> findByOrganizationAndApplicationDeadlineBetween(@Param("organization") Organization organization,
                                                                    @Param("startDate") LocalDate startDate,
                                                                    @Param("endDate") LocalDate endDate);
    
    // Find expired job postings
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.applicationDeadline < CURRENT_DATE AND jp.status = 'ACTIVE'")
    List<JobPosting> findExpiredByOrganization(@Param("organization") Organization organization);
    
    // Find job postings with open positions
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.positionsFilled < jp.positionsAvailable")
    List<JobPosting> findWithOpenPositionsByOrganization(@Param("organization") Organization organization);
    
    // Find job postings with no open positions
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.positionsFilled >= jp.positionsAvailable")
    List<JobPosting> findWithNoOpenPositionsByOrganization(@Param("organization") Organization organization);
    
    // Find urgent job postings
    List<JobPosting> findByOrganizationAndIsUrgentTrue(Organization organization);
    
    // Find internal-only job postings
    List<JobPosting> findByOrganizationAndIsInternalOnlyTrue(Organization organization);
    
    // Find remote work job postings
    List<JobPosting> findByOrganizationAndIsRemoteWorkTrue(Organization organization);
    
    // Find job postings by salary range
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.minSalary <= :maxSalary AND jp.maxSalary >= :minSalary")
    List<JobPosting> findByOrganizationAndSalaryRange(@Param("organization") Organization organization,
                                                     @Param("minSalary") BigDecimal minSalary,
                                                     @Param("maxSalary") BigDecimal maxSalary);
    
    // Find job postings by experience range
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.minExperienceYears <= :maxExperience AND " +
           "(jp.maxExperienceYears IS NULL OR jp.maxExperienceYears >= :minExperience)")
    List<JobPosting> findByOrganizationAndExperienceRange(@Param("organization") Organization organization,
                                                         @Param("minExperience") Integer minExperience,
                                                         @Param("maxExperience") Integer maxExperience);
    
    // Find job postings by education level
    List<JobPosting> findByOrganizationAndEducationLevel(Organization organization, String educationLevel);
    
    // Find job postings with applications
    @Query("SELECT DISTINCT jp FROM JobPosting jp LEFT JOIN FETCH jp.applications WHERE jp.id = :id")
    Optional<JobPosting> findByIdWithApplications(@Param("id") Long id);
    
    // Count job postings by organization and status
    long countByOrganizationAndStatus(Organization organization, String status);
    
    // Count applications by job posting
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting = :jobPosting")
    long countApplicationsByJobPosting(@Param("jobPosting") JobPosting jobPosting);
    
    // Find job postings with application count
    @Query("SELECT jp, COUNT(a) FROM JobPosting jp LEFT JOIN jp.applications a " +
           "WHERE jp.organization = :organization GROUP BY jp")
    List<Object[]> findJobPostingsWithApplicationCount(@Param("organization") Organization organization);
    
    // Check if job code exists in organization
    boolean existsByOrganizationAndJobCode(Organization organization, String jobCode);
    
    // Find job postings with comprehensive search
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "(:jobTitle IS NULL OR LOWER(jp.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
           "(:status IS NULL OR jp.status = :status) AND " +
           "(:department IS NULL OR jp.department = :department) AND " +
           "(:location IS NULL OR jp.location = :location) AND " +
           "(:employmentType IS NULL OR jp.employmentType = :employmentType) AND " +
           "(:isRemoteWork IS NULL OR jp.isRemoteWork = :isRemoteWork) AND " +
           "(:isUrgent IS NULL OR jp.isUrgent = :isUrgent)")
    Page<JobPosting> findBySearchCriteria(@Param("organization") Organization organization,
                                         @Param("jobTitle") String jobTitle,
                                         @Param("status") String status,
                                         @Param("department") Department department,
                                         @Param("location") Location location,
                                         @Param("employmentType") EmploymentType employmentType,
                                         @Param("isRemoteWork") Boolean isRemoteWork,
                                         @Param("isUrgent") Boolean isUrgent,
                                         Pageable pageable);
    
    // Find recently posted job postings
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.postedDate >= :sinceDate ORDER BY jp.postedDate DESC")
    List<JobPosting> findRecentlyPostedByOrganization(@Param("organization") Organization organization,
                                                     @Param("sinceDate") LocalDate sinceDate);
    
    // Find job postings closing soon
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "jp.applicationDeadline BETWEEN CURRENT_DATE AND :beforeDate AND jp.status = 'ACTIVE'")
    List<JobPosting> findClosingSoonByOrganization(@Param("organization") Organization organization,
                                                  @Param("beforeDate") LocalDate beforeDate);
    
    // Get job posting statistics by organization
    @Query("SELECT jp.status, COUNT(jp), SUM(jp.positionsAvailable), SUM(jp.positionsFilled) " +
           "FROM JobPosting jp WHERE jp.organization = :organization GROUP BY jp.status")
    List<Object[]> getJobPostingStatsByOrganization(@Param("organization") Organization organization);
    
    // Find job postings by year and month
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "YEAR(jp.postedDate) = :year AND MONTH(jp.postedDate) = :month")
    List<JobPosting> findByOrganizationAndYearAndMonth(@Param("organization") Organization organization,
                                                      @Param("year") int year,
                                                      @Param("month") int month);
    
    // Find job postings by skills (text search in skills field)
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "LOWER(jp.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<JobPosting> findByOrganizationAndSkillsContaining(@Param("organization") Organization organization,
                                                          @Param("skill") String skill);
    
    // Find job postings by qualifications (text search in qualifications field)
    @Query("SELECT jp FROM JobPosting jp WHERE jp.organization = :organization AND " +
           "LOWER(jp.qualifications) LIKE LOWER(CONCAT('%', :qualification, '%'))")
    List<JobPosting> findByOrganizationAndQualificationsContaining(@Param("organization") Organization organization,
                                                                  @Param("qualification") String qualification);
}

