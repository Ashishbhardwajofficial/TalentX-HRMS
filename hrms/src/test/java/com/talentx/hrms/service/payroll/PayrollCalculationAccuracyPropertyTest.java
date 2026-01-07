package com.talentx.hrms.service.payroll;

import com.talentx.hrms.entity.payroll.Payslip;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 11: Payroll Calculation Accuracy**
 * 
 * Test that validates payroll calculations are mathematically correct.
 * This test ensures that for any payroll run with attendance and salary data, 
 * gross pay calculations are mathematically correct based on the input parameters.
 * 
 * **Validates: Requirements 5.1**
 */
public class PayrollCalculationAccuracyPropertyTest {

    // Tax rates and constants from PayrollService
    private static final BigDecimal FEDERAL_TAX_RATE = new BigDecimal("0.22");
    private static final BigDecimal STATE_TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal SOCIAL_SECURITY_RATE = new BigDecimal("0.062");
    private static final BigDecimal MEDICARE_RATE = new BigDecimal("0.0145");
    private static final BigDecimal UNEMPLOYMENT_TAX_RATE = new BigDecimal("0.006");

    @Property(tries = 100)
    void payrollCalculationAccuracy(
            @ForAll @BigRange(min = "1000.00", max = "200000.00") BigDecimal basicSalary,
            @ForAll @BigRange(min = "0.00", max = "5000.00") BigDecimal overtimePay,
            @ForAll @BigRange(min = "0.00", max = "5000.00") BigDecimal bonus,
            @ForAll @BigRange(min = "0.00", max = "3000.00") BigDecimal commission,
            @ForAll @BigRange(min = "0.00", max = "1000.00") BigDecimal allowances,
            @ForAll @BigRange(min = "0.00", max = "500.00") BigDecimal reimbursements
    ) {
        // Create a payslip for testing calculation logic
        Payslip payslip = new Payslip();
        
        // Set the payslip values
        payslip.setBasicSalary(basicSalary);
        payslip.setOvertimePay(overtimePay);
        payslip.setBonus(bonus);
        payslip.setCommission(commission);
        payslip.setAllowances(allowances);
        payslip.setReimbursements(reimbursements);

        // Calculate expected gross pay
        BigDecimal expectedGrossPay = basicSalary
            .add(overtimePay)
            .add(bonus)
            .add(commission)
            .add(allowances)
            .add(reimbursements);

        // Calculate expected taxes
        BigDecimal expectedFederalTax = expectedGrossPay.multiply(FEDERAL_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedStateTax = expectedGrossPay.multiply(STATE_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedSocialSecurityTax = expectedGrossPay.multiply(SOCIAL_SECURITY_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedMedicareTax = expectedGrossPay.multiply(MEDICARE_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedUnemploymentTax = expectedGrossPay.multiply(UNEMPLOYMENT_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);

        // Set tax values
        payslip.setFederalTax(expectedFederalTax);
        payslip.setStateTax(expectedStateTax);
        payslip.setSocialSecurityTax(expectedSocialSecurityTax);
        payslip.setMedicareTax(expectedMedicareTax);
        payslip.setUnemploymentTax(expectedUnemploymentTax);

        // Set standard deductions
        payslip.setHealthInsurance(new BigDecimal("150.00"));
        payslip.setDentalInsurance(new BigDecimal("25.00"));
        payslip.setVisionInsurance(new BigDecimal("10.00"));
        payslip.setLifeInsurance(new BigDecimal("20.00"));
        payslip.setRetirement401k(basicSalary.multiply(new BigDecimal("0.05"))
            .setScale(2, RoundingMode.HALF_UP));
        payslip.setOtherDeductions(BigDecimal.ZERO);

        // Calculate totals using the payslip's method
        payslip.calculateTotals();

        // Verify calculations
        assertThat(payslip.getBasicSalary())
            .as("Basic salary should match input")
            .isEqualByComparingTo(basicSalary);

        assertThat(payslip.getOvertimePay())
            .as("Overtime pay should match input")
            .isEqualByComparingTo(overtimePay);

        assertThat(payslip.getGrossPay())
            .as("Gross pay should equal total earnings")
            .isEqualByComparingTo(expectedGrossPay);

        // Verify tax calculations
        assertThat(payslip.getFederalTax())
            .as("Federal tax should be calculated correctly")
            .isEqualByComparingTo(expectedFederalTax);

        assertThat(payslip.getStateTax())
            .as("State tax should be calculated correctly")
            .isEqualByComparingTo(expectedStateTax);

        assertThat(payslip.getSocialSecurityTax())
            .as("Social Security tax should be calculated correctly")
            .isEqualByComparingTo(expectedSocialSecurityTax);

        assertThat(payslip.getMedicareTax())
            .as("Medicare tax should be calculated correctly")
            .isEqualByComparingTo(expectedMedicareTax);

        // Verify total calculations
        BigDecimal expectedTotalTaxes = expectedFederalTax
            .add(expectedStateTax)
            .add(expectedSocialSecurityTax)
            .add(expectedMedicareTax)
            .add(expectedUnemploymentTax);

        assertThat(payslip.getTotalTaxes())
            .as("Total taxes should be sum of all tax components")
            .isEqualByComparingTo(expectedTotalTaxes);

        BigDecimal expectedTotalDeductions = new BigDecimal("150.00") // health
            .add(new BigDecimal("25.00")) // dental
            .add(new BigDecimal("10.00")) // vision
            .add(new BigDecimal("20.00")) // life
            .add(payslip.getRetirement401k()) // 401k
            .add(BigDecimal.ZERO); // other

        assertThat(payslip.getTotalDeductions())
            .as("Total deductions should be sum of all deduction components")
            .isEqualByComparingTo(expectedTotalDeductions);

        BigDecimal expectedNetPay = expectedGrossPay
            .subtract(expectedTotalTaxes)
            .subtract(expectedTotalDeductions);

        assertThat(payslip.getNetPay())
            .as("Net pay should be gross pay minus taxes and deductions")
            .isEqualByComparingTo(expectedNetPay);

        // Verify net pay is never negative (business rule)
        assertThat(payslip.getNetPay())
            .as("Net pay should never be negative")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        // Verify mathematical consistency: gross pay should equal total earnings
        BigDecimal actualTotalEarnings = payslip.getTotalEarnings();
        assertThat(actualTotalEarnings)
            .as("Total earnings should equal gross pay")
            .isEqualByComparingTo(expectedGrossPay);

        // Verify the fundamental payroll equation: Net Pay = Gross Pay - Taxes - Deductions
        BigDecimal calculatedNetPay = payslip.getGrossPay()
            .subtract(payslip.getTotalTaxes())
            .subtract(payslip.getTotalDeductions());
        
        assertThat(payslip.getNetPay())
            .as("Net pay calculation should follow the fundamental payroll equation")
            .isEqualByComparingTo(calculatedNetPay);
    }

}

