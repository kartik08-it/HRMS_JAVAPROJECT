package com.kartik.hrms.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kartik.hrms.dto.AbsenteeDTO;
import com.kartik.hrms.dto.AttendanceActivityDTO;
import com.kartik.hrms.dto.AttendanceDashboardResponseDTO;
import com.kartik.hrms.dto.AttendanceDepartmentDTO;
import com.kartik.hrms.dto.AttendanceSummaryDTO;
import com.kartik.hrms.dto.LateArrivalDTO;
import com.kartik.hrms.dto.WeeklyTrendDTO;
import com.kartik.hrms.entity.AttendanceLog;
import com.kartik.hrms.entity.AttendanceRecord;
import com.kartik.hrms.entity.Employee;
import com.kartik.hrms.exception.ForbiddenException;
import com.kartik.hrms.exception.UnauthorizedException;
import com.kartik.hrms.repository.AttendanceLogRepository;
import com.kartik.hrms.repository.AttendanceRecordRepository;
import com.kartik.hrms.repository.EmployeeRepository;
import com.kartik.hrms.security.AuthenticatedUser;

@Service
public class AttendanceDashboardService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    public AttendanceDashboardService(EmployeeRepository employeeRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            AttendanceLogRepository attendanceLogRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.attendanceLogRepository = attendanceLogRepository;
    }

    public AttendanceDashboardResponseDTO getDashboard(LocalDate date, AuthenticatedUser actor) {
        LocalDate targetDate = date == null ? attendanceRecordRepository.fetchCurrentDate() : date;

        List<Employee> employees = employeeRepository.findByIsDeletedFalse();
        Map<Long, Employee> employeeById = employees.stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByDate(targetDate);
        List<AttendanceRecord> activeRecords = records.stream()
                .filter(record -> employeeById.containsKey(record.getEmployee().getId()))
                .collect(Collectors.toList());
        Map<Long, AttendanceRecord> recordByEmployeeId = activeRecords.stream()
                .collect(Collectors.toMap(record -> record.getEmployee().getId(), record -> record));

        AttendanceSummaryDTO summary = buildSummary(employees.size(), recordByEmployeeId);
        List<AttendanceDepartmentDTO> departments = buildDepartments(employees, recordByEmployeeId);
        List<AttendanceActivityDTO> recentActivity = buildRecentActivity(targetDate, employeeById);
        List<LateArrivalDTO> lateArrivals = buildLateArrivals(activeRecords);
        List<AbsenteeDTO> absentees = buildAbsentees(activeRecords);
        List<WeeklyTrendDTO> weeklyTrend = buildWeeklyTrend(employees, targetDate);

        return new AttendanceDashboardResponseDTO(
                summary,
                departments,
                recentActivity,
                lateArrivals,
                absentees,
                weeklyTrend);
    }

    private AttendanceSummaryDTO buildSummary(int totalEmployees, Map<Long, AttendanceRecord> recordByEmployeeId) {
        int present = 0;
        int late = 0;
        int workFromHome = 0;
        int onLeave = 0;
        int halfDay = 0;

        for (AttendanceRecord record : recordByEmployeeId.values()) {
            String state = normalizeState(record.getState());
            if ("PRESENT".equals(state)) {
                present++;
            } else if ("LATE".equals(state)) {
                late++;
            } else if ("WORK_FROM_HOME".equals(state)) {
                workFromHome++;
            } else if ("LEAVE".equals(state)) {
                onLeave++;
            } else if ("HALF_DAY".equals(state)) {
                halfDay++;
            }
        }

        int accounted = present + late + workFromHome + onLeave + halfDay;
        int absent = Math.max(0, totalEmployees - accounted);

        return new AttendanceSummaryDTO(totalEmployees, present, absent, late, workFromHome, onLeave, halfDay);
    }

    private List<AttendanceDepartmentDTO> buildDepartments(List<Employee> employees,
            Map<Long, AttendanceRecord> recordByEmployeeId) {
        Map<String, DepartmentAggregate> aggregates = new LinkedHashMap<>();

        for (Employee employee : employees) {
            String department = normalizeDepartment(employee.getDepartment());
            DepartmentAggregate aggregate = aggregates.computeIfAbsent(department, key -> new DepartmentAggregate());
            aggregate.total++;

            AttendanceRecord record = recordByEmployeeId.get(employee.getId());
            String state = record == null ? null : normalizeState(record.getState());

            if ("LATE".equals(state)) {
                aggregate.late++;
            } else if ("PRESENT".equals(state) || "WORK_FROM_HOME".equals(state) || "HALF_DAY".equals(state)) {
                aggregate.present++;
            }
        }

        List<AttendanceDepartmentDTO> result = new ArrayList<>();
        for (Map.Entry<String, DepartmentAggregate> entry : aggregates.entrySet()) {
            DepartmentAggregate aggregate = entry.getValue();
            int absent = Math.max(0, aggregate.total - aggregate.present - aggregate.late);
            result.add(new AttendanceDepartmentDTO(entry.getKey(), aggregate.present, absent, aggregate.late, aggregate.total));
        }

        return result;
    }

    private List<AttendanceActivityDTO> buildRecentActivity(LocalDate targetDate,
            Map<Long, Employee> employeeById) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        List<AttendanceLog> logs = attendanceLogRepository
                .findTop8ByTimestampBetweenOrderByTimestampDesc(start, end);

        List<AttendanceActivityDTO> activity = new ArrayList<>();
        for (AttendanceLog log : logs) {
            Employee employee = employeeById.get(log.getEmployee().getId());
            if (employee == null) {
                continue;
            }
            activity.add(new AttendanceActivityDTO(
                    log.getId(),
                    resolveName(employee),
                    formatAction(log.getAction()),
                    formatTime(log.getTimestamp()),
                    formatStatus(log.getStatus()),
                    normalizeDepartment(employee.getDepartment())));
        }
        return activity;
    }

    private List<LateArrivalDTO> buildLateArrivals(List<AttendanceRecord> records) {
        List<AttendanceRecord> lateRecords = records.stream()
                .filter(record -> "LATE".equals(normalizeState(record.getState())))
                .sorted(Comparator.comparing(AttendanceRecord::getLateMinutes,
                        Comparator.nullsLast(Integer::compareTo)).reversed())
                .collect(Collectors.toList());

        List<LateArrivalDTO> lateArrivals = new ArrayList<>();
        for (AttendanceRecord record : lateRecords) {
            Employee employee = record.getEmployee();
            lateArrivals.add(new LateArrivalDTO(
                    resolveName(employee),
                    normalizeDepartment(employee.getDepartment()),
                    formatTime(record.getCheckInTime()),
                    formatDelay(record.getLateMinutes())));
        }

        return lateArrivals;
    }

    private List<AbsenteeDTO> buildAbsentees(List<AttendanceRecord> records) {
        List<AbsenteeDTO> absentees = new ArrayList<>();

        for (AttendanceRecord record : records) {
            String state = normalizeState(record.getState());
            if (!"ABSENT".equals(state) && !"LEAVE".equals(state)) {
                continue;
            }
            Employee employee = record.getEmployee();
            String reason = normalizeOptional(record.getAbsenceReason());
            if (reason == null) {
                reason = "LEAVE".equals(state) ? "Leave" : "Unplanned";
            }
            boolean approved = Boolean.TRUE.equals(record.getAbsenceApproved());
            absentees.add(new AbsenteeDTO(
                    resolveName(employee),
                    normalizeDepartment(employee.getDepartment()),
                    reason,
                    approved));
        }

        return absentees;
    }

    private List<WeeklyTrendDTO> buildWeeklyTrend(List<Employee> employees, LocalDate targetDate) {
        LocalDate startDate = targetDate.minusDays(4);
        List<AttendanceRecord> weeklyRecords = attendanceRecordRepository
                .findByDateBetween(startDate, targetDate);

        Map<LocalDate, Map<Long, AttendanceRecord>> recordsByDate = new HashMap<>();
        for (AttendanceRecord record : weeklyRecords) {
            LocalDate date = record.getDate();
            recordsByDate
                    .computeIfAbsent(date, key -> new HashMap<>())
                    .put(record.getEmployee().getId(), record);
        }

        List<WeeklyTrendDTO> trend = new ArrayList<>();
        int totalEmployees = employees.size();

        for (int i = 4; i >= 0; i--) {
            LocalDate day = targetDate.minusDays(i);
            Map<Long, AttendanceRecord> dailyRecords = recordsByDate.getOrDefault(day, Collections.emptyMap());

            int present = 0;
            int late = 0;
            int workFromHome = 0;
            int onLeave = 0;
            int halfDay = 0;

            for (Employee employee : employees) {
                AttendanceRecord record = dailyRecords.get(employee.getId());
                if (record == null) {
                    continue;
                }
                String state = normalizeState(record.getState());
                if ("PRESENT".equals(state)) {
                    present++;
                } else if ("LATE".equals(state)) {
                    late++;
                } else if ("WORK_FROM_HOME".equals(state)) {
                    workFromHome++;
                } else if ("LEAVE".equals(state)) {
                    onLeave++;
                } else if ("HALF_DAY".equals(state)) {
                    halfDay++;
                }
            }

            int accounted = present + late + workFromHome + onLeave + halfDay;
            int absent = Math.max(0, totalEmployees - accounted);
            trend.add(new WeeklyTrendDTO(day.format(DAY_FORMATTER), present, absent, late));
        }

        return trend;
    }

    private String normalizeDepartment(String department) {
        String normalized = normalizeOptional(department);
        return normalized == null ? "Unassigned" : normalized;
    }

    private String resolveName(Employee employee) {
        String name = normalizeOptional(employee.getFullName());
        if (name != null) {
            return name;
        }
        return employee.getUsername();
    }

    private String normalizeState(String state) {
        if (state == null) {
            return null;
        }
        String trimmed = state.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ENGLISH);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.format(TIME_FORMATTER);
    }

    private String formatAction(String action) {
        if (action == null) {
            return null;
        }
        String normalized = action.trim().toUpperCase(Locale.ENGLISH);
        if ("CHECK_IN".equals(normalized)) {
            return "Check In";
        }
        if ("CHECK_OUT".equals(normalized)) {
            return "Check Out";
        }
        return action.trim();
    }

    private String formatStatus(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ENGLISH);
        if ("ON_TIME".equals(normalized) || "ON-TIME".equals(normalized)) {
            return "on-time";
        }
        if ("LATE".equals(normalized)) {
            return "late";
        }
        return status.trim().toLowerCase(Locale.ENGLISH);
    }

    private String formatDelay(Integer minutesLate) {
        if (minutesLate == null || minutesLate <= 0) {
            return null;
        }
        if (minutesLate < 60) {
            return minutesLate + (minutesLate == 1 ? " min" : " mins");
        }
        double hours = minutesLate / 60.0;
        DecimalFormat formatter = new DecimalFormat("#.#");
        return formatter.format(hours) + " hrs";
    }

    private Long requireAdmin(AuthenticatedUser actor) {
        if (actor == null || actor.getUserId() == null) {
            throw new UnauthorizedException("Authentication token is required");
        }
        if (actor.getRole() == null || !"ADMIN".equalsIgnoreCase(actor.getRole())) {
            throw new ForbiddenException("Only admin can access attendance dashboard");
        }
        return actor.getUserId();
    }

    private static class DepartmentAggregate {
        private int present;
        private int late;
        private int total;
    }
}
