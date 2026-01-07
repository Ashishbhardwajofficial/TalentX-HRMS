package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.recruitment.Application;
import com.talentx.hrms.entity.recruitment.Candidate;
import com.talentx.hrms.entity.recruitment.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    // Find interviews by application
    List<Interview> findByApplication(Application application);
    
    // Find interviews by candidate
    List<Interview> findByCandidate(Candidate candidate);
    
    // Find interviews by candidate with pagination
    Page<Interview> findByCandidate(Candidate candidate, Pageable pageable);
    
    // Find interviews by interviewer
    List<Interview> findByInterviewer(Employee interviewer);
    
    // Find interviews by interviewer with pagination
    Page<Interview> findByInterviewer(Employee interviewer, Pageable pageable);
    
    // Find interviews by organization
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization")
    Page<Interview> findByOrganization(@Param("organization") Organization organization, Pageable pageable);
    
    // Find interviews by status
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.status = :status")
    List<Interview> findByOrganizationAndStatus(@Param("organization") Organization organization, 
                                               @Param("status") String status);
    
    // Find interviews by interview type
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.interviewType = :interviewType")
    List<Interview> findByOrganizationAndInterviewType(@Param("organization") Organization organization, 
                                                      @Param("interviewType") String interviewType);
    
    // Find interviews by round
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.round = :round")
    List<Interview> findByOrganizationAndRound(@Param("organization") Organization organization, 
                                              @Param("round") String round);
    
    // Find scheduled interviews
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.status = 'SCHEDULED'")
    List<Interview> findScheduledByOrganization(@Param("organization") Organization organization);
    
    // Find completed interviews
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.status = 'COMPLETED'")
    List<Interview> findCompletedByOrganization(@Param("organization") Organization organization);
    
    // Find cancelled interviews
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.status = 'CANCELLED'")
    List<Interview> findCancelledByOrganization(@Param("organization") Organization organization);
    
    // Find interviews scheduled between dates
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.scheduledDateTime BETWEEN :startDate AND :endDate")
    List<Interview> findByOrganizationAndScheduledDateTimeBetween(@Param("organization") Organization organization,
                                                                 @Param("startDate") Instant startDate,
                                                                 @Param("endDate") Instant endDate);
    
    // Find interviews scheduled for today
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "DATE(i.scheduledDateTime) = CURRENT_DATE AND i.status = 'SCHEDULED'")
    List<Interview> findTodaysInterviewsByOrganization(@Param("organization") Organization organization);
    
    // Find interviews scheduled for a specific date
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "DATE(i.scheduledDateTime) = :date")
    List<Interview> findByOrganizationAndScheduledDate(@Param("organization") Organization organization,
                                                      @Param("date") LocalDate date);
    
    // Find interviews by interviewer and date range
    @Query("SELECT i FROM Interview i WHERE i.interviewer = :interviewer AND " +
           "i.scheduledDateTime BETWEEN :startDate AND :endDate")
    List<Interview> findByInterviewerAndScheduledDateTimeBetween(@Param("interviewer") Employee interviewer,
                                                                @Param("startDate") Instant startDate,
                                                                @Param("endDate") Instant endDate);
    
    // Find interviews with ratings
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.overallRating IS NOT NULL")
    List<Interview> findWithRatingsByOrganization(@Param("organization") Organization organization);
    
    // Find interviews by overall rating range
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.overallRating BETWEEN :minRating AND :maxRating")
    List<Interview> findByOrganizationAndOverallRatingBetween(@Param("organization") Organization organization,
                                                             @Param("minRating") Integer minRating,
                                                             @Param("maxRating") Integer maxRating);
    
    // Find interviews by recommendation
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.recommendation = :recommendation")
    List<Interview> findByOrganizationAndRecommendation(@Param("organization") Organization organization,
                                                       @Param("recommendation") String recommendation);
    
    // Find interviews where candidate showed up
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.candidateShowedUp = true")
    List<Interview> findWhereCandidateShowedUpByOrganization(@Param("organization") Organization organization);
    
    // Find interviews where candidate didn't show up (no-shows)
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.candidateShowedUp = false")
    List<Interview> findNoShowsByOrganization(@Param("organization") Organization organization);
    
    // Find rescheduled interviews
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.rescheduledFrom IS NOT NULL")
    List<Interview> findRescheduledByOrganization(@Param("organization") Organization organization);
    
    // Find upcoming interviews (next 7 days)
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.scheduledDateTime BETWEEN CURRENT_TIMESTAMP AND :endDate AND i.status = 'SCHEDULED'")
    List<Interview> findUpcomingByOrganization(@Param("organization") Organization organization,
                                              @Param("endDate") Instant endDate);
    
    // Find overdue interviews (scheduled in the past but not completed)
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.scheduledDateTime < CURRENT_TIMESTAMP AND i.status = 'SCHEDULED'")
    List<Interview> findOverdueByOrganization(@Param("organization") Organization organization);
    
    // Count interviews by organization and status
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.status = :status")
    long countByOrganizationAndStatus(@Param("organization") Organization organization, @Param("status") String status);
    
    // Count interviews by interviewer
    long countByInterviewer(Employee interviewer);
    
    // Count interviews by candidate
    long countByCandidate(Candidate candidate);
    
    // Get interview statistics by organization
    @Query("SELECT i.status, COUNT(i) FROM Interview i WHERE i.application.jobPosting.organization = :organization GROUP BY i.status")
    List<Object[]> getInterviewStatsByOrganization(@Param("organization") Organization organization);
    
    // Get interview statistics by interviewer
    @Query("SELECT i.interviewer.id, i.interviewer.firstName, i.interviewer.lastName, COUNT(i), AVG(i.overallRating) " +
           "FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.interviewer IS NOT NULL " +
           "GROUP BY i.interviewer.id, i.interviewer.firstName, i.interviewer.lastName")
    List<Object[]> getInterviewStatsByInterviewer(@Param("organization") Organization organization);
    
    // Get average ratings by organization
    @Query("SELECT AVG(i.overallRating), AVG(i.technicalRating), AVG(i.communicationRating), AVG(i.culturalFitRating) " +
           "FROM Interview i WHERE i.application.jobPosting.organization = :organization AND i.overallRating IS NOT NULL")
    List<Object[]> getAverageRatingsByOrganization(@Param("organization") Organization organization);
    
    // Find interviews by comprehensive search criteria
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "(:candidateName IS NULL OR LOWER(i.candidate.firstName) LIKE LOWER(CONCAT('%', :candidateName, '%')) OR " +
           "LOWER(i.candidate.lastName) LIKE LOWER(CONCAT('%', :candidateName, '%'))) AND " +
           "(:interviewerName IS NULL OR LOWER(i.interviewer.firstName) LIKE LOWER(CONCAT('%', :interviewerName, '%')) OR " +
           "LOWER(i.interviewer.lastName) LIKE LOWER(CONCAT('%', :interviewerName, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:interviewType IS NULL OR i.interviewType = :interviewType) AND " +
           "(:round IS NULL OR i.round = :round) AND " +
           "(:startDate IS NULL OR i.scheduledDateTime >= :startDate) AND " +
           "(:endDate IS NULL OR i.scheduledDateTime <= :endDate)")
    Page<Interview> findBySearchCriteria(@Param("organization") Organization organization,
                                        @Param("candidateName") String candidateName,
                                        @Param("interviewerName") String interviewerName,
                                        @Param("status") String status,
                                        @Param("interviewType") String interviewType,
                                        @Param("round") String round,
                                        @Param("startDate") Instant startDate,
                                        @Param("endDate") Instant endDate,
                                        Pageable pageable);
    
    // Find interviews by year and month
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "YEAR(i.scheduledDateTime) = :year AND MONTH(i.scheduledDateTime) = :month")
    List<Interview> findByOrganizationAndYearAndMonth(@Param("organization") Organization organization,
                                                     @Param("year") int year,
                                                     @Param("month") int month);
    
    // Find top-rated interviews
    @Query("SELECT i FROM Interview i WHERE i.application.jobPosting.organization = :organization AND " +
           "i.overallRating IS NOT NULL ORDER BY i.overallRating DESC")
    List<Interview> findTopRatedByOrganization(@Param("organization") Organization organization, Pageable pageable);
}

