package com.talentx.hrms.entity.enums;

public enum BenefitPlanType {
    HEALTH_INSURANCE("Health Insurance"),
    DENTAL_INSURANCE("Dental Insurance"),
    VISION_INSURANCE("Vision Insurance"),
    LIFE_INSURANCE("Life Insurance"),
    DISABILITY_INSURANCE("Disability Insurance"),
    RETIREMENT_401K("401(k) Retirement Plan"),
    PENSION("Pension Plan"),
    FLEXIBLE_SPENDING("Flexible Spending Account"),
    HEALTH_SAVINGS("Health Savings Account"),
    PAID_TIME_OFF("Paid Time Off"),
    WELLNESS_PROGRAM("Wellness Program"),
    EMPLOYEE_ASSISTANCE("Employee Assistance Program"),
    TRANSPORTATION("Transportation Benefits"),
    MEAL_ALLOWANCE("Meal Allowance"),
    EDUCATION_ASSISTANCE("Education Assistance"),
    CHILDCARE("Childcare Benefits"),
    OTHER("Other");

    private final String displayName;

    BenefitPlanType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

