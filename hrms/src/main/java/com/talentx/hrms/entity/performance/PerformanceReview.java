package com.talentx.hrms.entity.performance;

import com.talentx.hrms.common.BaseEntity;
import com.talentx.hrms.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Getter
@Setter
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_cycle_id", nullable = false)
    private PerformanceReviewCycle reviewCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(name = "overall_rating", precision = 3, scale = 2)
    private BigDecimal overallRating;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "goals_next_period", columnDefinition = "TEXT")
    private String goalsNextPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.NOT_STARTED;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    public enum ReviewType {
        SELF, MANAGER, PEER, THREE_SIXTY
    }

    public enum ReviewStatus {
        NOT_STARTED, IN_PROGRESS, SUBMITTED, ACKNOWLEDGED
    }

    // Constructors
    public PerformanceReview() {}

    public PerformanceReview(PerformanceReviewCycle reviewCycle, Employee employee, 
                           Employee reviewer, ReviewType reviewType) {
        this.reviewCycle = reviewCycle;
        this.employee = employee;
        this.reviewer = reviewer;
        this.reviewType = reviewType;
    }
}

