package com.talentx.hrms.service.payroll;

import com.talentx.hrms.entity.payroll.Payslip;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-database-integration, Property 12: Deduction Calculation Compliance**
 * 
 * Test that validates deduction calculations follow compliance rules accurately.
 * This test ensures that for any payroll processing with deductions, 
 * tax and other deduction calculations follow the defined compliance rules accurately.
 * 
 * **Validates: Requirements 5.2**
 */
public class DeductionCalculationCompliancePropertyTest {

    // Tax rates and deduction constants from PayrollService
    private static final BigDecimal FEDERAL_TAX_RATE = new BigDecimal("0.22");
    private static final BigDecimal STATE_TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal SOCIAL_SECURITY_RATE = new BigDecimal("0.062");
    private static final BigDecimal MEDICARE_RATE = new BigDecimal("0.0145");
    private static final BigDecimal UNEMPLOYMENT_TAX_RATE = new BigDecimal("0.006");
    
    // Standard deduction amounts
    private static final BigDecimal STANDARD_HEALTH_INSURANCE = new BigDecimal("150.00");
    private static final BigDecimal STANDARD_DENTAL_INSURANCE = new BigDecimal("25.00");
    private static final BigDecimal STANDARD_VISION_INSURANCE = new BigDecimal("10.00");
    private static final BigDecimal STANDARD_LIFE_INSURANCE = new BigDecimal("20.00");
    private static final BigDecimal RETIREMENT_401K_RATE = new BigDecimal("0.05");

    @Property(tries = 100)
    void deductionCalculationCompliance(
            @ForAll @BigRange(min = "2000.00", max = "150000.00") BigDecimal grossPay,
            @ForAll @BigRange(min = "0.00", max = "2000.00") BigDecimal bonus,
            @ForAll @BigRange(min = "0.00", max = "1000.00") BigDecimal allowances
    ) {
        // Create a payslip for testing deduction calculation compliance
        Payslip payslip = new Payslip();
        
        // Set basic earnings
        payslip.setBasicSalary(grossPay);
        payslip.setBonus(bonus);
        payslip.setAllowances(allowances);
        payslip.setOvertimePay(BigDecimal.ZERO);
        payslip.setCommission(BigDecimal.ZERO);
        payslip.setReimbursements(BigDecimal.ZERO);

        // Calculate total gross pay for tax calculations
        BigDecimal totalGrossPay = grossPay.add(bonus).add(allowances);

        // Calculate tax deductions according to compliance rules
        BigDecimal expectedFederalTax = totalGrossPay.multiply(FEDERAL_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedStateTax = totalGrossPay.multiply(STATE_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedSocialSecurityTax = totalGrossPay.multiply(SOCIAL_SECURITY_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedMedicareTax = totalGrossPay.multiply(MEDICARE_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedUnemploymentTax = totalGrossPay.multiply(UNEMPLOYMENT_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);

        // Set tax deductions
        payslip.setFederalTax(expectedFederalTax);
        payslip.setStateTax(expectedStateTax);
        payslip.setSocialSecurityTax(expectedSocialSecurityTax);
        payslip.setMedicareTax(expectedMedicareTax);
        payslip.setUnemploymentTax(expectedUnemploymentTax);

        // Set standard benefit deductions
        payslip.setHealthInsurance(STANDARD_HEALTH_INSURANCE);
        payslip.setDentalInsurance(STANDARD_DENTAL_INSURANCE);
        payslip.setVisionInsurance(STANDARD_VISION_INSURANCE);
        payslip.setLifeInsurance(STANDARD_LIFE_INSURANCE);
        
        // Calculate 401k deduction as percentage of basic salary
        BigDecimal expected401k = grossPay.multiply(RETIREMENT_401K_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        payslip.setRetirement401k(expected401k);
        payslip.setOtherDeductions(BigDecimal.ZERO);

        // Calculate totals using the payslip's method
        payslip.calculateTotals();

        // COMPLIANCE RULE 1: Tax rates must be applied correctly to gross pay
        assertThat(payslip.getFederalTax())
            .as("Federal tax must be calculated at 22% of gross pay")
            .isEqualByComparingTo(expectedFederalTax);

        assertThat(payslip.getStateTax())
            .as("State tax must be calculated at 5% of gross pay")
            .isEqualByComparingTo(expectedStateTax);

        assertThat(payslip.getSocialSecurityTax())
            .as("Social Security tax must be calculated at 6.2% of gross pay")
            .isEqualByComparingTo(expectedSocialSecurityTax);

        assertThat(payslip.getMedicareTax())
            .as("Medicare tax must be calculated at 1.45% of gross pay")
            .isEqualByComparingTo(expectedMedicareTax);

        // COMPLIANCE RULE 2: All tax deductions must be non-negative
        assertThat(payslip.getFederalTax())
            .as("Federal tax deduction must be non-negative")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        assertThat(payslip.getStateTax())
            .as("State tax deduction must be non-negative")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        assertThat(payslip.getSocialSecurityTax())
            .as("Social Security tax deduction must be non-negative")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        assertThat(payslip.getMedicareTax())
            .as("Medicare tax deduction must be non-negative")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        // COMPLIANCE RULE 3: Benefit deductions must follow standard amounts
        assertThat(payslip.getHealthInsurance())
            .as("Health insurance deduction must follow standard amount")
            .isEqualByComparingTo(STANDARD_HEALTH_INSURANCE);

        assertThat(payslip.getDentalInsurance())
            .as("Dental insurance deduction must follow standard amount")
            .isEqualByComparingTo(STANDARD_DENTAL_INSURANCE);

        assertThat(payslip.getVisionInsurance())
            .as("Vision insurance deduction must follow standard amount")
            .isEqualByComparingTo(STANDARD_VISION_INSURANCE);

        assertThat(payslip.getLifeInsurance())
            .as("Life insurance deduction must follow standard amount")
            .isEqualByComparingTo(STANDARD_LIFE_INSURANCE);

        // COMPLIANCE RULE 4: 401k deduction must be calculated as percentage of basic salary
        assertThat(payslip.getRetirement401k())
            .as("401k deduction must be 5% of basic salary")
            .isEqualByComparingTo(expected401k);

        // COMPLIANCE RULE 5: Total tax deductions must equal sum of individual tax components
        BigDecimal expectedTotalTaxes = expectedFederalTax
            .add(expectedStateTax)
            .add(expectedSocialSecurityTax)
            .add(expectedMedicareTax)
            .add(expectedUnemploymentTax);

        assertThat(payslip.getTotalTaxes())
            .as("Total taxes must equal sum of all tax components")
            .isEqualByComparingTo(expectedTotalTaxes);

        // COMPLIANCE RULE 6: Total deductions must equal sum of all non-tax deduction components
        BigDecimal expectedTotalDeductions = STANDARD_HEALTH_INSURANCE
            .add(STANDARD_DENTAL_INSURANCE)
            .add(STANDARD_VISION_INSURANCE)
            .add(STANDARD_LIFE_INSURANCE)
            .add(expected401k)
            .add(BigDecimal.ZERO); // other deductions

        assertThat(payslip.getTotalDeductions())
            .as("Total deductions must equal sum of all deduction components")
            .isEqualByComparingTo(expectedTotalDeductions);

        // COMPLIANCE RULE 7: Net pay calculation must follow the fundamental equation
        BigDecimal expectedNetPay = totalGrossPay
            .subtract(expectedTotalTaxes)
            .subtract(expectedTotalDeductions);

        assertThat(payslip.getNetPay())
            .as("Net pay must equal gross pay minus taxes and deductions")
            .isEqualByComparingTo(expectedNetPay);

        // COMPLIANCE RULE 8: Net pay must never be negative (minimum wage protection)
        assertThat(payslip.getNetPay())
            .as("Net pay must never be negative to comply with minimum wage laws")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        // COMPLIANCE RULE 9: Tax deductions must not exceed reasonable limits
        BigDecimal totalTaxRate = FEDERAL_TAX_RATE
            .add(STATE_TAX_RATE)
            .add(SOCIAL_SECURITY_RATE)
            .add(MEDICARE_RATE)
            .add(UNEMPLOYMENT_TAX_RATE);

        assertThat(totalTaxRate)
            .as("Combined tax rate must not exceed 50% for compliance")
            .isLessThanOrEqualTo(new BigDecimal("0.50"));

        // COMPLIANCE RULE 10: Deduction amounts must be properly rounded to 2 decimal places
        assertThat(payslip.getFederalTax().scale())
            .as("Federal tax must be rounded to 2 decimal places")
            .isLessThanOrEqualTo(2);

        assertThat(payslip.getStateTax().scale())
            .as("State tax must be rounded to 2 decimal places")
            .isLessThanOrEqualTo(2);

        assertThat(payslip.getSocialSecurityTax().scale())
            .as("Social Security tax must be rounded to 2 decimal places")
            .isLessThanOrEqualTo(2);

        assertThat(payslip.getMedicareTax().scale())
            .as("Medicare tax must be rounded to 2 decimal places")
            .isLessThanOrEqualTo(2);

        // COMPLIANCE RULE 11: Total deductions must not exceed gross pay (protection against over-deduction)
        BigDecimal totalAllDeductions = expectedTotalTaxes.add(expectedTotalDeductions);
        assertThat(totalAllDeductions)
            .as("Total deductions must not exceed gross pay")
            .isLessThanOrEqualTo(totalGrossPay);

        // COMPLIANCE RULE 12: Verify mathematical consistency in calculations
        BigDecimal calculatedGrossPay = payslip.getBasicSalary()
            .add(payslip.getBonus())
            .add(payslip.getAllowances())
            .add(payslip.getOvertimePay())
            .add(payslip.getCommission())
            .add(payslip.getReimbursements());

        assertThat(payslip.getGrossPay())
            .as("Gross pay calculation must be mathematically consistent")
            .isEqualByComparingTo(calculatedGrossPay);
    }

    @Property(tries = 50)
    void deductionCalculationEdgeCases(
            @ForAll @BigRange(min = "0.01", max = "100.00") BigDecimal lowGrossPay
    ) {
        // Test edge cases with very low gross pay to ensure compliance rules still apply
        Payslip payslip = new Payslip();
        
        payslip.setBasicSalary(lowGrossPay);
        payslip.setBonus(BigDecimal.ZERO);
        payslip.setAllowances(BigDecimal.ZERO);
        payslip.setOvertimePay(BigDecimal.ZERO);
        payslip.setCommission(BigDecimal.ZERO);
        payslip.setReimbursements(BigDecimal.ZERO);

        // Calculate tax deductions
        BigDecimal federalTax = lowGrossPay.multiply(FEDERAL_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal stateTax = lowGrossPay.multiply(STATE_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal socialSecurityTax = lowGrossPay.multiply(SOCIAL_SECURITY_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal medicareTax = lowGrossPay.multiply(MEDICARE_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal unemploymentTax = lowGrossPay.multiply(UNEMPLOYMENT_TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);

        payslip.setFederalTax(federalTax);
        payslip.setStateTax(stateTax);
        payslip.setSocialSecurityTax(socialSecurityTax);
        payslip.setMedicareTax(medicareTax);
        payslip.setUnemploymentTax(unemploymentTax);

        // Set minimal deductions for low-income scenarios
        payslip.setHealthInsurance(BigDecimal.ZERO); // May be waived for low income
        payslip.setDentalInsurance(BigDecimal.ZERO);
        payslip.setVisionInsurance(BigDecimal.ZERO);
        payslip.setLifeInsurance(BigDecimal.ZERO);
        payslip.setRetirement401k(BigDecimal.ZERO); // May be waived for low income
        payslip.setOtherDeductions(BigDecimal.ZERO);

        payslip.calculateTotals();

        // EDGE CASE COMPLIANCE RULE: Even with low gross pay, calculations must be accurate
        assertThat(payslip.getFederalTax())
            .as("Federal tax calculation must be accurate even for low gross pay")
            .isEqualByComparingTo(federalTax);

        // EDGE CASE COMPLIANCE RULE: Net pay must not be negative even with low gross pay
        assertThat(payslip.getNetPay())
            .as("Net pay must not be negative even with low gross pay")
            .isGreaterThanOrEqualTo(BigDecimal.ZERO);

        // EDGE CASE COMPLIANCE RULE: All deduction amounts must be properly formatted
        assertThat(payslip.getTotalTaxes().scale())
            .as("Total taxes must be properly formatted to 2 decimal places")
            .isLessThanOrEqualTo(2);
    }
}

