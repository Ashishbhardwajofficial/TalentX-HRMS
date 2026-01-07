package com.talentx.hrms.repository;

import com.talentx.hrms.entity.attendance.EmployeeShift;
import com.talentx.hrms.entity.attendance.Shift;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.enums.EmploymentStatus;
import com.talentx.hrms.entity.enums.EmploymentType;
import com.talentx.hrms.entity.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for EmployeeShiftRepository.findOverlappingByEmployee method.
 * Tests Requirements: 4.1
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmployeeShiftRepository Tests")
class EmployeeShiftRepositoryTest {

    @Autowired
    private EmployeeShiftRepository employeeShiftRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Employee testEmployee;
    private Shift testShift;
    private Organization testOrganization;
    private Department testDepartment;

    @BeforeEach
    void setup() {
        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setLegalName("Test Organization LLC");
        testOrganization = organizationRepository.save(testOrganization);

        // Create test department
        testDepartment = new Department();
        testDepartment.setName("Engineering");
        testDepartment.setCode("ENG");
        testDepartment.setOrganization(testOrganization);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@test.com");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testEmployee.setGender(Gender.MALE);
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setJobTitle("Engineer");
        testEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee.setDepartment(testDepartment);
        testEmployee.setOrganization(testOrganization);
        testEmployee = employeeRepository.save(testEmployee);

        // Create test shift
        testShift = new Shift();
        testShift.setName("Morning Shift");
        testShift.setCode("MORNING");
        testShift.setStartTime(LocalTime.of(9, 0));
        testShift.setEndTime(LocalTime.of(17, 0));
        testShift.setTotalHours(BigDecimal.valueOf(8.0));
        testShift.setOrganization(testOrganization);
        testShift = shiftRepository.save(testShift);
    }

    @Test
    @DisplayName("Should find overlapping shifts correctly")
    void testFindOverlappingByEmployee_FindsOverlappingShifts() {
        // Create shift assignments with different date ranges
        // Shift 1: Jan 1 - Jan 15
        EmployeeShift shift1 = new EmployeeShift();
        shift1.setEmployee(testEmployee);
        shift1.setShift(testShift);
        shift1.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        shift1.setEffectiveTo(LocalDate.of(2026, 1, 15));
        shift1.setIsActive(true);
        employeeShiftRepository.save(shift1);

        // Shift 2: Jan 10 - Jan 25 (overlaps with shift 1)
        EmployeeShift shift2 = new EmployeeShift();
        shift2.setEmployee(testEmployee);
        shift2.setShift(testShift);
        shift2.setEffectiveFrom(LocalDate.of(2026, 1, 10));
        shift2.setEffectiveTo(LocalDate.of(2026, 1, 25));
        shift2.setIsActive(true);
        employeeShiftRepository.save(shift2);

        // Shift 3: Feb 1 - Feb 15 (no overlap)
        EmployeeShift shift3 = new EmployeeShift();
        shift3.setEmployee(testEmployee);
        shift3.setShift(testShift);
        shift3.setEffectiveFrom(LocalDate.of(2026, 2, 1));
        shift3.setEffectiveTo(LocalDate.of(2026, 2, 15));
        shift3.setIsActive(true);
        employeeShiftRepository.save(shift3);

        // Test: Find overlapping shifts for Jan 5 - Jan 20
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 20)
        );

        // Should find shift1 and shift2, but not shift3
        assertEquals(2, overlapping.size(), "Should find 2 overlapping shifts");
        assertTrue(overlapping.stream().anyMatch(s -> s.getId().equals(shift1.getId())),
            "Should include shift1");
        assertTrue(overlapping.stream().anyMatch(s -> s.getId().equals(shift2.getId())),
            "Should include shift2");
    }

    @Test
    @DisplayName("Should handle null end dates correctly")
    void testFindOverlappingByEmployee_HandlesNullEndDate() {
        // Create shift with no end date (ongoing)
        EmployeeShift ongoingShift = new EmployeeShift();
        ongoingShift.setEmployee(testEmployee);
        ongoingShift.setShift(testShift);
        ongoingShift.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        ongoingShift.setEffectiveTo(null); // No end date
        ongoingShift.setIsActive(true);
        employeeShiftRepository.save(ongoingShift);

        // Test: Find overlapping shifts for future date range
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30)
        );

        // Should find the ongoing shift
        assertEquals(1, overlapping.size(), "Should find the ongoing shift");
        assertEquals(ongoingShift.getId(), overlapping.get(0).getId());
    }

    @Test
    @DisplayName("Should handle exact date matches")
    void testFindOverlappingByEmployee_HandlesExactDateMatches() {
        // Create shift: Jan 10 - Jan 20
        EmployeeShift shift = new EmployeeShift();
        shift.setEmployee(testEmployee);
        shift.setShift(testShift);
        shift.setEffectiveFrom(LocalDate.of(2026, 1, 10));
        shift.setEffectiveTo(LocalDate.of(2026, 1, 20));
        shift.setIsActive(true);
        employeeShiftRepository.save(shift);

        // Test 1: Query with exact start date match
        List<EmployeeShift> result1 = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 15)
        );
        assertEquals(1, result1.size(), "Should find shift with exact start date match");

        // Test 2: Query with exact end date match
        List<EmployeeShift> result2 = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 1, 15),
            LocalDate.of(2026, 1, 20)
        );
        assertEquals(1, result2.size(), "Should find shift with exact end date match");

        // Test 3: Query with both exact matches
        List<EmployeeShift> result3 = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 20)
        );
        assertEquals(1, result3.size(), "Should find shift with exact date range match");
    }

    @Test
    @DisplayName("Should return empty list when no overlaps exist")
    void testFindOverlappingByEmployee_ReturnsEmptyWhenNoOverlaps() {
        // Create shift: Jan 1 - Jan 15
        EmployeeShift shift = new EmployeeShift();
        shift.setEmployee(testEmployee);
        shift.setShift(testShift);
        shift.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        shift.setEffectiveTo(LocalDate.of(2026, 1, 15));
        shift.setIsActive(true);
        employeeShiftRepository.save(shift);

        // Test: Query for non-overlapping date range
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        );

        assertTrue(overlapping.isEmpty(), "Should return empty list when no overlaps exist");
    }

    @Test
    @DisplayName("Should only return shifts for specified employee")
    void testFindOverlappingByEmployee_FiltersCorrectEmployee() {
        // Create another employee
        Employee otherEmployee = new Employee();
        otherEmployee.setEmployeeNumber("EMP002");
        otherEmployee.setFirstName("Jane");
        otherEmployee.setLastName("Smith");
        otherEmployee.setEmail("jane.smith@test.com");
        otherEmployee.setPhoneNumber("+1234567891");
        otherEmployee.setDateOfBirth(LocalDate.of(1992, 1, 1));
        otherEmployee.setGender(Gender.FEMALE);
        otherEmployee.setHireDate(LocalDate.now());
        otherEmployee.setJobTitle("Engineer");
        otherEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        otherEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        otherEmployee.setDepartment(testDepartment);
        otherEmployee.setOrganization(testOrganization);
        otherEmployee = employeeRepository.save(otherEmployee);

        // Create shift for test employee
        EmployeeShift shift1 = new EmployeeShift();
        shift1.setEmployee(testEmployee);
        shift1.setShift(testShift);
        shift1.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        shift1.setEffectiveTo(LocalDate.of(2026, 1, 15));
        shift1.setIsActive(true);
        employeeShiftRepository.save(shift1);

        // Create shift for other employee
        EmployeeShift shift2 = new EmployeeShift();
        shift2.setEmployee(otherEmployee);
        shift2.setShift(testShift);
        shift2.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        shift2.setEffectiveTo(LocalDate.of(2026, 1, 15));
        shift2.setIsActive(true);
        employeeShiftRepository.save(shift2);

        // Test: Query for test employee only
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingByEmployee(
            testEmployee,
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 10)
        );

        assertEquals(1, overlapping.size(), "Should only return shifts for specified employee");
        assertEquals(testEmployee.getId(), overlapping.get(0).getEmployee().getId());
    }
}
