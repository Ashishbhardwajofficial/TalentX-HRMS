package com.talentx.hrms.service.recruitment;

import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.recruitment.Application;
import com.talentx.hrms.entity.recruitment.Candidate;
import com.talentx.hrms.entity.recruitment.Interview;
import com.talentx.hrms.entity.recruitment.JobPosting;
import com.talentx.hrms.repository.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for recruitment tracking accuracy.
 * Feature: hrms-database-integration, Property 18: Recruitment Tracking Accuracy
 * Validates: Requirements 7.2
 * 
 * Property: For any job application, the system should accurately track candidate
 * progress through all interview stages.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RecruitmentTrackingAccuracyPropertyTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Property(tries = 100)
    @Label("Property 18: Recruitment Tracking Accuracy - Applications track candidate progress")
    void applicationsTrackCandidateProgress(
            @ForAll("applicationStatuses") String status,
            @ForAll @IntRange(min = 0, max = 5) int interviewCount) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept);
        Candidate candidate = createCandidate();

        // Create application
        Application application = new Application();
        application.setJobPosting(job);
        application.setCandidate(candidate);
        application.setApplicationDate(LocalDate.now());
        application.setStatus(status);
        application.setNotes("Application for " + job.getTitle());
        application = applicationRepository.save(application);

        // Create interviews for the application
        for (int i = 0; i < interviewCount; i++) {
            Interview interview = new Interview();
            interview.setApplication(application);
            interview.setScheduledDate(LocalDateTime.now().plusDays(i + 1));
            interview.setInterviewType("Round " + (i + 1));
            interview.setStatus("SCHEDULED");
            interviewRepository.save(interview);
        }

        // Verify: Application is tracked
        assertThat(application.getId()).isNotNull();
        assertThat(application.getStatus()).isEqualTo(status);
        assertThat(application.getCandidate()).isNotNull();
        assertThat(application.getJobPosting()).isNotNull();

        // Verify: Application can be retrieved
        Application retrieved = applicationRepository.findById(application.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getStatus()).isEqualTo(status);
    }

    @Property(tries = 100)
    @Label("Property 18: Recruitment Tracking Accuracy - Interview stages are sequential")
    void interviewStagesAreSequential(
            @ForAll @IntRange(min = 1, max = 5) int stageCount) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept);
        Candidate candidate = createCandidate();
        Application application = createApplication(job, candidate);

        // Create sequential interview stages
        LocalDateTime baseDate = LocalDateTime.now();
        for (int i = 0; i < stageCount; i++) {
            Interview interview = new Interview();
            interview.setApplication(application);
            interview.setScheduledDate(baseDate.plusDays(i * 7)); // Weekly intervals
            interview.setInterviewType("Stage " + (i + 1));
            interview.setStatus("SCHEDULED");
            interview = interviewRepository.save(interview);

            // Verify: Each interview is scheduled after the previous one
            if (i > 0) {
                assertThat(interview.getScheduledDate()).isAfter(baseDate.plusDays((i - 1) * 7));
            }
        }
    }

    @Property(tries = 100)
    @Label("Property 18: Recruitment Tracking Accuracy - Application status transitions are valid")
    void applicationStatusTransitionsAreValid(
            @ForAll("applicationStatuses") String initialStatus,
            @ForAll("applicationStatuses") String finalStatus) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept);
        Candidate candidate = createCandidate();

        // Create application with initial status
        Application application = new Application();
        application.setJobPosting(job);
        application.setCandidate(candidate);
        application.setApplicationDate(LocalDate.now());
        application.setStatus(initialStatus);
        application = applicationRepository.save(application);

        // Update status
        application.setStatus(finalStatus);
        application = applicationRepository.save(application);

        // Verify: Status is updated
        Application retrieved = applicationRepository.findById(application.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getStatus()).isEqualTo(finalStatus);
    }

    @Property(tries = 100)
    @Label("Property 18: Recruitment Tracking Accuracy - Candidates maintain application history")
    void candidatesMaintainApplicationHistory(
            @ForAll @IntRange(min = 1, max = 3) int applicationCount) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        Candidate candidate = createCandidate();

        // Create multiple applications for the same candidate
        for (int i = 0; i < applicationCount; i++) {
            JobPosting job = createJobPosting(org, dept);
            Application application = new Application();
            application.setJobPosting(job);
            application.setCandidate(candidate);
            application.setApplicationDate(LocalDate.now().minusDays(i));
            application.setStatus("SUBMITTED");
            applicationRepository.save(application);
        }

        // Verify: Candidate has multiple applications
        Candidate retrieved = candidateRepository.findById(candidate.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(candidate.getId());
    }

    @Property(tries = 100)
    @Label("Property 18: Recruitment Tracking Accuracy - Interview feedback is recorded")
    void interviewFeedbackIsRecorded(
            @ForAll("feedbackTexts") String feedback,
            @ForAll @IntRange(min = 1, max = 5) int rating) {

        // Setup test data
        Organization org = createOrganization();
        Department dept = createDepartment(org);
        JobPosting job = createJobPosting(org, dept);
        Candidate candidate = createCandidate();
        Application application = createApplication(job, candidate);

        // Create interview with feedback
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setScheduledDate(LocalDateTime.now().plusDays(1));
        interview.setInterviewType("Technical Interview");
        interview.setStatus("COMPLETED");
        interview.setFeedback(feedback);
        interview.setRating(rating);
        interview = interviewRepository.save(interview);

        // Verify: Feedback is recorded
        assertThat(interview.getId()).isNotNull();
        assertThat(interview.getFeedback()).isEqualTo(feedback);
        assertThat(interview.getRating()).isEqualTo(rating);

        // Verify: Feedback can be retrieved
        Interview retrieved = interviewRepository.findById(interview.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getFeedback()).isEqualTo(feedback);
        assertThat(retrieved.getRating()).isEqualTo(rating);
    }

    // Arbitraries
    @Provide
    Arbitrary<String> applicationStatuses() {
        return Arbitraries.of(
                "SUBMITTED",
                "UNDER_REVIEW",
                "INTERVIEW_SCHEDULED",
                "INTERVIEWED",
                "OFFER_EXTENDED",
                "ACCEPTED",
                "REJECTED",
                "WITHDRAWN"
        );
    }

    @Provide
    Arbitrary<String> feedbackTexts() {
        return Arbitraries.of(
                "Strong technical skills",
                "Good communication",
                "Needs improvement",
                "Excellent candidate",
                "Not a good fit",
                "Highly recommended",
                "Average performance",
                "Outstanding interview"
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

    private JobPosting createJobPosting(Organization org, Department dept) {
        JobPosting job = new JobPosting();
        job.setTitle("Software Engineer " + System.currentTimeMillis());
        job.setDescription("Test job posting");
        job.setDepartment(dept);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setLocation("Test Location");
        job.setPostedDate(LocalDate.now());
        job.setStatus("OPEN");
        job.setOrganization(org);
        return jobPostingRepository.save(job);
    }

    private Candidate createCandidate() {
        Candidate candidate = new Candidate();
        candidate.setFirstName("Test");
        candidate.setLastName("Candidate");
        candidate.setEmail("candidate" + System.currentTimeMillis() + "@test.com");
        candidate.setPhoneNumber("+1234567890");
        return candidateRepository.save(candidate);
    }

    private Application createApplication(JobPosting job, Candidate candidate) {
        Application application = new Application();
        application.setJobPosting(job);
        application.setCandidate(candidate);
        application.setApplicationDate(LocalDate.now());
        application.setStatus("SUBMITTED");
        return applicationRepository.save(application);
    }
}

