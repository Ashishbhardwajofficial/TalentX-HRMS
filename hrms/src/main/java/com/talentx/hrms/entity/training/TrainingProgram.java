package com.talentx.hrms.entity.training;

import com.talentx.hrms.entity.core.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "training_programs")
@Getter
@Setter
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type")
    private TrainingType trainingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    private DeliveryMethod deliveryMethod;

    @Column(name = "duration_hours", precision = 6, scale = 2)
    private BigDecimal durationHours;

    @Column(name = "cost_per_participant", precision = 10, scale = 2)
    private BigDecimal costPerParticipant;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "provider")
    private String provider;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum TrainingType {
        ONBOARDING, COMPLIANCE, TECHNICAL, SOFT_SKILLS, LEADERSHIP, SAFETY
    }

    public enum DeliveryMethod {
        IN_PERSON, ONLINE, HYBRID, SELF_PACED
    }

    // Constructors
    public TrainingProgram() {
    }

    public TrainingProgram(Organization organization, String title, TrainingType trainingType,
            DeliveryMethod deliveryMethod, Boolean isMandatory) {
        this.organization = organization;
        this.title = title;
        this.trainingType = trainingType;
        this.deliveryMethod = deliveryMethod;
        this.isMandatory = isMandatory;
    }

    // Business logic methods
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}

