package com.talentx.hrms.repository;

import com.talentx.hrms.entity.recruitment.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    // Find candidate by email
    Optional<Candidate> findByEmail(String email);
    
    // Check if candidate exists by email
    boolean existsByEmail(String email);
    
    // Find candidates by name (first or last name)
    @Query("SELECT c FROM Candidate c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Candidate> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Find candidates by skills
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Candidate> findBySkillsContaining(@Param("skill") String skill);
    
    // Find candidates by current job title
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.currentJobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))")
    List<Candidate> findByCurrentJobTitleContaining(@Param("jobTitle") String jobTitle);
    
    // Find candidates by experience range
    @Query("SELECT c FROM Candidate c WHERE c.totalExperienceYears BETWEEN :minExperience AND :maxExperience")
    List<Candidate> findByExperienceRange(@Param("minExperience") Integer minExperience, 
                                         @Param("maxExperience") Integer maxExperience);
    
    // Find candidates by expected salary range
    @Query("SELECT c FROM Candidate c WHERE c.expectedSalary BETWEEN :minSalary AND :maxSalary")
    List<Candidate> findByExpectedSalaryRange(@Param("minSalary") BigDecimal minSalary, 
                                             @Param("maxSalary") BigDecimal maxSalary);
    
    // Find candidates available immediately
    List<Candidate> findByIsAvailableImmediatelyTrue();
    
    // Find candidates willing to relocate
    List<Candidate> findByIsWillingToRelocateTrue();
    
    // Find candidates open to remote work
    List<Candidate> findByIsOpenToRemoteTrue();
    
    // Find candidates by notice period
    @Query("SELECT c FROM Candidate c WHERE c.noticePeriodDays <= :maxNoticePeriod")
    List<Candidate> findByNoticePeriodLessThanEqual(@Param("maxNoticePeriod") Integer maxNoticePeriod);
    
    // Find candidates by education level
    List<Candidate> findByHighestEducation(String educationLevel);
    
    // Find candidates by university
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.university) LIKE LOWER(CONCAT('%', :university, '%'))")
    List<Candidate> findByUniversityContaining(@Param("university") String university);
    
    // Find candidates by field of study
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.fieldOfStudy) LIKE LOWER(CONCAT('%', :fieldOfStudy, '%'))")
    List<Candidate> findByFieldOfStudyContaining(@Param("fieldOfStudy") String fieldOfStudy);
    
    // Find candidates by source
    List<Candidate> findBySource(String source);
    
    // Find candidates by city
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Candidate> findByCityContaining(@Param("city") String city);
    
    // Find candidates by country
    List<Candidate> findByCountry(String country);
    
    // Find non-blacklisted candidates
    List<Candidate> findByIsBlacklistedFalse();
    
    // Find blacklisted candidates
    List<Candidate> findByIsBlacklistedTrue();
    
    // Find candidates with applications
    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.applications WHERE c.id = :id")
    Optional<Candidate> findByIdWithApplications(@Param("id") Long id);
    
    // Find candidates with interviews
    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.interviews WHERE c.id = :id")
    Optional<Candidate> findByIdWithInterviews(@Param("id") Long id);
    
    // Search candidates with comprehensive criteria
    @Query("SELECT c FROM Candidate c WHERE " +
           "(:name IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:jobTitle IS NULL OR LOWER(c.currentJobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
           "(:skills IS NULL OR LOWER(c.skills) LIKE LOWER(CONCAT('%', :skills, '%'))) AND " +
           "(:minExperience IS NULL OR c.totalExperienceYears >= :minExperience) AND " +
           "(:maxExperience IS NULL OR c.totalExperienceYears <= :maxExperience) AND " +
           "(:education IS NULL OR c.highestEducation = :education) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:isAvailableImmediately IS NULL OR c.isAvailableImmediately = :isAvailableImmediately) AND " +
           "(:isWillingToRelocate IS NULL OR c.isWillingToRelocate = :isWillingToRelocate) AND " +
           "(:isOpenToRemote IS NULL OR c.isOpenToRemote = :isOpenToRemote) AND " +
           "c.isBlacklisted = false")
    Page<Candidate> findBySearchCriteria(@Param("name") String name,
                                        @Param("email") String email,
                                        @Param("jobTitle") String jobTitle,
                                        @Param("skills") String skills,
                                        @Param("minExperience") Integer minExperience,
                                        @Param("maxExperience") Integer maxExperience,
                                        @Param("education") String education,
                                        @Param("city") String city,
                                        @Param("isAvailableImmediately") Boolean isAvailableImmediately,
                                        @Param("isWillingToRelocate") Boolean isWillingToRelocate,
                                        @Param("isOpenToRemote") Boolean isOpenToRemote,
                                        Pageable pageable);
    
    // Count candidates by source
    @Query("SELECT c.source, COUNT(c) FROM Candidate c GROUP BY c.source")
    List<Object[]> countCandidatesBySource();
    
    // Count candidates by education level
    @Query("SELECT c.highestEducation, COUNT(c) FROM Candidate c GROUP BY c.highestEducation")
    List<Object[]> countCandidatesByEducation();
    
    // Count candidates by experience range
    @Query("SELECT " +
           "CASE " +
           "WHEN c.totalExperienceYears < 2 THEN 'Entry Level' " +
           "WHEN c.totalExperienceYears < 5 THEN 'Junior' " +
           "WHEN c.totalExperienceYears < 10 THEN 'Mid Level' " +
           "ELSE 'Senior' " +
           "END as experienceLevel, COUNT(c) " +
           "FROM Candidate c " +
           "WHERE c.totalExperienceYears IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "WHEN c.totalExperienceYears < 2 THEN 'Entry Level' " +
           "WHEN c.totalExperienceYears < 5 THEN 'Junior' " +
           "WHEN c.totalExperienceYears < 10 THEN 'Mid Level' " +
           "ELSE 'Senior' " +
           "END")
    List<Object[]> countCandidatesByExperienceLevel();
}

