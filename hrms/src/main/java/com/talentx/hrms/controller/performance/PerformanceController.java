package com.talentx.hrms.controller.performance;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.dto.performance.*;
import com.talentx.hrms.entity.performance.Goal;
import com.talentx.hrms.entity.performance.PerformanceReview;
import com.talentx.hrms.entity.performance.PerformanceReviewCycle;
import com.talentx.hrms.mapper.PerformanceMapper;
import com.talentx.hrms.service.performance.PerformanceService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performance")
@Tag(name = "Performance Management", description = "Performance review cycles, reviews, and goals management")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final PerformanceMapper performanceMapper;

    @Autowired
    public PerformanceController(PerformanceService performanceService, PerformanceMapper performanceMapper) {
        this.performanceService = performanceService;
        this.performanceMapper = performanceMapper;
    }

    // ===== PERFORMANCE REVIEW CYCLE ENDPOINTS =====

    /**
     * Create a new performance review cycle
     */
    @PostMapping("/cycles")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Create review cycle", description = "Create a new performance review cycle")
    public ResponseEntity<ApiResponse<PerformanceReviewCycleResponse>> createReviewCycle(
            @Valid @RequestBody PerformanceReviewCycleRequest request) {
        try {
            PerformanceReviewCycle cycle = performanceService.createReviewCycle(
                request.getOrganizationId(),
                request.getName(),
                request.getReviewType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getSelfReviewDeadline(),
                request.getManagerReviewDeadline()
            );

            PerformanceReviewCycleResponse response = performanceMapper.toResponse(cycle);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review cycle created successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create review cycle: " + e.getMessage()));
        }
    }

    /**
     * Get all review cycles for an organization
     */
    @GetMapping("/cycles")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Get review cycles", description = "Get all review cycles for an organization")
    public ResponseEntity<ApiResponse<Page<PerformanceReviewCycleResponse>>> getReviewCycles(
            @Parameter(description = "Organization ID") @RequestParam Long organizationId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        Page<PerformanceReviewCycle> cycles = performanceService.getReviewCycles(organizationId, paginationRequest);
        Page<PerformanceReviewCycleResponse> response = cycles.map(performanceMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success("Review cycles retrieved successfully", response));
    }

    /**
     * Get active review cycles for an organization
     */
    @GetMapping("/cycles/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Get active review cycles", description = "Get all active review cycles for an organization")
    public ResponseEntity<ApiResponse<List<PerformanceReviewCycleResponse>>> getActiveReviewCycles(
            @Parameter(description = "Organization ID") @RequestParam Long organizationId) {
        
        List<PerformanceReviewCycle> cycles = performanceService.getActiveReviewCycles(organizationId);
        List<PerformanceReviewCycleResponse> response = cycles.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Active review cycles retrieved successfully", response));
    }

    /**
     * Get review cycle by ID
     */
    @GetMapping("/cycles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Get review cycle", description = "Get a specific review cycle by ID")
    public ResponseEntity<ApiResponse<PerformanceReviewCycleResponse>> getReviewCycle(@PathVariable Long id) {
        try {
            PerformanceReviewCycle cycle = performanceService.getReviewCycle(id);
            PerformanceReviewCycleResponse response = performanceMapper.toResponse(cycle);
            return ResponseEntity.ok(ApiResponse.success("Review cycle retrieved successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a review cycle
     */
    @PutMapping("/cycles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update review cycle", description = "Update an existing review cycle")
    public ResponseEntity<ApiResponse<PerformanceReviewCycleResponse>> updateReviewCycle(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceReviewCycleRequest request) {
        try {
            PerformanceReviewCycle cycle = performanceService.updateReviewCycle(
                id,
                request.getName(),
                request.getReviewType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getSelfReviewDeadline(),
                request.getManagerReviewDeadline(),
                PerformanceReviewCycle.ReviewCycleStatus.DRAFT // Default status for updates
            );

            PerformanceReviewCycleResponse response = performanceMapper.toResponse(cycle);
            return ResponseEntity.ok(ApiResponse.success("Review cycle updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update review cycle: " + e.getMessage()));
        }
    }

    /**
     * Activate a review cycle
     */
    @PutMapping("/cycles/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Activate review cycle", description = "Activate a draft review cycle")
    public ResponseEntity<ApiResponse<PerformanceReviewCycleResponse>> activateReviewCycle(@PathVariable Long id) {
        try {
            PerformanceReviewCycle cycle = performanceService.activateReviewCycle(id);
            PerformanceReviewCycleResponse response = performanceMapper.toResponse(cycle);
            return ResponseEntity.ok(ApiResponse.success("Review cycle activated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to activate review cycle: " + e.getMessage()));
        }
    }

    /**
     * Complete a review cycle
     */
    @PutMapping("/cycles/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Complete review cycle", description = "Complete an active review cycle")
    public ResponseEntity<ApiResponse<PerformanceReviewCycleResponse>> completeReviewCycle(@PathVariable Long id) {
        try {
            PerformanceReviewCycle cycle = performanceService.completeReviewCycle(id);
            PerformanceReviewCycleResponse response = performanceMapper.toResponse(cycle);
            return ResponseEntity.ok(ApiResponse.success("Review cycle completed successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to complete review cycle: " + e.getMessage()));
        }
    }

    // ===== PERFORMANCE REVIEW ENDPOINTS =====

    /**
     * Create a performance review
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Create review", description = "Create a new performance review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> createPerformanceReview(
            @Valid @RequestBody PerformanceReviewRequest request) {
        try {
            PerformanceReview review = performanceService.createPerformanceReview(
                request.getReviewCycleId(),
                request.getEmployeeId(),
                request.getReviewerId(),
                request.getReviewType()
            );

            PerformanceReviewResponse response = performanceMapper.toResponse(review);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Performance review created successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create performance review: " + e.getMessage()));
        }
    }

    /**
     * Update performance review content
     */
    @PutMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @performanceService.isReviewOwner(#id, authentication.name)")
    @Operation(summary = "Update review", description = "Update performance review content")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> updatePerformanceReview(
            @PathVariable Long id,
            @Valid @RequestBody PerformanceReviewRequest request) {
        try {
            PerformanceReview review = performanceService.updatePerformanceReview(
                id,
                request.getOverallRating(),
                request.getStrengths(),
                request.getAreasForImprovement(),
                request.getAchievements(),
                request.getGoalsNextPeriod()
            );

            PerformanceReviewResponse response = performanceMapper.toResponse(review);
            return ResponseEntity.ok(ApiResponse.success("Performance review updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update performance review: " + e.getMessage()));
        }
    }

    /**
     * Submit a performance review
     */
    @PutMapping("/reviews/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @performanceService.isReviewOwner(#id, authentication.name)")
    @Operation(summary = "Submit review", description = "Submit a performance review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> submitPerformanceReview(@PathVariable Long id) {
        try {
            PerformanceReview review = performanceService.submitPerformanceReview(id);
            PerformanceReviewResponse response = performanceMapper.toResponse(review);
            return ResponseEntity.ok(ApiResponse.success("Performance review submitted successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to submit performance review: " + e.getMessage()));
        }
    }

    /**
     * Acknowledge a performance review
     */
    @PutMapping("/reviews/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Acknowledge review", description = "Acknowledge a submitted performance review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> acknowledgePerformanceReview(@PathVariable Long id) {
        try {
            PerformanceReview review = performanceService.acknowledgePerformanceReview(id);
            PerformanceReviewResponse response = performanceMapper.toResponse(review);
            return ResponseEntity.ok(ApiResponse.success("Performance review acknowledged successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to acknowledge performance review: " + e.getMessage()));
        }
    }

    /**
     * Get performance reviews for an employee
     */
    @GetMapping("/reviews/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get employee reviews", description = "Get all performance reviews for an employee")
    public ResponseEntity<ApiResponse<Page<PerformanceReviewResponse>>> getEmployeeReviews(
            @PathVariable Long employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        Page<PerformanceReview> reviews = performanceService.getEmployeeReviews(employeeId, paginationRequest);
        Page<PerformanceReviewResponse> response = reviews.map(performanceMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success("Employee reviews retrieved successfully", response));
    }

    /**
     * Get pending reviews for an employee
     */
    @GetMapping("/reviews/employee/{employeeId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get pending employee reviews", description = "Get pending performance reviews for an employee")
    public ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>> getPendingEmployeeReviews(
            @PathVariable Long employeeId) {
        
        List<PerformanceReview> reviews = performanceService.getPendingReviewsForEmployee(employeeId);
        List<PerformanceReviewResponse> response = reviews.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Pending employee reviews retrieved successfully", response));
    }

    /**
     * Get pending reviews for a reviewer
     */
    @GetMapping("/reviews/reviewer/{reviewerId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#reviewerId)")
    @Operation(summary = "Get pending reviewer reviews", description = "Get pending performance reviews for a reviewer")
    public ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>> getPendingReviewerReviews(
            @PathVariable Long reviewerId) {
        
        List<PerformanceReview> reviews = performanceService.getPendingReviewsForReviewer(reviewerId);
        List<PerformanceReviewResponse> response = reviews.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Pending reviewer reviews retrieved successfully", response));
    }

    // ===== GOAL ENDPOINTS =====

    /**
     * Create a goal
     */
    @PostMapping("/goals")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#request.employeeId)")
    @Operation(summary = "Create goal", description = "Create a new goal for an employee")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody GoalRequest request) {
        try {
            Goal goal = performanceService.createGoal(
                request.getEmployeeId(),
                request.getTitle(),
                request.getDescription(),
                request.getGoalType(),
                request.getCategory(),
                request.getStartDate(),
                request.getTargetDate(),
                request.getWeight(),
                request.getMeasurementCriteria(),
                request.getCreatedByEmployeeId()
            );

            GoalResponse response = performanceMapper.toResponse(goal);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to create goal: " + e.getMessage()));
        }
    }

    /**
     * Update goal progress
     */
    @PutMapping("/goals/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @performanceService.isGoalOwner(#id, authentication.name)")
    @Operation(summary = "Update goal progress", description = "Update goal progress and status")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoalProgress(
            @PathVariable Long id,
            @Valid @RequestBody GoalProgressUpdateRequest request) {
        try {
            Goal goal = performanceService.updateGoalProgress(
                id,
                request.getProgressPercentage(),
                request.getStatus()
            );

            GoalResponse response = performanceMapper.toResponse(goal);
            return ResponseEntity.ok(ApiResponse.success("Goal progress updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update goal progress: " + e.getMessage()));
        }
    }

    /**
     * Get goals for an employee
     */
    @GetMapping("/goals/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get employee goals", description = "Get all goals for an employee")
    public ResponseEntity<ApiResponse<Page<GoalResponse>>> getEmployeeGoals(
            @PathVariable Long employeeId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
        Page<Goal> goals = performanceService.getEmployeeGoals(employeeId, paginationRequest);
        Page<GoalResponse> response = goals.map(performanceMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success("Employee goals retrieved successfully", response));
    }

    /**
     * Get active goals for an employee
     */
    @GetMapping("/goals/employee/{employeeId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get active employee goals", description = "Get active goals for an employee")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getActiveEmployeeGoals(@PathVariable Long employeeId) {
        
        List<Goal> goals = performanceService.getActiveGoalsForEmployee(employeeId);
        List<GoalResponse> response = goals.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Active employee goals retrieved successfully", response));
    }

    /**
     * Get overdue goals for an employee
     */
    @GetMapping("/goals/employee/{employeeId}/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get overdue employee goals", description = "Get overdue goals for an employee")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getOverdueEmployeeGoals(@PathVariable Long employeeId) {
        
        List<Goal> goals = performanceService.getOverdueGoalsForEmployee(employeeId);
        List<GoalResponse> response = goals.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Overdue employee goals retrieved successfully", response));
    }

    /**
     * Get goals due soon for an employee
     */
    @GetMapping("/goals/employee/{employeeId}/due-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @employeeService.isCurrentUserOrManager(#employeeId)")
    @Operation(summary = "Get goals due soon", description = "Get goals due soon for an employee")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsDueSoon(
            @PathVariable Long employeeId,
            @Parameter(description = "Due date threshold") @RequestParam(required = false) LocalDate dueDate) {
        
        LocalDate dueDateThreshold = dueDate != null ? dueDate : LocalDate.now().plusDays(30);
        List<Goal> goals = performanceService.getGoalsDueSoonForEmployee(employeeId, dueDateThreshold);
        List<GoalResponse> response = goals.stream()
            .map(performanceMapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Goals due soon retrieved successfully", response));
    }

    /**
     * Get goal by ID
     */
    @GetMapping("/goals/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @performanceService.isGoalOwner(#id, authentication.name)")
    @Operation(summary = "Get goal", description = "Get a specific goal by ID")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoal(@PathVariable Long id) {
        try {
            Goal goal = performanceService.getGoal(id);
            GoalResponse response = performanceMapper.toResponse(goal);
            return ResponseEntity.ok(ApiResponse.success("Goal retrieved successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a goal
     */
    @DeleteMapping("/goals/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @performanceService.isGoalOwner(#id, authentication.name)")
    @Operation(summary = "Delete goal", description = "Delete a goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        try {
            performanceService.deleteGoal(id);
            return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete goal: " + e.getMessage()));
        }
    }
}

