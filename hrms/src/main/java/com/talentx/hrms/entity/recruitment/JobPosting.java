package com.talentx.hrms.entity.recruitment;

import com.talentx.hrms.common.BaseEntity;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Location;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
public class JobPosting extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "job_level")
    private String jobLevel;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;

    @Column(name = "openings")
    private Integer openings = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobPostingStatus status = JobPostingStatus.DRAFT;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id")
    private Employee hiringManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private Employee recruiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdByEmployee;

    // Additional fields expected by service layer (transient - not in database yet)
    @Transient
    private String jobCode;

    @Transient
    private String qualifications;

    @Transient
    private String skills;

    @Transient
    private Boolean isSalaryNegotiable;

    @Transient
    private Integer minExperienceYears;

    @Transient
    private Integer maxExperienceYears;

    @Transient
    private String educationLevel;

    @Transient
    private Integer positionsAvailable;

    @Transient
    private LocalDate applicationDeadline;

    @Transient
    private LocalDate expectedStartDate;

    @Transient
    private Boolean isInternalOnly;

    @Transient
    private Boolean isRemoteWork;

    @Transient
    private Boolean isUrgent;

    @Transient
    private String benefits;

    @Transient
    private String notes;

    @Transient
    private Integer positionsFilled = 0;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications = new ArrayList<>();

    public enum JobPostingStatus {
        DRAFT, OPEN, CLOSED, ON_HOLD, CANCELLED
    }

    // Constructors
    public JobPosting() {}

    public JobPosting(String title, EmploymentType employmentType, Organization organization) {
        this.title = title;
        this.employmentType = employmentType;
        this.organization = organization;
    }

    // Getter aliases for service layer compatibility
    public String getJobTitle() {
        return title;
    }

    public void setJobTitle(String jobTitle) {
        this.title = jobTitle;
    }

    public String getJobDescription() {
        return description;
    }

    public void setJobDescription(String jobDescription) {
        this.description = jobDescription;
    }

    public BigDecimal getMinSalary() {
        return salaryMin;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.salaryMin = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return salaryMax;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.salaryMax = maxSalary;
    }

    public Integer getPositionsAvailable() {
        return positionsAvailable != null ? positionsAvailable : openings;
    }

    public void setPositionsAvailable(Integer positionsAvailable) {
        this.positionsAvailable = positionsAvailable;
        if (this.openings == null || this.openings == 1) {
            this.openings = positionsAvailable;
        }
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline != null ? applicationDeadline : closingDate;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
        if (this.closingDate == null) {
            this.closingDate = applicationDeadline;
        }
    }

    public Boolean getIsInternalOnly() {
        return isInternalOnly != null ? isInternalOnly : false;
    }

    public Boolean getIsRemoteWork() {
        return isRemoteWork != null ? isRemoteWork : false;
    }

    public Boolean getIsUrgent() {
        return isUrgent != null ? isUrgent : false;
    }

    public Boolean getIsSalaryNegotiable() {
        return isSalaryNegotiable != null ? isSalaryNegotiable : false;
    }

    // Business methods
    public boolean isActive() {
        return status == JobPostingStatus.OPEN;
    }

    public boolean isExpired() {
        LocalDate deadline = getApplicationDeadline();
        return deadline != null && deadline.isBefore(LocalDate.now());
    }

    public boolean hasOpenPositions() {
        Integer available = getPositionsAvailable();
        return available != null && positionsFilled != null && available > positionsFilled;
    }

    // Status management with String compatibility
    public void setStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.status = JobPostingStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                // Map common string values to enum
                switch (statusStr.toUpperCase()) {
                    case "ACTIVE":
                        this.status = JobPostingStatus.OPEN;
                        break;
                    case "CLOSED":
                        this.status = JobPostingStatus.CLOSED;
                        break;
                    case "DRAFT":
                        this.status = JobPostingStatus.DRAFT;
                        break;
                    default:
                        this.status = JobPostingStatus.DRAFT;
                }
            }
        }
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }
}

