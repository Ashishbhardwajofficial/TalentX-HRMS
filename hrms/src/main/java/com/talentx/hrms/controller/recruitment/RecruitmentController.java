package com.talentx.hrms.controller.recruitment;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.dto.recruitment.ApplicationDTO;
import com.talentx.hrms.dto.recruitment.JobPostingDTO;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.recruitment.JobPosting;
import com.talentx.hrms.service.recruitment.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment")
@Tag(name = "Recruitment Management", description = "Job posting and application management operations")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @Autowired
    public RecruitmentController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    // ========== JOB POSTING ENDPOINTS ==========

    /**
     * Get all job postings with pagination
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get all job postings", description = "Retrieve all job postings with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<JobPostingDTO>>> getAllJobPostings(
            @Parameter(description = "Active status filter") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Published status filter") @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "Department ID filter") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Employment type filter") @RequestParam(required = false) EmploymentType employmentType,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        Page<JobPostingDTO> jobPostings = recruitmentService.getAllJobPostings(
            isActive, isPublished, departmentId, employmentType, location, paginationRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Job postings retrieved successfully", jobPostings));
    }

    /**
     * Get public job postings (for career page)
     */
    @GetMapping("/jobs/public")
    @Operation(summary = "Get public job postings", description = "Get published job postings for public career page")
    public ResponseEntity<ApiResponse<Page<JobPostingDTO>>> getPublicJobPostings(
            @Parameter(description = "Department ID filter") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Employment type filter") @RequestParam(required = false) EmploymentType employmentType,
            @Parameter(description = "Location filter") @RequestParam(required = false) String location,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, "postingDate", "desc");
        Page<JobPostingDTO> jobPostings = recruitmentService.getPublicJobPostings(
            departmentId, employmentType, location, keyword, paginationRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Public job postings retrieved successfully", jobPostings));
    }

    /**
     * Get job posting by ID
     */
    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get job posting by ID", description = "Retrieve a specific job posting by ID")
    public ResponseEntity<ApiResponse<JobPostingDTO>> getJobPosting(@PathVariable Long id) {
        try {
            JobPostingDTO jobPosting = recruitmentService.getJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("Job posting retrieved successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get public job posting by ID
     */
    @GetMapping("/jobs/{id}/public")
    @Operation(summary = "Get public job posting", description = "Get published job posting for public viewing")
    public ResponseEntity<ApiResponse<JobPostingDTO>> getPublicJobPosting(@PathVariable Long id) {
        try {
            JobPostingDTO jobPosting = recruitmentService.getPublicJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("Job posting retrieved successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create new job posting
     */
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Create job posting", description = "Create a new job posting")
    public ResponseEntity<ApiResponse<JobPostingDTO>> createJobPosting(@Valid @RequestBody JobPostingDTO request) {
        try {
            JobPostingDTO jobPosting = recruitmentService.createJobPosting(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posting created successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update job posting
     */
    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Update job posting", description = "Update an existing job posting")
    public ResponseEntity<ApiResponse<JobPostingDTO>> updateJobPosting(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingDTO request) {
        try {
            JobPostingDTO jobPosting = recruitmentService.updateJobPosting(id, request);
            return ResponseEntity.ok(ApiResponse.success("Job posting updated successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Publish job posting
     */
    @PostMapping("/jobs/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Publish job posting", description = "Publish a job posting to make it public")
    public ResponseEntity<ApiResponse<JobPostingDTO>> publishJobPosting(@PathVariable Long id) {
        try {
            JobPostingDTO jobPosting = recruitmentService.publishJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("Job posting published successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Unpublish job posting
     */
    @PostMapping("/jobs/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Unpublish job posting", description = "Unpublish a job posting to make it private")
    public ResponseEntity<ApiResponse<JobPostingDTO>> unpublishJobPosting(@PathVariable Long id) {
        try {
            JobPostingDTO jobPosting = recruitmentService.unpublishJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("Job posting unpublished successfully", jobPosting));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Close job posting
     */
    @PostMapping("/jobs/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Close job posting", description = "Close a job posting to stop accepting applications")
    public ResponseEntity<ApiResponse<JobPostingDTO>> closeJobPosting(@PathVariable Long id) {
        try {
            JobPosting jobPosting = recruitmentService.closeJobPosting(id);
            JobPostingDTO dto = convertJobPostingToDTO(jobPosting);
            return ResponseEntity.ok(ApiResponse.success("Job posting closed successfully", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    private JobPostingDTO convertJobPostingToDTO(JobPosting jobPosting) {
        JobPostingDTO dto = new JobPostingDTO();
        dto.setId(jobPosting.getId());
        dto.setJobCode(jobPosting.getJobCode());
        dto.setTitle(jobPosting.getJobTitle());
        dto.setDescription(jobPosting.getJobDescription());
        dto.setRequirements(jobPosting.getRequirements());
        dto.setResponsibilities(jobPosting.getResponsibilities());
        dto.setQualifications(jobPosting.getQualifications());
        dto.setBenefits(jobPosting.getBenefits());
        dto.setEmploymentType(jobPosting.getEmploymentType());
        dto.setSalaryMin(jobPosting.getMinSalary());
        dto.setSalaryMax(jobPosting.getMaxSalary());
        dto.setSalaryCurrency(jobPosting.getSalaryCurrency());
        dto.setNumberOfPositions(jobPosting.getPositionsAvailable());
        dto.setClosingDate(jobPosting.getApplicationDeadline());
        dto.setIsActive("ACTIVE".equals(jobPosting.getStatus()));
        dto.setIsPublished("ACTIVE".equals(jobPosting.getStatus()));
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

    /**
     * Delete job posting
     */
    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete job posting", description = "Delete a job posting (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(@PathVariable Long id) {
        try {
            recruitmentService.deleteJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("Job posting deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== APPLICATION ENDPOINTS ==========

    /**
     * Get all applications with pagination
     */
    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get all applications", description = "Retrieve all applications with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<ApplicationDTO>>> getAllApplications(
            @Parameter(description = "Job posting ID filter") @RequestParam(required = false) Long jobPostingId,
            @Parameter(description = "Application status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Current stage filter") @RequestParam(required = false) String stage,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "appliedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        Page<ApplicationDTO> applications = recruitmentService.getAllApplications(
            jobPostingId, status, stage, paginationRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
    }

    /**
     * Get applications for a specific job posting
     */
    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get job applications", description = "Get applications for a specific job posting")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getJobApplications(
            @PathVariable Long jobId,
            @Parameter(description = "Application status filter") @RequestParam(required = false) String status) {
        
        List<ApplicationDTO> applications = recruitmentService.getJobApplications(jobId, status);
        return ResponseEntity.ok(ApiResponse.success("Job applications retrieved successfully", applications));
    }

    /**
     * Get application by ID
     */
    @GetMapping("/applications/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get application by ID", description = "Retrieve a specific application by ID")
    public ResponseEntity<ApiResponse<ApplicationDTO>> getApplication(@PathVariable Long id) {
        try {
            ApplicationDTO application = recruitmentService.getApplication(id);
            return ResponseEntity.ok(ApiResponse.success("Application retrieved successfully", application));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit job application (public endpoint)
     */
    @PostMapping("/jobs/{jobId}/apply")
    @Operation(summary = "Submit application", description = "Submit a job application")
    public ResponseEntity<ApiResponse<ApplicationDTO>> submitApplication(
            @PathVariable Long jobId,
            @Valid @RequestBody ApplicationDTO request,
            @RequestParam(required = false) MultipartFile resume,
            @RequestParam(required = false) MultipartFile portfolio) {
        try {
            ApplicationDTO application = recruitmentService.submitApplication(jobId, request, resume, portfolio);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", application));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update application status
     */
    @PostMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Update application status", description = "Update application status and stage")
    public ResponseEntity<ApiResponse<ApplicationDTO>> updateApplicationStatus(
            @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status,
            @Parameter(description = "New stage") @RequestParam(required = false) String stage,
            @Parameter(description = "Notes") @RequestParam(required = false) String notes) {
        try {
            ApplicationDTO application = recruitmentService.updateApplicationStatus(id, status, stage, notes);
            return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", application));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Rate application
     */
    @PostMapping("/applications/{id}/rate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Rate application", description = "Rate an application")
    public ResponseEntity<ApiResponse<ApplicationDTO>> rateApplication(
            @PathVariable Long id,
            @Parameter(description = "Rating (1-5)") @RequestParam Integer rating,
            @Parameter(description = "Feedback") @RequestParam(required = false) String feedback) {
        try {
            ApplicationDTO application = recruitmentService.rateApplication(id, rating, feedback);
            return ResponseEntity.ok(ApiResponse.success("Application rated successfully", application));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== INTERVIEW ENDPOINTS ==========

    /**
     * Schedule interview
     */
    @PostMapping("/applications/{id}/schedule-interview")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Schedule interview", description = "Schedule an interview for an application")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scheduleInterview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> interviewDetails) {
        try {
            Map<String, Object> interview = recruitmentService.scheduleInterview(id, interviewDetails);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview scheduled successfully", interview));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get interviews for application
     */
    @GetMapping("/applications/{id}/interviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get application interviews", description = "Get interviews for a specific application")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplicationInterviews(@PathVariable Long id) {
        List<Map<String, Object>> interviews = recruitmentService.getApplicationInterviews(id);
        return ResponseEntity.ok(ApiResponse.success("Application interviews retrieved successfully", interviews));
    }

    /**
     * Update interview feedback
     */
    @PostMapping("/interviews/{interviewId}/feedback")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Update interview feedback", description = "Update feedback for an interview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateInterviewFeedback(
            @PathVariable Long interviewId,
            @RequestBody Map<String, Object> feedback) {
        try {
            Map<String, Object> interview = recruitmentService.updateInterviewFeedback(interviewId, feedback);
            return ResponseEntity.ok(ApiResponse.success("Interview feedback updated successfully", interview));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== CANDIDATE MANAGEMENT ==========

    /**
     * Convert candidate to employee
     */
    @PostMapping("/applications/{id}/hire")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Hire candidate", description = "Convert successful candidate to employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hireCandidate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> employeeDetails) {
        try {
            Map<String, Object> result = recruitmentService.hireCandidate(id, employeeDetails);
            return ResponseEntity.ok(ApiResponse.success("Candidate hired successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get recruitment statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get recruitment statistics", description = "Get comprehensive recruitment statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecruitmentStatistics(
            @Parameter(description = "Year filter") @RequestParam(required = false) Integer year,
            @Parameter(description = "Department ID filter") @RequestParam(required = false) Long departmentId) {
        
        Map<String, Object> statistics = recruitmentService.getRecruitmentStatistics(year, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Recruitment statistics retrieved successfully", statistics));
    }

    /**
     * Get recruitment pipeline
     */
    @GetMapping("/pipeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'RECRUITER')")
    @Operation(summary = "Get recruitment pipeline", description = "Get recruitment pipeline overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecruitmentPipeline() {
        Map<String, Object> pipeline = recruitmentService.getRecruitmentPipeline();
        return ResponseEntity.ok(ApiResponse.success("Recruitment pipeline retrieved successfully", pipeline));
    }
}

