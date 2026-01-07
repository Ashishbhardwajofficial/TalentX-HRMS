package com.talentx.hrms.entity;

import com.talentx.hrms.entity.attendance.EmployeeShift;
import com.talentx.hrms.entity.attendance.Shift;
import com.talentx.hrms.entity.employee.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmployeeShift entity accessor methods.
 * Validates: Requirements 1.2
 */
@DisplayName("EmployeeShift Entity Tests")
class EmployeeShiftEntityTest {

    private EmployeeShift employeeShift;
    private Employee employee;
    private Shift shift;

    @BeforeEach
    void setUp() {
        employeeShift = new EmployeeShift();
        employee = new Employee();
        employee.setId(1L);
        shift = new Shift();
        shift.setId(1L);
    }

    @Test
    @DisplayName("Should set and get effectiveDate")
    void testEffectiveDateAccessors() {
        LocalDate effectiveDate = LocalDate.of(2026, 1, 1);
        employeeShift.setEffectiveDate(effectiveDate);
        assertThat(employeeShift.getEffectiveDate()).isEqualTo(effectiveDate);
        // Verify it syncs with effectiveFrom
        assertThat(employeeShift.getEffectiveFrom()).isEqualTo(effectiveDate);
    }

    @Test
    @DisplayName("Should set and get endDate")
    void testEndDateAccessors() {
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        employeeShift.setEndDate(endDate);
        assertThat(employeeShift.getEndDate()).isEqualTo(endDate);
        // Verify it syncs with effectiveTo
        assertThat(employeeShift.getEffectiveTo()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("Should set and get isCurrent")
    void testIsCurrentAccessors() {
        Boolean isCurrent = true;
        employeeShift.setIsCurrent(isCurrent);
        assertThat(employeeShift.getIsCurrent()).isEqualTo(isCurrent);
        // Verify it syncs with isActive
        assertThat(employeeShift.getIsActive()).isEqualTo(isCurrent);
    }

    @Test
    @DisplayName("Should set and get effectiveFrom")
    void testEffectiveFromAccessors() {
        LocalDate effectiveFrom = LocalDate.of(2026, 1, 1);
        employeeShift.setEffectiveFrom(effectiveFrom);
        assertThat(employeeShift.getEffectiveFrom()).isEqualTo(effectiveFrom);
    }

    @Test
    @DisplayName("Should set and get effectiveTo")
    void testEffectiveToAccessors() {
        LocalDate effectiveTo = LocalDate.of(2026, 12, 31);
        employeeShift.setEffectiveTo(effectiveTo);
        assertThat(employeeShift.getEffectiveTo()).isEqualTo(effectiveTo);
    }

    @Test
    @DisplayName("Should set and get isActive")
    void testIsActiveAccessors() {
        Boolean isActive = true;
        employeeShift.setIsActive(isActive);
        assertThat(employeeShift.getIsActive()).isEqualTo(isActive);
    }

    @Test
    @DisplayName("Should set and get employee")
    void testEmployeeAccessors() {
        employeeShift.setEmployee(employee);
        assertThat(employeeShift.getEmployee()).isEqualTo(employee);
    }

    @Test
    @DisplayName("Should set and get shift")
    void testShiftAccessors() {
        employeeShift.setShift(shift);
        assertThat(employeeShift.getShift()).isEqualTo(shift);
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void testCreatedAtAccessors() {
        Instant createdAt = Instant.now();
        employeeShift.setCreatedAt(createdAt);
        assertThat(employeeShift.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should set and get updatedAt")
    void testUpdatedAtAccessors() {
        Instant updatedAt = Instant.now();
        employeeShift.setUpdatedAt(updatedAt);
        assertThat(employeeShift.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should properly store and retrieve all fields together")
    void testAllFieldsAccessors() {
        // Arrange
        LocalDate effectiveDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Boolean isCurrent = true;
        Instant now = Instant.now();

        // Act
        employeeShift.setEmployee(employee);
        employeeShift.setShift(shift);
        employeeShift.setEffectiveDate(effectiveDate);
        employeeShift.setEndDate(endDate);
        employeeShift.setIsCurrent(isCurrent);
        employeeShift.setCreatedAt(now);
        employeeShift.setUpdatedAt(now);

        // Assert
        assertThat(employeeShift.getEmployee()).isEqualTo(employee);
        assertThat(employeeShift.getShift()).isEqualTo(shift);
        assertThat(employeeShift.getEffectiveDate()).isEqualTo(effectiveDate);
        assertThat(employeeShift.getEndDate()).isEqualTo(endDate);
        assertThat(employeeShift.getIsCurrent()).isEqualTo(isCurrent);
        assertThat(employeeShift.getCreatedAt()).isEqualTo(now);
        assertThat(employeeShift.getUpdatedAt()).isEqualTo(now);
        
        // Verify sync with persistent fields
        assertThat(employeeShift.getEffectiveFrom()).isEqualTo(effectiveDate);
        assertThat(employeeShift.getEffectiveTo()).isEqualTo(endDate);
        assertThat(employeeShift.getIsActive()).isEqualTo(isCurrent);
    }

    @Test
    @DisplayName("Should handle null values properly")
    void testNullValues() {
        employeeShift.setEmployee(null);
        employeeShift.setShift(null);
        employeeShift.setEndDate(null);
        employeeShift.setIsCurrent(null);
        employeeShift.setCreatedAt(null);
        employeeShift.setUpdatedAt(null);

        assertThat(employeeShift.getEmployee()).isNull();
        assertThat(employeeShift.getShift()).isNull();
        assertThat(employeeShift.getEndDate()).isNull();
        assertThat(employeeShift.getIsCurrent()).isNull();
        assertThat(employeeShift.getCreatedAt()).isNull();
        assertThat(employeeShift.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should use constructor properly")
    void testConstructor() {
        LocalDate effectiveFrom = LocalDate.of(2026, 1, 1);

        EmployeeShift constructedEmployeeShift = new EmployeeShift(employee, shift, effectiveFrom);

        assertThat(constructedEmployeeShift.getEmployee()).isEqualTo(employee);
        assertThat(constructedEmployeeShift.getShift()).isEqualTo(shift);
        assertThat(constructedEmployeeShift.getEffectiveFrom()).isEqualTo(effectiveFrom);
        assertThat(constructedEmployeeShift.getEffectiveDate()).isEqualTo(effectiveFrom);
    }

    @Test
    @DisplayName("Should sync transient fields when setting effectiveDate")
    void testEffectiveDateSyncsWithEffectiveFrom() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        employeeShift.setEffectiveDate(date);
        
        assertThat(employeeShift.getEffectiveDate()).isEqualTo(date);
        assertThat(employeeShift.getEffectiveFrom()).isEqualTo(date);
    }

    @Test
    @DisplayName("Should sync transient fields when setting endDate")
    void testEndDateSyncsWithEffectiveTo() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        employeeShift.setEndDate(date);
        
        assertThat(employeeShift.getEndDate()).isEqualTo(date);
        assertThat(employeeShift.getEffectiveTo()).isEqualTo(date);
    }

    @Test
    @DisplayName("Should sync transient fields when setting isCurrent")
    void testIsCurrentSyncsWithIsActive() {
        employeeShift.setIsCurrent(true);
        
        assertThat(employeeShift.getIsCurrent()).isTrue();
        assertThat(employeeShift.getIsActive()).isTrue();
        
        employeeShift.setIsCurrent(false);
        
        assertThat(employeeShift.getIsCurrent()).isFalse();
        assertThat(employeeShift.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should correctly determine if shift is active")
    void testIsActiveMethod() {
        employeeShift.setIsActive(true);
        employeeShift.setEffectiveFrom(LocalDate.now().minusDays(10));
        employeeShift.setEffectiveTo(LocalDate.now().plusDays(10));
        
        assertThat(employeeShift.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should correctly determine if shift is active on a specific date")
    void testIsActiveOnMethod() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        LocalDate testDate = LocalDate.of(2026, 6, 15);
        
        employeeShift.setEffectiveFrom(startDate);
        employeeShift.setEffectiveTo(endDate);
        
        assertThat(employeeShift.isActiveOn(testDate)).isTrue();
        assertThat(employeeShift.isActiveOn(LocalDate.of(2025, 12, 31))).isFalse();
        assertThat(employeeShift.isActiveOn(LocalDate.of(2027, 1, 1))).isFalse();
    }
}
