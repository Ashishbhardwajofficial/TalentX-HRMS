package com.talentx.hrms.entity.payroll;

import com.talentx.hrms.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_items")
public class PayrollItem extends BaseEntity {

    @NotNull(message = "Payslip is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;

    @NotBlank(message = "Item type is required")
    @Size(max = 50, message = "Item type must not exceed 50 characters")
    @Column(name = "item_type", nullable = false)
    private String itemType; // EARNING, DEDUCTION, TAX

    @NotBlank(message = "Item code is required")
    @Size(max = 50, message = "Item code must not exceed 50 characters")
    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @NotBlank(message = "Item name is required")
    @Size(max = 255, message = "Item name must not exceed 255 characters")
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "rate", precision = 10, scale = 4)
    private BigDecimal rate;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "is_taxable")
    private Boolean isTaxable = true;

    @Column(name = "is_statutory")
    private Boolean isStatutory = false;

    @Column(name = "calculation_order")
    private Integer calculationOrder = 0;

    @Size(max = 100, message = "Unit must not exceed 100 characters")
    @Column(name = "unit")
    private String unit;

    // Constructors
    public PayrollItem() {}

    public PayrollItem(Payslip payslip, String itemType, String itemCode, String itemName, BigDecimal amount) {
        this.payslip = payslip;
        this.itemType = itemType;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.amount = amount;
    }

    // Getters and Setters
    public Payslip getPayslip() {
        return payslip;
    }

    public void setPayslip(Payslip payslip) {
        this.payslip = payslip;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Boolean getIsTaxable() {
        return isTaxable;
    }

    public void setIsTaxable(Boolean isTaxable) {
        this.isTaxable = isTaxable;
    }

    public Boolean getIsStatutory() {
        return isStatutory;
    }

    public void setIsStatutory(Boolean isStatutory) {
        this.isStatutory = isStatutory;
    }

    public Integer getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(Integer calculationOrder) {
        this.calculationOrder = calculationOrder;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Helper methods
    public boolean isEarning() {
        return "EARNING".equalsIgnoreCase(itemType);
    }

    public boolean isDeduction() {
        return "DEDUCTION".equalsIgnoreCase(itemType);
    }

    public boolean isTax() {
        return "TAX".equalsIgnoreCase(itemType);
    }

    public BigDecimal calculateAmount() {
        if (rate != null && quantity != null) {
            return rate.multiply(quantity);
        }
        return amount != null ? amount : BigDecimal.ZERO;
    }
}

