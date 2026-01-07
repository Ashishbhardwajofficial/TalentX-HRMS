package com.talentx.hrms.controller.attendance;

import com.talentx.hrms.common.ApiResponse;
import com.talentx.hrms.dto.attendance.*;
import com.talentx.hrms.entity.enums.AttendanceStatus;
import com.talentx.hrms.service.attendance.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Management", description = "Attendance tracking and management operations")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Check in an employee
     * POST /api/attendance/check-in
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Check in employee", description = "Record employee check-in with timestamp and location")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkIn(@Valid @RequestBody CheckInRequest request) {
        try {
            AttendanceRecordResponse response = attendanceService.checkIn(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in recorded successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check out an employee
     * POST /api/attendance/check-out
     */
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Check out employee", description = "Record employee check-out with timestamp and calculate hours worked")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        try {
            AttendanceRecordResponse response = attendanceService.checkOut(request);
            return ResponseEntity.ok(ApiResponse.success("Check-out recorded successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get attendance records with filtering and pagination
     * GET /api/attendance/records
     */
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Get attendance records", description = "Retrieve attendance records with filtering and pagination")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getAttendanceRecords(
            @Parameter(description = "Organization ID") @RequestParam(required = false) Long organizationId,
            @Parameter(description = "Employee name") @RequestParam(required = false) String employeeName,
            @Parameter(description = "Attendance status") @RequestParam(required = false) AttendanceStatus status,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AttendanceRecordResponse> records = attendanceService.getAttendanceRecords(
            organizationId, employeeName, status, startDate, endDate, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", records));
    }

    /**
     * Get attendance records for a specific employee
     * GET /api/attendance/employee/{id}
     */
    @GetMapping("/employee/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @attendanceService.isCurrentUser(#id)")
    @Operation(summary = "Get employee attendance", description = "Retrieve attendance records for a specific employee")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getEmployeeAttendance(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<AttendanceRecordResponse> records = attendanceService.getEmployeeAttendance(id, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Employee attendance retrieved successfully", records));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update an attendance record
     * PUT /api/attendance/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    @Operation(summary = "Update attendance record", description = "Update an existing attendance record")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> updateAttendanceRecord(
            @Parameter(description = "Attendance record ID") @PathVariable Long id,
            @Valid @RequestBody AttendanceRecordRequest request) {
        
        try {
            AttendanceRecordResponse response = attendanceService.updateAttendanceRecord(id, request);
            return ResponseEntity.ok(ApiResponse.success("Attendance record updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Generate attendance report for an employee
     * GET /api/attendance/employee/{id}/report
     */
    @GetMapping("/employee/{id}/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER') or @attendanceService.isCurrentUser(#id)")
    @Operation(summary = "Generate attendance report", description = "Generate attendance summary report for an employee")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> generateAttendanceReport(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            AttendanceReportResponse report = attendanceService.generateAttendanceReport(id, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Attendance report generated successfully", report));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

