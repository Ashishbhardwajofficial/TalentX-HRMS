package com.talentx.hrms.service.recruitment;

import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Location;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import com.talentx.hrms.entity.recruitment.*;
import com.talentx.hrms.repository.*;
import com.talentx.hrms.service.auth.AuthService;
import com.talentx.hrms.service.employee.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecruitmentService {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;

    @Autowired
    public RecruitmentService(JobPostingRepository jobPostingRepository,
                             CandidateRepository candidateRepository,
                             ApplicationRepository applicationRepository,
                             InterviewRepository interviewRepository,
                             OrganizationRepository organizationRepository,
                             DepartmentRepository departmentRepository,
                             LocationRepository locationRepository,
                             EmployeeRepository employeeRepository,
                             AuthService authService) {
        this.jobPostingRepository = jobPostingRepository;
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.locationRepository = locationRepository;
        this.employeeRepository = employeeRepository;
        this.authService = authService;
    }

    // Job Posting Management
    public JobPosting createJobPosting(JobPosting jobPosting) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        jobPosting.setOrganization(currentOrg);
        
        if (jobPosting.getJobCode() != null && 
            jobPostingRepository.existsByOrganizationAndJobCode(currentOrg, jobPosting.getJobCode())) {
            throw new RuntimeException("Job code already exists in organization");
        }
        
        return jobPostingRepository.save(jobPosting);
    }

    public JobPosting updateJobPosting(Long id, JobPosting updatedJobPosting) {
        JobPosting existingJobPosting = getJobPostingById(id);
        
        // Update fields
        existingJobPosting.setJobTitle(updatedJobPosting.getJobTitle());
        existingJobPosting.setJobDescription(updatedJobPosting.getJobDescription());
        existingJobPosting.setRequirements(updatedJobPosting.getRequirements());
        existingJobPosting.setResponsibilities(updatedJobPosting.getResponsibilities());
        existingJobPosting.setQualifications(updatedJobPosting.getQualifications());
        existingJobPosting.setSkills(updatedJobPosting.getSkills());
        existingJobPosting.setEmploymentType(updatedJobPosting.getEmploymentType());
        existingJobPosting.setMinSalary(updatedJobPosting.getMinSalary());
        existingJobPosting.setMaxSalary(updatedJobPosting.getMaxSalary());
        existingJobPosting.setSalaryCurrency(updatedJobPosting.getSalaryCurrency());
        existingJobPosting.setIsSalaryNegotiable(updatedJobPosting.getIsSalaryNegotiable());
        existingJobPosting.setMinExperienceYears(updatedJobPosting.getMinExperienceYears());
        existingJobPosting.setMaxExperienceYears(updatedJobPosting.getMaxExperienceYears());
        existingJobPosting.setEducationLevel(updatedJobPosting.getEducationLevel());
        existingJobPosting.setPositionsAvailable(updatedJobPosting.getPositionsAvailable());
        existingJobPosting.setApplicationDeadline(updatedJobPosting.getApplicationDeadline());
        existingJobPosting.setExpectedStartDate(updatedJobPosting.getExpectedStartDate());
        existingJobPosting.setStatus(updatedJobPosting.getStatus());
        existingJobPosting.setIsInternalOnly(updatedJobPosting.getIsInternalOnly());
        existingJobPosting.setIsRemoteWork(updatedJobPosting.getIsRemoteWork());
        existingJobPosting.setIsUrgent(updatedJobPosting.getIsUrgent());
        existingJobPosting.setBenefits(updatedJobPosting.getBenefits());
        existingJobPosting.setNotes(updatedJobPosting.getNotes());
        existingJobPosting.setDepartment(updatedJobPosting.getDepartment());
        existingJobPosting.setLocation(updatedJobPosting.getLocation());
        existingJobPosting.setHiringManager(updatedJobPosting.getHiringManager());
        existingJobPosting.setRecruiter(updatedJobPosting.getRecruiter());
        
        return jobPostingRepository.save(existingJobPosting);
    }

    public JobPosting getJobPostingById(Long id) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return jobPostingRepository.findById(id)
                .filter(jp -> jp.getOrganization().equals(currentOrg))
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
    }

    public Page<JobPosting> getJobPostings(PaginationRequest request) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        Pageable pageable = createPageable(request);
        return jobPostingRepository.findByOrganization(currentOrg, pageable);
    }

    public Page<JobPosting> searchJobPostings(String jobTitle, String status, Long departmentId, 
                                            Long locationId, EmploymentType employmentType, 
                                            Boolean isRemoteWork, Boolean isUrgent, 
                                            PaginationRequest request) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        Department department = departmentId != null ? 
                departmentRepository.findById(departmentId).orElse(null) : null;
        Location location = locationId != null ? 
                locationRepository.findById(locationId).orElse(null) : null;
        
        Pageable pageable = createPageable(request);
        
        return jobPostingRepository.findBySearchCriteria(currentOrg, jobTitle, status, 
                department, location, employmentType, isRemoteWork, isUrgent, pageable);
    }

    public void deleteJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        if (!jobPosting.getApplications().isEmpty()) {
            throw new RuntimeException("Cannot delete job posting with existing applications");
        }
        jobPostingRepository.delete(jobPosting);
    }

    public JobPosting closeJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        jobPosting.setStatus("CLOSED");
        return jobPostingRepository.save(jobPosting);
    }

    // Candidate Management
    public Candidate createCandidate(Candidate candidate) {
        if (candidateRepository.existsByEmail(candidate.getEmail())) {
            throw new RuntimeException("Candidate with this email already exists");
        }
        return candidateRepository.save(candidate);
    }

    public Candidate updateCandidate(Long id, Candidate updatedCandidate) {
        Candidate existingCandidate = getCandidateById(id);
        
        // Update fields
        existingCandidate.setFirstName(updatedCandidate.getFirstName());
        existingCandidate.setMiddleName(updatedCandidate.getMiddleName());
        existingCandidate.setLastName(updatedCandidate.getLastName());
        existingCandidate.setEmail(updatedCandidate.getEmail());
        existingCandidate.setPhone(updatedCandidate.getPhone());
        existingCandidate.setMobile(updatedCandidate.getMobile());
        existingCandidate.setDateOfBirth(updatedCandidate.getDateOfBirth());
        existingCandidate.setGender(updatedCandidate.getGender());
        existingCandidate.setNationality(updatedCandidate.getNationality());
        existingCandidate.setAddress(updatedCandidate.getAddress());
        existingCandidate.setCity(updatedCandidate.getCity());
        existingCandidate.setStateProvince(updatedCandidate.getStateProvince());
        existingCandidate.setPostalCode(updatedCandidate.getPostalCode());
        existingCandidate.setCountry(updatedCandidate.getCountry());
        existingCandidate.setLinkedinProfile(updatedCandidate.getLinkedinProfile());
        existingCandidate.setPortfolioUrl(updatedCandidate.getPortfolioUrl());
        existingCandidate.setCurrentJobTitle(updatedCandidate.getCurrentJobTitle());
        existingCandidate.setCurrentEmployer(updatedCandidate.getCurrentEmployer());
        existingCandidate.setTotalExperienceYears(updatedCandidate.getTotalExperienceYears());
        existingCandidate.setHighestEducation(updatedCandidate.getHighestEducation());
        existingCandidate.setUniversity(updatedCandidate.getUniversity());
        existingCandidate.setFieldOfStudy(updatedCandidate.getFieldOfStudy());
        existingCandidate.setGraduationYear(updatedCandidate.getGraduationYear());
        existingCandidate.setSkills(updatedCandidate.getSkills());
        existingCandidate.setCertifications(updatedCandidate.getCertifications());
        existingCandidate.setSummary(updatedCandidate.getSummary());
        existingCandidate.setCurrentSalary(updatedCandidate.getCurrentSalary());
        existingCandidate.setExpectedSalary(updatedCandidate.getExpectedSalary());
        existingCandidate.setSalaryCurrency(updatedCandidate.getSalaryCurrency());
        existingCandidate.setNoticePeriodDays(updatedCandidate.getNoticePeriodDays());
        existingCandidate.setIsAvailableImmediately(updatedCandidate.getIsAvailableImmediately());
        existingCandidate.setIsWillingToRelocate(updatedCandidate.getIsWillingToRelocate());
        existingCandidate.setIsOpenToRemote(updatedCandidate.getIsOpenToRemote());
        existingCandidate.setSource(updatedCandidate.getSource());
        existingCandidate.setReferredBy(updatedCandidate.getReferredBy());
        existingCandidate.setResumePath(updatedCandidate.getResumePath());
        existingCandidate.setCoverLetterPath(updatedCandidate.getCoverLetterPath());
        existingCandidate.setNotes(updatedCandidate.getNotes());
        
        return candidateRepository.save(existingCandidate);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
    }

    public Page<Candidate> getCandidates(PaginationRequest request) {
        Pageable pageable = createPageable(request);
        return candidateRepository.findAll(pageable);
    }

    public Page<Candidate> searchCandidates(String name, String email, String jobTitle, String skills,
                                          Integer minExperience, Integer maxExperience, String education,
                                          String city, Boolean isAvailableImmediately, Boolean isWillingToRelocate,
                                          Boolean isOpenToRemote, PaginationRequest request) {
        Pageable pageable = createPageable(request);
        
        return candidateRepository.findBySearchCriteria(name, email, jobTitle, skills, 
                minExperience, maxExperience, education, city, isAvailableImmediately, 
                isWillingToRelocate, isOpenToRemote, pageable);
    }

    public void blacklistCandidate(Long id, String reason) {
        Candidate candidate = getCandidateById(id);
        candidate.blacklist(reason);
        candidateRepository.save(candidate);
    }

    public void removeFromBlacklist(Long id) {
        Candidate candidate = getCandidateById(id);
        candidate.removeFromBlacklist();
        candidateRepository.save(candidate);
    }

    // Application Management
    public Application createApplication(Long candidateId, Long jobPostingId, Application application) {
        Candidate candidate = getCandidateById(candidateId);
        JobPosting jobPosting = getJobPostingById(jobPostingId);
        
        if (applicationRepository.existsByCandidateAndJobPosting(candidate, jobPosting)) {
            throw new RuntimeException("Candidate has already applied for this job");
        }
        
        if (!jobPosting.isActive()) {
            throw new RuntimeException("Job posting is not active");
        }
        
        if (jobPosting.isExpired()) {
            throw new RuntimeException("Job posting application deadline has passed");
        }
        
        application.setCandidate(candidate);
        application.setJobPosting(jobPosting);
        application.setApplicationDate(LocalDate.now());
        application.setStatus("APPLIED");
        
        return applicationRepository.save(application);
    }

    public Application updateApplication(Long id, Application updatedApplication) {
        Application existingApplication = getApplicationById(id);
        
        existingApplication.setCoverLetter(updatedApplication.getCoverLetter());
        existingApplication.setResumePath(updatedApplication.getResumePath());
        existingApplication.setPortfolioPath(updatedApplication.getPortfolioPath());
        existingApplication.setExpectedSalary(updatedApplication.getExpectedSalary());
        existingApplication.setSalaryCurrency(updatedApplication.getSalaryCurrency());
        existingApplication.setNoticePeriodDays(updatedApplication.getNoticePeriodDays());
        existingApplication.setIsAvailableImmediately(updatedApplication.getIsAvailableImmediately());
        existingApplication.setEarliestStartDate(updatedApplication.getEarliestStartDate());
        existingApplication.setIsWillingToRelocate(updatedApplication.getIsWillingToRelocate());
        existingApplication.setIsOpenToRemote(updatedApplication.getIsOpenToRemote());
        existingApplication.setAdditionalNotes(updatedApplication.getAdditionalNotes());
        existingApplication.setInternalNotes(updatedApplication.getInternalNotes());
        
        return applicationRepository.save(existingApplication);
    }

    public Application getApplicationById(Long id) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return applicationRepository.findById(id)
                .filter(app -> app.getJobPosting().getOrganization().equals(currentOrg))
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public Page<Application> getApplications(PaginationRequest request) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        Pageable pageable = createPageable(request);
        return applicationRepository.findByOrganization(currentOrg, pageable);
    }

    public Page<Application> getApplicationsByJobPosting(Long jobPostingId, PaginationRequest request) {
        JobPosting jobPosting = getJobPostingById(jobPostingId);
        Pageable pageable = createPageable(request);
        return applicationRepository.findByJobPosting(jobPosting, pageable);
    }

    public Page<Application> getApplicationsByCandidate(Long candidateId, PaginationRequest request) {
        Candidate candidate = getCandidateById(candidateId);
        Pageable pageable = createPageable(request);
        return applicationRepository.findByCandidate(candidate, pageable);
    }

    public Application screenApplication(Long id, String screenedBy, Integer score, String notes) {
        Application application = getApplicationById(id);
        application.moveToScreening(screenedBy, score, notes);
        return applicationRepository.save(application);
    }

    public Application rejectApplication(Long id, String reason) {
        Application application = getApplicationById(id);
        application.reject(reason);
        return applicationRepository.save(application);
    }

    public Application withdrawApplication(Long id, String reason) {
        Application application = getApplicationById(id);
        application.withdraw(reason);
        return applicationRepository.save(application);
    }

    // Interview Management
    public Interview scheduleInterview(Long applicationId, Interview interview) {
        Application application = getApplicationById(applicationId);
        
        interview.setApplication(application);
        interview.setCandidate(application.getCandidate());
        interview.setStatus("SCHEDULED");
        
        Interview savedInterview = interviewRepository.save(interview);
        
        // Update application status
        application.scheduleInterview();
        applicationRepository.save(application);
        
        return savedInterview;
    }

    public Interview updateInterview(Long id, Interview updatedInterview) {
        Interview existingInterview = getInterviewById(id);
        
        existingInterview.setInterviewType(updatedInterview.getInterviewType());
        existingInterview.setRound(updatedInterview.getRound());
        existingInterview.setScheduledDateTime(updatedInterview.getScheduledDateTime());
        existingInterview.setDurationMinutes(updatedInterview.getDurationMinutes());
        existingInterview.setLocation(updatedInterview.getLocation());
        existingInterview.setMeetingLink(updatedInterview.getMeetingLink());
        existingInterview.setInterviewer(updatedInterview.getInterviewer());
        existingInterview.setAdditionalInterviewers(updatedInterview.getAdditionalInterviewers());
        existingInterview.setNotes(updatedInterview.getNotes());
        
        return interviewRepository.save(existingInterview);
    }

    public Interview getInterviewById(Long id) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return interviewRepository.findById(id)
                .filter(interview -> interview.getApplication().getJobPosting().getOrganization().equals(currentOrg))
                .orElseThrow(() -> new RuntimeException("Interview not found"));
    }

    public Page<Interview> getInterviews(PaginationRequest request) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        Pageable pageable = createPageable(request);
        return interviewRepository.findByOrganization(currentOrg, pageable);
    }

    public List<Interview> getTodaysInterviews() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return interviewRepository.findTodaysInterviewsByOrganization(currentOrg);
    }

    public List<Interview> getUpcomingInterviews(int days) {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        Instant endDate = Instant.now().plusSeconds(days * 24 * 60 * 60);
        return interviewRepository.findUpcomingByOrganization(currentOrg, endDate);
    }

    public Interview startInterview(Long id) {
        Interview interview = getInterviewById(id);
        interview.start();
        return interviewRepository.save(interview);
    }

    public Interview completeInterview(Long id, Integer overallRating, Integer technicalRating,
                                     Integer communicationRating, Integer culturalFitRating,
                                     String feedback, String strengths, String areasForImprovement,
                                     String recommendation, String notes) {
        Interview interview = getInterviewById(id);
        
        interview.setOverallRating(overallRating);
        interview.setTechnicalRating(technicalRating);
        interview.setCommunicationRating(communicationRating);
        interview.setCulturalFitRating(culturalFitRating);
        interview.setFeedback(feedback);
        interview.setStrengths(strengths);
        interview.setAreasForImprovement(areasForImprovement);
        interview.setRecommendation(recommendation != null ? Interview.InterviewRecommendation.valueOf(recommendation) : null);
        interview.setNotes(notes);
        
        interview.complete();
        return interviewRepository.save(interview);
    }

    public Interview cancelInterview(Long id, String reason, String cancelledBy) {
        Interview interview = getInterviewById(id);
        interview.cancel(reason, cancelledBy);
        return interviewRepository.save(interview);
    }

    public Interview rescheduleInterview(Long id, Instant newDateTime, String reason) {
        Interview interview = getInterviewById(id);
        interview.reschedule(newDateTime, reason);
        return interviewRepository.save(interview);
    }

    // Hiring Workflow
    public Application extendOffer(Long applicationId, BigDecimal offerAmount) {
        Application application = getApplicationById(applicationId);
        application.extendOffer(offerAmount);
        return applicationRepository.save(application);
    }

    public Application acceptOffer(Long applicationId) {
        Application application = getApplicationById(applicationId);
        application.acceptOffer();
        return applicationRepository.save(application);
    }

    public Application rejectOffer(Long applicationId, String reason) {
        Application application = getApplicationById(applicationId);
        application.rejectOffer(reason);
        return applicationRepository.save(application);
    }

    public Employee hireCandidate(Long applicationId) {
        Application application = getApplicationById(applicationId);
        
        if (!"OFFER".equals(application.getStatus()) || application.getOfferAcceptedAt() == null) {
            throw new RuntimeException("Cannot hire candidate without accepted offer");
        }
        
        // Create employee from candidate
        Candidate candidate = application.getCandidate();
        JobPosting jobPosting = application.getJobPosting();
        
        Employee employee = new Employee();
        employee.setFirstName(candidate.getFirstName());
        employee.setMiddleName(candidate.getMiddleName());
        employee.setLastName(candidate.getLastName());
        employee.setWorkEmail(candidate.getEmail());
        employee.setPhoneNumber(candidate.getPhone());
        employee.setMobile(candidate.getMobile());
        employee.setDateOfBirth(candidate.getDateOfBirth() != null ? java.sql.Date.valueOf(candidate.getDateOfBirth()) : null);
        employee.setGender(candidate.getGender() != null ? Gender.valueOf(candidate.getGender()) : null);
        employee.setNationality(candidate.getNationality());
        employee.setOrganization(jobPosting.getOrganization());
        employee.setDepartment(jobPosting.getDepartment());
        employee.setLocation(jobPosting.getLocation());
        employee.setJobTitle(jobPosting.getJobTitle());
        employee.setEmploymentType(jobPosting.getEmploymentType());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setHireDate(java.sql.Date.valueOf(LocalDate.now()));
        employee.setManager(jobPosting.getHiringManager());
        
        if (application.getOfferAmount() != null) {
            employee.setSalaryAmount(application.getOfferAmount());
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Update application status
        application.hire();
        applicationRepository.save(application);
        
        // Update job posting positions filled
        jobPosting.setPositionsFilled(jobPosting.getPositionsFilled() + 1);
        if (!jobPosting.hasOpenPositions()) {
            jobPosting.setStatus("CLOSED");
        }
        jobPostingRepository.save(jobPosting);
        
        return savedEmployee;
    }

    // Analytics and Reporting
    public List<Object[]> getJobPostingStatistics() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return jobPostingRepository.getJobPostingStatsByOrganization(currentOrg);
    }

    public List<Object[]> getApplicationStatistics() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return applicationRepository.getApplicationStatsByOrganization(currentOrg);
    }

    public List<Object[]> getInterviewStatistics() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return interviewRepository.getInterviewStatsByOrganization(currentOrg);
    }

    public List<Object[]> getCandidateSourceStatistics() {
        return candidateRepository.countCandidatesBySource();
    }

    public long getActiveJobPostingsCount() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return jobPostingRepository.countByOrganizationAndStatus(currentOrg, "ACTIVE");
    }

    public long getPendingApplicationsCount() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return applicationRepository.countByOrganizationAndStatus(currentOrg, "APPLIED");
    }

    public long getScheduledInterviewsCount() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        return interviewRepository.countByOrganizationAndStatus(currentOrg, "SCHEDULED");
    }

    // Helper method for pagination
    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort sort = Sort.unsorted();
        if (paginationRequest.getSortBy() != null && !paginationRequest.getSortBy().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, paginationRequest.getSortBy());
        }

        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

    // Additional methods required by RecruitmentController
    
    /**
     * Get all job postings with filtering (returns DTOs)
     */
    @Transactional(readOnly = true)
    public Page<com.talentx.hrms.dto.recruitment.JobPostingDTO> getAllJobPostings(
            Boolean isActive, Boolean isPublished, Long departmentId, 
            EmploymentType employmentType, String location, PaginationRequest paginationRequest) {
        
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        
        Department department = departmentId != null ? 
                departmentRepository.findById(departmentId).orElse(null) : null;
        Location loc = location != null && !location.isEmpty() ? 
                locationRepository.findByName(location).orElse(null) : null;
        
        String status = isActive != null && isActive ? "ACTIVE" : null;
        Boolean isRemote = null;
        Boolean isUrgent = null;
        
        Pageable pageable = createPageable(paginationRequest);
        Page<JobPosting> jobPostings = jobPostingRepository.findBySearchCriteria(
            currentOrg, null, status, department, loc, employmentType, isRemote, isUrgent, pageable);
        
        return jobPostings.map(this::convertJobPostingToDTO);
    }
    
    /**
     * Get public job postings (returns DTOs)
     */
    @Transactional(readOnly = true)
    public Page<com.talentx.hrms.dto.recruitment.JobPostingDTO> getPublicJobPostings(
            Long departmentId, EmploymentType employmentType, String location, 
            String keyword, PaginationRequest paginationRequest) {
        
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        
        Department department = departmentId != null ? 
                departmentRepository.findById(departmentId).orElse(null) : null;
        Location loc = location != null && !location.isEmpty() ? 
                locationRepository.findByName(location).orElse(null) : null;
        
        Pageable pageable = createPageable(paginationRequest);
        Page<JobPosting> jobPostings = jobPostingRepository.findBySearchCriteria(
            currentOrg, keyword, "ACTIVE", department, loc, employmentType, null, null, pageable);
        
        return jobPostings.map(this::convertJobPostingToDTO);
    }
    
    /**
     * Get job posting by ID (returns DTO)
     */
    @Transactional(readOnly = true)
    public com.talentx.hrms.dto.recruitment.JobPostingDTO getJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Get public job posting by ID (returns DTO)
     */
    @Transactional(readOnly = true)
    public com.talentx.hrms.dto.recruitment.JobPostingDTO getPublicJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
            .filter(jp -> "ACTIVE".equals(jp.getStatus()))
            .orElseThrow(() -> new RuntimeException("Job posting not found or not published"));
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Create job posting from DTO
     */
    public com.talentx.hrms.dto.recruitment.JobPostingDTO createJobPosting(com.talentx.hrms.dto.recruitment.JobPostingDTO dto) {
        JobPosting jobPosting = convertDTOToJobPosting(dto);
        jobPosting = createJobPosting(jobPosting);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Update job posting from DTO
     */
    public com.talentx.hrms.dto.recruitment.JobPostingDTO updateJobPosting(Long id, com.talentx.hrms.dto.recruitment.JobPostingDTO dto) {
        JobPosting updatedJobPosting = convertDTOToJobPosting(dto);
        JobPosting jobPosting = updateJobPosting(id, updatedJobPosting);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Publish job posting
     */
    public com.talentx.hrms.dto.recruitment.JobPostingDTO publishJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        jobPosting.setStatus("ACTIVE");
        jobPosting = jobPostingRepository.save(jobPosting);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Unpublish job posting
     */
    public com.talentx.hrms.dto.recruitment.JobPostingDTO unpublishJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        jobPosting.setStatus("DRAFT");
        jobPosting = jobPostingRepository.save(jobPosting);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Close job posting (returns DTO)
     */
    public com.talentx.hrms.dto.recruitment.JobPostingDTO closeJobPostingDTO(Long id) {
        JobPosting jobPosting = closeJobPosting(id);
        return convertJobPostingToDTO(jobPosting);
    }
    
    /**
     * Get all applications with filtering (returns DTOs)
     */
    @Transactional(readOnly = true)
    public Page<com.talentx.hrms.dto.recruitment.ApplicationDTO> getAllApplications(
            Long jobPostingId, String status, String stage, PaginationRequest paginationRequest) {
        
        Pageable pageable = createPageable(paginationRequest);
        Page<Application> applications;
        
        if (jobPostingId != null) {
            JobPosting jobPosting = getJobPostingById(jobPostingId);
            applications = applicationRepository.findByJobPosting(jobPosting, pageable);
        } else {
            applications = getApplications(paginationRequest);
        }
        
        return applications.map(this::convertApplicationToDTO);
    }
    
    /**
     * Get job applications (returns DTOs)
     */
    @Transactional(readOnly = true)
    public List<com.talentx.hrms.dto.recruitment.ApplicationDTO> getJobApplications(Long jobId, String status) {
        JobPosting jobPosting = getJobPostingById(jobId);
        List<Application> applications = applicationRepository.findByJobPosting(jobPosting, Pageable.unpaged()).getContent();
        
        if (status != null && !status.isEmpty()) {
            applications = applications.stream()
                .filter(app -> status.equals(app.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        return applications.stream()
            .map(this::convertApplicationToDTO)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get application by ID (returns DTO)
     */
    @Transactional(readOnly = true)
    public com.talentx.hrms.dto.recruitment.ApplicationDTO getApplication(Long id) {
        Application application = getApplicationById(id);
        return convertApplicationToDTO(application);
    }
    
    /**
     * Submit application with files
     */
    public com.talentx.hrms.dto.recruitment.ApplicationDTO submitApplication(
            Long jobId, com.talentx.hrms.dto.recruitment.ApplicationDTO dto, 
            org.springframework.web.multipart.MultipartFile resume, 
            org.springframework.web.multipart.MultipartFile portfolio) {
        
        // Create or get candidate
        Candidate candidate = candidateRepository.findByEmail(dto.getCandidateEmail())
            .orElseGet(() -> {
                Candidate newCandidate = new Candidate();
                newCandidate.setEmail(dto.getCandidateEmail());
                newCandidate.setFirstName(dto.getCandidateFirstName());
                newCandidate.setLastName(dto.getCandidateLastName());
                newCandidate.setPhone(dto.getCandidatePhone());
                return candidateRepository.save(newCandidate);
            });
        
        Application application = new Application();
        application.setCoverLetter(dto.getCoverLetter());
        application.setExpectedSalary(dto.getExpectedSalary());
        
        // Handle file uploads (placeholder - would integrate with file storage service)
        if (resume != null) {
            application.setResumePath("/uploads/resumes/" + resume.getOriginalFilename());
        }
        if (portfolio != null) {
            application.setPortfolioPath("/uploads/portfolios/" + portfolio.getOriginalFilename());
        }
        
        application = createApplication(candidate.getId(), jobId, application);
        return convertApplicationToDTO(application);
    }
    
    /**
     * Update application status
     */
    public com.talentx.hrms.dto.recruitment.ApplicationDTO updateApplicationStatus(
            Long id, String status, String stage, String notes) {
        
        Application application = getApplicationById(id);
        application.setStatus(status);
        if (stage != null) {
            application.setCurrentStage(stage);
        }
        if (notes != null) {
            application.setInternalNotes(notes);
        }
        application = applicationRepository.save(application);
        return convertApplicationToDTO(application);
    }
    
    /**
     * Rate application
     */
    public com.talentx.hrms.dto.recruitment.ApplicationDTO rateApplication(Long id, Integer rating, String feedback) {
        Application application = screenApplication(id, authService.getCurrentUser().getUsername(), rating, feedback);
        return convertApplicationToDTO(application);
    }
    
    /**
     * Schedule interview (returns map)
     */
    public Map<String, Object> scheduleInterview(Long applicationId, Map<String, Object> interviewDetails) {
        Interview interview = new Interview();
        String interviewTypeStr = (String) interviewDetails.get("interviewType");
        interview.setInterviewType(interviewTypeStr != null ? Interview.InterviewType.valueOf(interviewTypeStr) : null);
        interview.setRound((String) interviewDetails.get("round"));
        interview.setScheduledDateTime(java.time.Instant.parse((String) interviewDetails.get("scheduledDateTime")));
        interview.setDurationMinutes((Integer) interviewDetails.get("durationMinutes"));
        interview.setLocation((String) interviewDetails.get("location"));
        interview.setMeetingLink((String) interviewDetails.get("meetingLink"));
        
        interview = scheduleInterview(applicationId, interview);
        return convertInterviewToMap(interview);
    }
    
    /**
     * Get application interviews
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getApplicationInterviews(Long applicationId) {
        Application application = getApplicationById(applicationId);
        List<Interview> interviews = interviewRepository.findByApplication(application);
        return interviews.stream()
            .map(this::convertInterviewToMap)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Update interview feedback
     */
    public Map<String, Object> updateInterviewFeedback(Long interviewId, Map<String, Object> feedback) {
        Interview interview = getInterviewById(interviewId);
        
        interview.setOverallRating((Integer) feedback.get("overallRating"));
        interview.setTechnicalRating((Integer) feedback.get("technicalRating"));
        interview.setCommunicationRating((Integer) feedback.get("communicationRating"));
        interview.setCulturalFitRating((Integer) feedback.get("culturalFitRating"));
        interview.setFeedback((String) feedback.get("feedback"));
        interview.setStrengths((String) feedback.get("strengths"));
        interview.setAreasForImprovement((String) feedback.get("areasForImprovement"));
        String recommendationStr = (String) feedback.get("recommendation");
        interview.setRecommendation(recommendationStr != null ? Interview.InterviewRecommendation.valueOf(recommendationStr) : null);
        
        interview.complete();
        interview = interviewRepository.save(interview);
        
        return convertInterviewToMap(interview);
    }
    
    /**
     * Hire candidate (returns map)
     */
    public Map<String, Object> hireCandidate(Long applicationId, Map<String, Object> employeeDetails) {
        Employee employee = hireCandidate(applicationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employee.getId());
        result.put("employeeNumber", employee.getEmployeeNumber());
        result.put("firstName", employee.getFirstName());
        result.put("lastName", employee.getLastName());
        result.put("email", employee.getWorkEmail());
        result.put("jobTitle", employee.getJobTitle());
        result.put("hireDate", employee.getHireDate());
        
        return result;
    }
    
    /**
     * Get recruitment statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRecruitmentStatistics(Integer year, Long departmentId) {
        Map<String, Object> stats = new HashMap<>();
        
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        
        stats.put("activeJobPostings", getActiveJobPostingsCount());
        stats.put("pendingApplications", getPendingApplicationsCount());
        stats.put("scheduledInterviews", getScheduledInterviewsCount());
        stats.put("totalApplications", applicationRepository.countByOrganization(currentOrg));
        stats.put("totalCandidates", candidateRepository.count());
        
        return stats;
    }
    
    /**
     * Get recruitment pipeline
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRecruitmentPipeline() {
        User currentUser = authService.getCurrentUser();
        Organization currentOrg = currentUser.getOrganization();
        
        Map<String, Object> pipeline = new HashMap<>();
        
        pipeline.put("applied", applicationRepository.countByOrganizationAndStatus(currentOrg, "APPLIED"));
        pipeline.put("screening", applicationRepository.countByOrganizationAndStatus(currentOrg, "SCREENING"));
        pipeline.put("interview", applicationRepository.countByOrganizationAndStatus(currentOrg, "INTERVIEW"));
        pipeline.put("offer", applicationRepository.countByOrganizationAndStatus(currentOrg, "OFFER"));
        pipeline.put("hired", applicationRepository.countByOrganizationAndStatus(currentOrg, "HIRED"));
        pipeline.put("rejected", applicationRepository.countByOrganizationAndStatus(currentOrg, "REJECTED"));
        
        return pipeline;
    }
    
    // Helper methods for DTO conversion
    
    private com.talentx.hrms.dto.recruitment.JobPostingDTO convertJobPostingToDTO(JobPosting jobPosting) {
        com.talentx.hrms.dto.recruitment.JobPostingDTO dto = new com.talentx.hrms.dto.recruitment.JobPostingDTO();
        dto.setId(jobPosting.getId());
        dto.setJobCode(jobPosting.getJobCode());
        dto.setTitle(jobPosting.getJobTitle());
        dto.setDescription(jobPosting.getJobDescription());
        dto.setRequirements(jobPosting.getRequirements());
        dto.setResponsibilities(jobPosting.getResponsibilities());
        dto.setQualifications(jobPosting.getQualifications());
        dto.setEmploymentType(jobPosting.getEmploymentType());
        dto.setSalaryMin(jobPosting.getMinSalary());
        dto.setSalaryMax(jobPosting.getMaxSalary());
        dto.setSalaryCurrency(jobPosting.getSalaryCurrency());
        dto.setNumberOfPositions(jobPosting.getPositionsAvailable());
        dto.setClosingDate(jobPosting.getApplicationDeadline());
        dto.setIsInternal(jobPosting.getIsInternalOnly());
        dto.setBenefits(jobPosting.getBenefits());
        dto.setCreatedAt(jobPosting.getCreatedAt());
        
        if (jobPosting.getDepartment() != null) {
            dto.setDepartmentId(jobPosting.getDepartment().getId());
            dto.setDepartmentName(jobPosting.getDepartment().getName());
        }
        if (jobPosting.getLocation() != null) {
            dto.setLocationId(jobPosting.getLocation().getId());
            dto.setLocationName(jobPosting.getLocation().getName());
        }
        
        return dto;
    }
    
    private JobPosting convertDTOToJobPosting(com.talentx.hrms.dto.recruitment.JobPostingDTO dto) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setJobCode(dto.getJobCode());
        jobPosting.setJobTitle(dto.getTitle());
        jobPosting.setJobDescription(dto.getDescription());
        jobPosting.setRequirements(dto.getRequirements());
        jobPosting.setResponsibilities(dto.getResponsibilities());
        jobPosting.setQualifications(dto.getQualifications());
        jobPosting.setEmploymentType(dto.getEmploymentType());
        jobPosting.setMinSalary(dto.getSalaryMin());
        jobPosting.setMaxSalary(dto.getSalaryMax());
        jobPosting.setSalaryCurrency(dto.getSalaryCurrency());
        jobPosting.setPositionsAvailable(dto.getNumberOfPositions());
        jobPosting.setApplicationDeadline(dto.getClosingDate());
        jobPosting.setIsInternalOnly(dto.getIsInternal());
        jobPosting.setBenefits(dto.getBenefits());
        
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            jobPosting.setDepartment(department);
        }
        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId()).orElse(null);
            jobPosting.setLocation(location);
        }
        
        return jobPosting;
    }
    
    private com.talentx.hrms.dto.recruitment.ApplicationDTO convertApplicationToDTO(Application application) {
        com.talentx.hrms.dto.recruitment.ApplicationDTO dto = new com.talentx.hrms.dto.recruitment.ApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        dto.setCurrentStage(application.getCurrentStage());
        dto.setCoverLetter(application.getCoverLetter());
        dto.setResumePath(application.getResumePath());
        dto.setExpectedSalary(application.getExpectedSalary());
        dto.setSalaryCurrency(application.getSalaryCurrency());
        
        if (application.getCandidate() != null) {
            dto.setCandidateId(application.getCandidate().getId());
            dto.setCandidateFirstName(application.getCandidate().getFirstName());
            dto.setCandidateLastName(application.getCandidate().getLastName());
            dto.setCandidateEmail(application.getCandidate().getEmail());
            dto.setCandidatePhone(application.getCandidate().getPhone());
        }
        
        if (application.getJobPosting() != null) {
            dto.setJobPostingId(application.getJobPosting().getId());
            dto.setJobTitle(application.getJobPosting().getJobTitle());
        }
        
        return dto;
    }
    
    private Map<String, Object> convertInterviewToMap(Interview interview) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", interview.getId());
        map.put("interviewType", interview.getInterviewType());
        map.put("round", interview.getRound());
        map.put("scheduledDateTime", interview.getScheduledDateTime());
        map.put("durationMinutes", interview.getDurationMinutes());
        map.put("location", interview.getLocation());
        map.put("meetingLink", interview.getMeetingLink());
        map.put("status", interview.getStatus());
        map.put("overallRating", interview.getOverallRating());
        map.put("feedback", interview.getFeedback());
        map.put("recommendation", interview.getRecommendation());
        return map;
    }
}

