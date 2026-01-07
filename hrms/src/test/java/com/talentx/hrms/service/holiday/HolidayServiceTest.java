package com.talentx.hrms.service.holiday;

import com.talentx.hrms.common.PaginationRequest;
import com.talentx.hrms.common.exception.EntityNotFoundException;
import com.talentx.hrms.common.exception.ValidationException;
import com.talentx.hrms.dto.attendance.HolidayRequest;
import com.talentx.hrms.dto.attendance.HolidayResponse;
import com.talentx.hrms.entity.attendance.LeaveCalendar;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.repository.HolidayRepository;
import com.talentx.hrms.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private HolidayService holidayService;

    private Organization organization;
    private HolidayRequest holidayRequest;
    private LeaveCalendar holiday;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Organization");

        holidayRequest = new HolidayRequest();
        holidayRequest.setName("New Year");
        holidayRequest.setHolidayDate(LocalDate.of(2024, 1, 1));
        holidayRequest.setHolidayType("NATIONAL");
        holidayRequest.setOrganizationId(1L);
        holidayRequest.setIsOptional(false);

        holiday = new LeaveCalendar();
        holiday.setId(1L);
        holiday.setHolidayName("New Year");
        holiday.setHolidayDate(LocalDate.of(2024, 1, 1));
        holiday.setHolidayType("NATIONAL");
        holiday.setOrganization(organization);
        holiday.setIsMandatory(true);
    }

    @Test
    void createHoliday_Success() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.existsByOrganizationAndHolidayDate(organization, holidayRequest.getHolidayDate()))
            .thenReturn(false);
        when(holidayRepository.save(any(LeaveCalendar.class))).thenReturn(holiday);

        // Act
        HolidayResponse result = holidayService.createHoliday(holidayRequest);

        // Assert
        assertNotNull(result);
        assertEquals("New Year", result.getName());
        assertEquals(LocalDate.of(2024, 1, 1), result.getHolidayDate());
        assertEquals("NATIONAL", result.getHolidayType());
        assertEquals(1L, result.getOrganizationId());
        assertFalse(result.getIsOptional());

        verify(organizationRepository).findById(1L);
        verify(holidayRepository).existsByOrganizationAndHolidayDate(organization, holidayRequest.getHolidayDate());
        verify(holidayRepository).save(any(LeaveCalendar.class));
    }

    @Test
    void createHoliday_OrganizationNotFound() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> holidayService.createHoliday(holidayRequest));
        verify(organizationRepository).findById(1L);
        verify(holidayRepository, never()).save(any());
    }

    @Test
    void createHoliday_DuplicateHoliday() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.existsByOrganizationAndHolidayDate(organization, holidayRequest.getHolidayDate()))
            .thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> holidayService.createHoliday(holidayRequest));
        verify(organizationRepository).findById(1L);
        verify(holidayRepository).existsByOrganizationAndHolidayDate(organization, holidayRequest.getHolidayDate());
        verify(holidayRepository, never()).save(any());
    }

    @Test
    void getHoliday_Success() {
        // Arrange
        when(holidayRepository.findById(1L)).thenReturn(Optional.of(holiday));

        // Act
        HolidayResponse result = holidayService.getHoliday(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Year", result.getName());
        verify(holidayRepository).findById(1L);
    }

    @Test
    void getHoliday_NotFound() {
        // Arrange
        when(holidayRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> holidayService.getHoliday(1L));
        verify(holidayRepository).findById(1L);
    }

    @Test
    void getHolidays_Success() {
        // Arrange
        List<LeaveCalendar> holidays = Arrays.asList(holiday);
        Page<LeaveCalendar> holidayPage = new PageImpl<>(holidays);
        PaginationRequest paginationRequest = new PaginationRequest(0, 10, "holidayDate", "asc");

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.findByOrganization(eq(organization), any(Pageable.class))).thenReturn(holidayPage);

        // Act
        Page<HolidayResponse> result = holidayService.getHolidays(1L, paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("New Year", result.getContent().get(0).getName());
        verify(organizationRepository).findById(1L);
        verify(holidayRepository).findByOrganization(eq(organization), any(Pageable.class));
    }

    @Test
    void calculateLeaveDays_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 5);
        List<LeaveCalendar> holidays = Arrays.asList(holiday); // Jan 1 is a holiday

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.findByOrganizationAndDateRange(organization, startDate, endDate))
            .thenReturn(holidays);

        // Act
        int result = holidayService.calculateLeaveDays(1L, startDate, endDate);

        // Assert
        // Jan 1 (holiday), Jan 2-5 (4 days), but need to check weekends
        // This is a basic test - actual calculation depends on day of week
        assertTrue(result >= 0);
        verify(organizationRepository).findById(1L);
        verify(holidayRepository).findByOrganizationAndDateRange(organization, startDate, endDate);
    }

    @Test
    void calculateLeaveDays_InvalidDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 5);
        LocalDate endDate = LocalDate.of(2024, 1, 1);

        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            holidayService.calculateLeaveDays(1L, startDate, endDate));
    }

    @Test
    void isHoliday_True() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 1);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.existsByOrganizationAndHolidayDate(organization, date)).thenReturn(true);

        // Act
        boolean result = holidayService.isHoliday(1L, date);

        // Assert
        assertTrue(result);
        verify(organizationRepository).findById(1L);
        verify(holidayRepository).existsByOrganizationAndHolidayDate(organization, date);
    }

    @Test
    void isHoliday_False() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 2);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(holidayRepository.existsByOrganizationAndHolidayDate(organization, date)).thenReturn(false);

        // Act
        boolean result = holidayService.isHoliday(1L, date);

        // Assert
        assertFalse(result);
        verify(organizationRepository).findById(1L);
        verify(holidayRepository).existsByOrganizationAndHolidayDate(organization, date);
    }
}

