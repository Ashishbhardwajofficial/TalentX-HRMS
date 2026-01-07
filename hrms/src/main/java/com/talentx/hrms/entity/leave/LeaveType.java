package com.talentx.hrms.entity.leave;

import com.talentx.hrms.common.BaseEntity;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.enums.LeaveTypeCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leave_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "organization_id", "code" })
})
@Getter
@Setter
public class LeaveType extends BaseEntity {

    @NotNull(message = "Organization is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotBlank(message = "Leave type name is required")
    @Size(max = 100, message = "Leave type name must not exceed 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private LeaveTypeCategory category;

    @Column(name = "is_paid")
    private Boolean isPaid = true;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    @Column(name = "is_carry_forward")
    private Boolean isCarryForward = false;

    @Column(name = "max_carry_forward_days")
    private Integer maxCarryForwardDays = 0;

    @Column(name = "accrual_rate", precision = 5, scale = 2)
    private BigDecimal accrualRate;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = true;

    @Column(name = "allow_negative_balance")
    private Boolean allowNegativeBalance = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "leaveType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveBalance> leaveBalances = new ArrayList<>();

    @OneToMany(mappedBy = "leaveType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

    // Constructors
    public LeaveType() {
    }

    public LeaveType(String name, String code, Organization organization) {
        this.name = name;
        this.code = code;
        this.organization = organization;
    }

    // Business logic methods
    // Compatibility setters for tests
    public void setDefaultDays(Integer days) {
        this.maxDaysPerYear = days;
    }

    public void setCarryForward(Boolean carryForward) {
        this.isCarryForward = carryForward;
    }

    public void setMaxCarryForward(Integer maxDays) {
        this.maxCarryForwardDays = maxDays;
    }

    public boolean isApplicableToEmployee(String employmentType, boolean isPermanent) {
        // By default, all leave types are applicable to all employees
        // This can be overridden with more complex logic if needed
        return isActive != null && isActive;
    }

    public Integer getMinDaysNotice() {
        // Default to 0 if not specified
        return 0;
    }
}
