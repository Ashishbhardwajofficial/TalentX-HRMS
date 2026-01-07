package com.talentx.hrms.entity;

import com.talentx.hrms.entity.attendance.Shift;
import com.talentx.hrms.entity.core.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Shift entity accessor methods.
 * Validates: Requirements 1.1
 */
@DisplayName("Shift Entity Tests")
class ShiftEntityTest {

    private Shift shift;
    private Organization organization;

    @BeforeEach
    void setUp() {
        shift = new Shift();
        organization = new Organization();
        organization.setId(1L);
    }

    @Test
    @DisplayName("Should set and get description")
    void testDescriptionAccessors() {
        String description = "Morning shift for production team";
        shift.setDescription(description);
        assertThat(shift.getDescription()).isEqualTo(description);
    }

    @Test
    @DisplayName("Should set and get break start time")
    void testBreakStartTimeAccessors() {
        LocalTime breakStartTime = LocalTime.of(12, 0);
        shift.setBreakStartTime(breakStartTime);
        assertThat(shift.getBreakStartTime()).isEqualTo(breakStartTime);
    }

    @Test
    @DisplayName("Should set and get break end time")
    void testBreakEndTimeAccessors() {
        LocalTime breakEndTime = LocalTime.of(13, 0);
        shift.setBreakEndTime(breakEndTime);
        assertThat(shift.getBreakEndTime()).isEqualTo(breakEndTime);
    }

    @Test
    @DisplayName("Should set and get grace period minutes")
    void testGracePeriodMinutesAccessors() {
        Integer gracePeriodMinutes = 15;
        shift.setGracePeriodMinutes(gracePeriodMinutes);
        assertThat(shift.getGracePeriodMinutes()).isEqualTo(gracePeriodMinutes);
    }

    @Test
    @DisplayName("Should set and get isFlexible flag")
    void testIsFlexibleAccessors() {
        Boolean isFlexible = true;
        shift.setIsFlexible(isFlexible);
        assertThat(shift.getIsFlexible()).isEqualTo(isFlexible);
    }

    @Test
    @DisplayName("Should set and get minimum hours")
    void testMinimumHoursAccessors() {
        BigDecimal minimumHours = new BigDecimal("4.0");
        shift.setMinimumHours(minimumHours);
        assertThat(shift.getMinimumHours()).isEqualTo(minimumHours);
    }

    @Test
    @DisplayName("Should set and get total hours")
    void testTotalHoursAccessors() {
        BigDecimal totalHours = new BigDecimal("8.0");
        shift.setTotalHours(totalHours);
        assertThat(shift.getTotalHours()).isEqualTo(totalHours);
    }

    @Test
    @DisplayName("Should set and get updated at timestamp")
    void testUpdatedAtAccessors() {
        Instant updatedAt = Instant.now();
        shift.setUpdatedAt(updatedAt);
        assertThat(shift.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Should properly store and retrieve all fields together")
    void testAllFieldsAccessors() {
        // Arrange
        String name = "Day Shift";
        String description = "Standard day shift";
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        LocalTime breakStartTime = LocalTime.of(12, 0);
        LocalTime breakEndTime = LocalTime.of(13, 0);
        Integer gracePeriodMinutes = 10;
        Boolean isFlexible = false;
        Boolean isNightShift = false;
        BigDecimal minimumHours = new BigDecimal("6.0");
        BigDecimal totalHours = new BigDecimal("8.0");
        Instant now = Instant.now();

        // Act
        shift.setName(name);
        shift.setDescription(description);
        shift.setOrganization(organization);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setBreakStartTime(breakStartTime);
        shift.setBreakEndTime(breakEndTime);
        shift.setGracePeriodMinutes(gracePeriodMinutes);
        shift.setIsFlexible(isFlexible);
        shift.setIsNightShift(isNightShift);
        shift.setMinimumHours(minimumHours);
        shift.setTotalHours(totalHours);
        shift.setUpdatedAt(now);

        // Assert
        assertThat(shift.getName()).isEqualTo(name);
        assertThat(shift.getDescription()).isEqualTo(description);
        assertThat(shift.getOrganization()).isEqualTo(organization);
        assertThat(shift.getStartTime()).isEqualTo(startTime);
        assertThat(shift.getEndTime()).isEqualTo(endTime);
        assertThat(shift.getBreakStartTime()).isEqualTo(breakStartTime);
        assertThat(shift.getBreakEndTime()).isEqualTo(breakEndTime);
        assertThat(shift.getGracePeriodMinutes()).isEqualTo(gracePeriodMinutes);
        assertThat(shift.getIsFlexible()).isEqualTo(isFlexible);
        assertThat(shift.getIsNightShift()).isEqualTo(isNightShift);
        assertThat(shift.getMinimumHours()).isEqualTo(minimumHours);
        assertThat(shift.getTotalHours()).isEqualTo(totalHours);
        assertThat(shift.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should handle null values properly")
    void testNullValues() {
        shift.setDescription(null);
        shift.setBreakStartTime(null);
        shift.setBreakEndTime(null);
        shift.setGracePeriodMinutes(null);
        shift.setIsFlexible(null);
        shift.setMinimumHours(null);
        shift.setTotalHours(null);
        shift.setUpdatedAt(null);

        assertThat(shift.getDescription()).isNull();
        assertThat(shift.getBreakStartTime()).isNull();
        assertThat(shift.getBreakEndTime()).isNull();
        assertThat(shift.getGracePeriodMinutes()).isNull();
        assertThat(shift.getIsFlexible()).isNull();
        assertThat(shift.getMinimumHours()).isNull();
        assertThat(shift.getTotalHours()).isNull();
        assertThat(shift.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should use constructor properly")
    void testConstructor() {
        String name = "Night Shift";
        LocalTime startTime = LocalTime.of(22, 0);
        LocalTime endTime = LocalTime.of(6, 0);

        Shift constructedShift = new Shift(name, startTime, endTime, organization);

        assertThat(constructedShift.getName()).isEqualTo(name);
        assertThat(constructedShift.getStartTime()).isEqualTo(startTime);
        assertThat(constructedShift.getEndTime()).isEqualTo(endTime);
        assertThat(constructedShift.getOrganization()).isEqualTo(organization);
    }
}
