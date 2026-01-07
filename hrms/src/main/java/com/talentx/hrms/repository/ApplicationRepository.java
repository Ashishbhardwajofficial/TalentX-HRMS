package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.recruitment.Application;
import com.talentx.hrms.entity.recruitment.Candidate;
import com.talentx.hrms.entity.recruitment.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // Find applications by candidate
    List<Application> findByCandidate(Candidate candidate);
    
    // Find applications by candidate with pagination
    Page<Application> findByCandidate(Candidate candidate, Pageable pageable);
    
    // Find applications by job posting
    List<Application> findByJobPosting(JobPosting jobPosting);
    
    // Find applications by job posting with pagination
    Page<Application> findByJobPosting(JobPosting jobPosting, Pageable pageable);
    
    // Find application by candidate and job posting
    Optional<Application> findByCandidateAndJobPosting(Candidate candidate, JobPosting jobPosting);
    
    // Find applications by organization
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization")
    Page<Application> findByOrganization(@Param("organization") Organization organization, Pageable pageable);
    
    // Find applications by status
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.status = :status")
    Page<Application> findByOrganizationAndStatus(@Param("organization") Organization organization,
                                                 @Param("status") String status,
                                                 Pageable pageable);
    
    // Find applications by candidate and status
    List<Application> findByCandidateAndStatus(Candidate candidate, String status);
    
    // Find applications by job posting and status
    List<Application> findByJobPostingAndStatus(JobPosting jobPosting, String status);
    
    // Find applications applied between dates
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.applicationDate BETWEEN :startDate AND :endDate")
    List<Application> findByOrganizationAndApplicationDateBetween(@Param("organization") Organization organization,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
    
    // Find applications screened between dates
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.screenedAt BETWEEN :startDate AND :endDate")
    List<Application> findByOrganizationAndScreenedAtBetween(@Param("organization") Organization organization,
                                                            @Param("startDate") Instant startDate,
                                                            @Param("endDate") Instant endDate);
    
    // Find applications by screened by
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.screenedBy = :screenedBy")
    List<Application> findByOrganizationAndScreenedBy(@Param("organization") Organization organization,
                                                     @Param("screenedBy") String screenedBy);
    
    // Find applications with screening score range
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.screeningScore BETWEEN :minScore AND :maxScore")
    List<Application> findByOrganizationAndScreeningScoreBetween(@Param("organization") Organization organization,
                                                                @Param("minScore") Integer minScore,
                                                                @Param("maxScore") Integer maxScore);
    
    // Find applications with interviews scheduled
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.interviewScheduledAt IS NOT NULL")
    List<Application> findWithInterviewsScheduledByOrganization(@Param("organization") Organization organization);
    
    // Find applications with offers extended
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.offerExtendedAt IS NOT NULL")
    List<Application> findWithOffersExtendedByOrganization(@Param("organization") Organization organization);
    
    // Find applications with offers accepted
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.offerAcceptedAt IS NOT NULL")
    List<Application> findWithOffersAcceptedByOrganization(@Param("organization") Organization organization);
    
    // Find applications with offers rejected
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.offerRejectedAt IS NOT NULL")
    List<Application> findWithOffersRejectedByOrganization(@Param("organization") Organization organization);
    
    // Find hired applications
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.status = 'HIRED'")
    List<Application> findHiredByOrganization(@Param("organization") Organization organization);
    
    // Find rejected applications
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.status = 'REJECTED'")
    List<Application> findRejectedByOrganization(@Param("organization") Organization organization);
    
    // Find withdrawn applications
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.status = 'WITHDRAWN'")
    List<Application> findWithdrawnByOrganization(@Param("organization") Organization organization);
    
    // Find active applications
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.status NOT IN ('HIRED', 'REJECTED', 'WITHDRAWN')")
    List<Application> findActiveByOrganization(@Param("organization") Organization organization);
    
    // Find applications by expected salary range
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.expectedSalary BETWEEN :minSalary AND :maxSalary")
    List<Application> findByOrganizationAndExpectedSalaryBetween(@Param("organization") Organization organization,
                                                                @Param("minSalary") BigDecimal minSalary,
                                                                @Param("maxSalary") BigDecimal maxSalary);
    
    // Find applications available immediately
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.isAvailableImmediately = true")
    List<Application> findAvailableImmediatelyByOrganization(@Param("organization") Organization organization);
    
    // Find applications willing to relocate
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.isWillingToRelocate = true")
    List<Application> findWillingToRelocateByOrganization(@Param("organization") Organization organization);
    
    // Find applications open to remote work
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.isOpenToRemote = true")
    List<Application> findOpenToRemoteByOrganization(@Param("organization") Organization organization);
    
    // Find applications by notice period
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.noticePeriodDays <= :maxNoticePeriod")
    List<Application> findByOrganizationAndNoticePeriodLessThanEqual(@Param("organization") Organization organization,
                                                                    @Param("maxNoticePeriod") Integer maxNoticePeriod);
    
    // Find applications with resume
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.resumePath IS NOT NULL")
    List<Application> findWithResumeByOrganization(@Param("organization") Organization organization);
    
    // Find applications with portfolio
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND a.portfolioPath IS NOT NULL")
    List<Application> findWithPortfolioByOrganization(@Param("organization") Organization organization);
    
    // Find applications by department
    @Query("SELECT a FROM Application a WHERE a.jobPosting.department.id = :departmentId")
    List<Application> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    // Count applications by organization
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.organization = :organization")
    long countByOrganization(@Param("organization") Organization organization);
    
    // Count applications by organization and status
    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.organization = :organization AND a.status = :status")
    long countByOrganizationAndStatus(@Param("organization") Organization organization, @Param("status") String status);
    
    // Count applications by job posting and status
    long countByJobPostingAndStatus(JobPosting jobPosting, String status);
    
    // Count applications by candidate
    long countByCandidate(Candidate candidate);
    
    // Check if application exists for candidate and job posting
    boolean existsByCandidateAndJobPosting(Candidate candidate, JobPosting jobPosting);
    
    // Find applications with comprehensive search
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "(:candidateName IS NULL OR LOWER(a.candidate.firstName) LIKE LOWER(CONCAT('%', :candidateName, '%')) OR " +
           "LOWER(a.candidate.lastName) LIKE LOWER(CONCAT('%', :candidateName, '%'))) AND " +
           "(:jobTitle IS NULL OR LOWER(a.jobPosting.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startDate IS NULL OR a.applicationDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.applicationDate <= :endDate)")
    Page<Application> findBySearchCriteria(@Param("organization") Organization organization,
                                          @Param("candidateName") String candidateName,
                                          @Param("jobTitle") String jobTitle,
                                          @Param("status") String status,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);
    
    // Get application statistics by organization
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.jobPosting.organization = :organization GROUP BY a.status")
    List<Object[]> getApplicationStatsByOrganization(@Param("organization") Organization organization);
    
    // Find applications by year and month
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "YEAR(a.applicationDate) = :year AND MONTH(a.applicationDate) = :month")
    List<Application> findByOrganizationAndYearAndMonth(@Param("organization") Organization organization,
                                                       @Param("year") int year,
                                                       @Param("month") int month);
    
    // Find applications with interviews
    @Query("SELECT DISTINCT a FROM Application a LEFT JOIN FETCH a.interviews WHERE a.id = :id")
    Optional<Application> findByIdWithInterviews(@Param("id") Long id);
    
    // Find top applications by screening score
    @Query("SELECT a FROM Application a WHERE a.jobPosting.organization = :organization AND " +
           "a.screeningScore IS NOT NULL ORDER BY a.screeningScore DESC")
    List<Application> findTopByScreeningScoreByOrganization(@Param("organization") Organization organization, Pageable pageable);
}

