package com.talentx.hrms.entity.recruitment;

import com.talentx.hrms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_posting_id", "candidate_id"})
})
@Getter
@Setter
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "stage", length = 100)
    private String stage;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejection_date")
    private LocalDate rejectionDate;

    @Column(name = "offer_amount", precision = 15, scale = 2)
    private BigDecimal offerAmount;

    @Column(name = "offer_currency", length = 3)
    private String offerCurrency;

    @Column(name = "offer_date")
    private LocalDate offerDate;

    @Column(name = "offer_accepted")
    private Boolean offerAccepted;

    // Additional fields expected by service layer (transient - not in database yet)
    @Transient
    private String resumePath;

    @Transient
    private String portfolioPath;

    @Transient
    private BigDecimal expectedSalary;

    @Transient
    private String salaryCurrency;

    @Transient
    private Integer noticePeriodDays;

    @Transient
    private Boolean isAvailableImmediately;

    @Transient
    private LocalDate earliestStartDate;

    @Transient
    private Boolean isWillingToRelocate;

    @Transient
    private Boolean isOpenToRemote;

    @Transient
    private String additionalNotes;

    @Transient
    private String internalNotes;

    @Transient
    private String currentStage;

    @Transient
    private String screenedBy;

    @Transient
    private Integer screeningScore;

    @Transient
    private String screeningNotes;

    @Transient
    private java.time.Instant offerAcceptedAt;

    @Transient
    private String withdrawalReason;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Interview> interviews = new ArrayList<>();

    public enum ApplicationStatus {
        APPLIED, SCREENING, INTERVIEW, ASSESSMENT, OFFER, REJECTED, WITHDRAWN, HIRED
    }

    // Constructors
    public Application() {}

    public Application(JobPosting jobPosting, Candidate candidate, LocalDate appliedDate) {
        this.jobPosting = jobPosting;
        this.candidate = candidate;
        this.appliedDate = appliedDate;
    }

    // Business methods
    public void setApplicationDate(LocalDate applicationDate) {
        this.appliedDate = applicationDate;
    }

    public LocalDate getApplicationDate() {
        return appliedDate;
    }

    public void moveToScreening(String screenedBy, Integer score, String notes) {
        this.status = ApplicationStatus.SCREENING;
        this.screenedBy = screenedBy;
        this.screeningScore = score;
        this.screeningNotes = notes;
        this.currentStage = "SCREENING";
    }

    public void reject(String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectionDate = LocalDate.now();
    }

    public void withdraw(String reason) {
        this.status = ApplicationStatus.WITHDRAWN;
        this.withdrawalReason = reason;
    }

    public void scheduleInterview() {
        this.status = ApplicationStatus.INTERVIEW;
        this.currentStage = "INTERVIEW";
    }

    public void extendOffer(BigDecimal amount) {
        this.status = ApplicationStatus.OFFER;
        this.offerAmount = amount;
        this.offerDate = LocalDate.now();
        this.currentStage = "OFFER";
    }

    public void acceptOffer() {
        this.offerAccepted = true;
        this.offerAcceptedAt = java.time.Instant.now();
    }

    public void rejectOffer(String reason) {
        this.offerAccepted = false;
        this.rejectionReason = reason;
        this.status = ApplicationStatus.REJECTED;
    }

    public void hire() {
        this.status = ApplicationStatus.HIRED;
        this.currentStage = "HIRED";
    }

    // Status management with String compatibility
    public void setStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.status = ApplicationStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                this.status = ApplicationStatus.APPLIED;
            }
        }
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }
}

