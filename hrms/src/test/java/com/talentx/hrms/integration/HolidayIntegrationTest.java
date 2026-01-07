package com.talentx.hrms.integration;

import com.talentx.hrms.dto.attendance.HolidayRequest;
import com.talentx.hrms.dto.attendance.HolidayResponse;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.repository.OrganizationRepository;
import com.talentx.hrms.service.holiday.HolidayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HolidayIntegrationTest {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setActive(true);
        testOrganization = organizationRepository.save(testOrganization);
    }

    @Test
    void testCreateAndRetrieveHoliday() {
        // Create holiday request
        HolidayRequest request = new HolidayRequest();
        request.setName("Independence Day");
        request.setHolidayDate(LocalDate.of(2024, 7, 4));
        request.setHolidayType("NATIONAL");
        request.setDescription("National Independence Day");
        request.setOrganizationId(testOrganization.getId());
        request.setIsOptional(false);

        // Create holiday
        HolidayResponse createdHoliday = holidayService.createHoliday(request);

        // Verify creation
        assertNotNull(createdHoliday);
        assertNotNull(createdHoliday.getId());
        assertEquals("Independence Day", createdHoliday.getName());
        assertEquals(LocalDate.of(2024, 7, 4), createdHoliday.getHolidayDate());
        assertEquals("NATIONAL", createdHoliday.getHolidayType());
        assertEquals("National Independence Day", createdHoliday.getDescription());
        assertEquals(testOrganization.getId(), createdHoliday.getOrganizationId());
        assertFalse(createdHoliday.getIsOptional());

        // Retrieve holiday
        HolidayResponse retrievedHoliday = holidayService.getHoliday(createdHoliday.getId());

        // Verify retrieval
        assertNotNull(retrievedHoliday);
        assertEquals(createdHoliday.getId(), retrievedHoliday.getId());
        assertEquals(createdHoliday.getName(), retrievedHoliday.getName());
        assertEquals(createdHoliday.getHolidayDate(), retrievedHoliday.getHolidayDate());
        assertEquals(createdHoliday.getHolidayType(), retrievedHoliday.getHolidayType());
    }

    @Test
    void testHolidayCalendarGeneration() {
        // Create multiple holidays
        createTestHoliday("New Year", LocalDate.of(2024, 1, 1), "NATIONAL");
        createTestHoliday("Christmas", LocalDate.of(2024, 12, 25), "NATIONAL");
        createTestHoliday("Company Day", LocalDate.of(2024, 6, 15), "COMPANY");

        // Generate calendar
        var calendar = holidayService.generateHolidayCalendar(testOrganization.getId(), 2024);

        // Verify calendar
        assertNotNull(calendar);
        assertEquals(2024, calendar.getYear());
        assertEquals(testOrganization.getId(), calendar.getOrganizationId());
        assertEquals(3, calendar.getTotalHolidays());
        assertEquals(3, calendar.getHolidays().size());
    }

    @Test
    void testLeaveDaysCalculation() {
        // Create a holiday in the middle of the week
        createTestHoliday("Mid Week Holiday", LocalDate.of(2024, 1, 3), "NATIONAL");

        // Calculate leave days (Jan 1-5, 2024)
        // Jan 1 (Monday), Jan 2 (Tuesday), Jan 3 (Wednesday - Holiday), Jan 4 (Thursday), Jan 5 (Friday)
        int leaveDays = holidayService.calculateLeaveDays(
            testOrganization.getId(), 
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 5)
        );

        // Should be 4 working days (excluding the holiday on Jan 3)
        assertEquals(4, leaveDays);
    }

    @Test
    void testIsHolidayCheck() {
        // Create holiday
        createTestHoliday("Test Holiday", LocalDate.of(2024, 5, 1), "NATIONAL");

        // Check if date is holiday
        assertTrue(holidayService.isHoliday(testOrganization.getId(), LocalDate.of(2024, 5, 1)));
        assertFalse(holidayService.isHoliday(testOrganization.getId(), LocalDate.of(2024, 5, 2)));
    }

    private void createTestHoliday(String name, LocalDate date, String type) {
        HolidayRequest request = new HolidayRequest();
        request.setName(name);
        request.setHolidayDate(date);
        request.setHolidayType(type);
        request.setOrganizationId(testOrganization.getId());
        request.setIsOptional(false);
        
        holidayService.createHoliday(request);
    }
}

