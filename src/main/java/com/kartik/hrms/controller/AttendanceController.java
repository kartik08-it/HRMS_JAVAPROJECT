package com.kartik.hrms.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kartik.hrms.dto.AttendanceDashboardResponseDTO;
import com.kartik.hrms.security.AuthenticatedUser;
import com.kartik.hrms.service.AttendanceDashboardService;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceDashboardService attendanceDashboardService;

    public AttendanceController(AttendanceDashboardService attendanceDashboardService) {
        this.attendanceDashboardService = attendanceDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AttendanceDashboardResponseDTO> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false)
            String range,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(attendanceDashboardService.getDashboard(date, range, actor));
    }
}
