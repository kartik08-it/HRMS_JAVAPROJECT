package com.kartik.hrms.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
import com.kartik.hrms.exception.BadRequestException;
import com.kartik.hrms.repository.AttendanceLogRepository;
import com.kartik.hrms.repository.AttendanceRecordRepository;
import com.kartik.hrms.repository.EmployeeRepository;
import com.kartik.hrms.security.AuthenticatedUser;

@Service
public class AttendanceDashboardService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM hh:mm a",
            Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

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

    public AttendanceDashboardResponseDTO getDashboard(LocalDate date, String range, AuthenticatedUser actor) {
        LocalDate targetDate = date == null ? attendanceRecordRepository.fetchCurrentDate() : date;
        DashboardRangeSelection rangeSelection = resolveRange(targetDate, range);

        List<Employee> employees = employeeRepository.findByIsDeletedFalse();
        Map<Long, Employee> employeeById = employees.stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee));

        List<AttendanceRecord> selectedRecords = filterActiveEmployees(
                attendanceRecordRepository.findByDateBetween(rangeSelection.getStartDate(), rangeSelection.getEndDate()),
                employeeById);
        Map<Long, AttendanceRecord> snapshotByEmployeeId = mapLatestRecordByEmployee(selectedRecords);

        LocalDate trendStartDate = rangeSelection.getTrendStartDate();
        List<AttendanceRecord> trendRecords = filterActiveEmployees(
                attendanceRecordRepository.findByDateBetween(trendStartDate, rangeSelection.getEndDate()),
                employeeById);

        AttendanceSummaryDTO summary = buildSummary(employees.size(), snapshotByEmployeeId);
        List<AttendanceDepartmentDTO> departments = buildDepartments(employees, snapshotByEmployeeId);
        List<AttendanceActivityDTO> recentActivity = buildRecentActivity(rangeSelection, employeeById);
        List<LateArrivalDTO> lateArrivals = buildLateArrivals(selectedRecords, rangeSelection.spansMultipleDays());
        List<AbsenteeDTO> absentees = buildAbsentees(selectedRecords);
        List<WeeklyTrendDTO> weeklyTrend = buildTrend(employees, trendRecords, rangeSelection);

        return new AttendanceDashboardResponseDTO(
                targetDate,
                rangeSelection.getLabel(),
                rangeSelection.getStartDate(),
                rangeSelection.getEndDate(),
                summary,
                departments,
                recentActivity,
                lateArrivals,
                absentees,
                weeklyTrend);
    }

    private List<AttendanceRecord> filterActiveEmployees(List<AttendanceRecord> records, Map<Long, Employee> employeeById) {
        return records.stream()
                .filter(record -> employeeById.containsKey(record.getEmployee().getId()))
                .collect(Collectors.toList());
    }

    private Map<Long, AttendanceRecord> mapLatestRecordByEmployee(List<AttendanceRecord> records) {
        Map<Long, AttendanceRecord> latestRecords = new HashMap<>();

        for (AttendanceRecord record : records) {
            Long employeeId = record.getEmployee().getId();
            AttendanceRecord current = latestRecords.get(employeeId);
            if (current == null || record.getDate().isAfter(current.getDate())) {
                latestRecords.put(employeeId, record);
            }
        }

        return latestRecords;
    }

    private AttendanceSummaryDTO buildSummary(int totalEmployees, Map<Long, AttendanceRecord> recordByEmployeeId) {
        int present = 0;
        int late = 0;
        int workFromHome = 0;
        int onLeave = 0;
        int halfDay = 0;

        for (AttendanceRecord record : recordByEmployeeId.values()) {
            String state = normalizeState(record.getState());
            if (null != state) switch (state) {
                case "PRESENT" -> present++;
                case "LATE" -> late++;
                case "WORK_FROM_HOME" -> workFromHome++;
                case "LEAVE" -> onLeave++;
                case "HALF_DAY" -> halfDay++;
                default -> {
                }
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

    private List<AttendanceActivityDTO> buildRecentActivity(DashboardRangeSelection rangeSelection,
            Map<Long, Employee> employeeById) {
        LocalDateTime start = rangeSelection.getStartDate().atStartOfDay();
        LocalDateTime end = rangeSelection.getEndDate().plusDays(1).atStartOfDay();

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
                    formatActivityTime(log.getTimestamp(), rangeSelection.spansMultipleDays()),
                    formatStatus(log.getStatus()),
                    normalizeDepartment(employee.getDepartment())));
        }
        return activity;
    }

    private List<LateArrivalDTO> buildLateArrivals(List<AttendanceRecord> records, boolean spansMultipleDays) {
        List<AttendanceRecord> lateRecords = records.stream()
                .filter(record -> "LATE".equals(normalizeState(record.getState())))
                .sorted(Comparator.comparing(AttendanceRecord::getDate, Comparator.reverseOrder())
                        .thenComparing(AttendanceRecord::getLateMinutes,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<LateArrivalDTO> lateArrivals = new ArrayList<>();
        for (AttendanceRecord record : lateRecords) {
            Employee employee = record.getEmployee();
            lateArrivals.add(new LateArrivalDTO(
                    resolveName(employee),
                    normalizeDepartment(employee.getDepartment()),
                    record.getDate(),
                    formatRecordTime(record.getCheckInTime(), spansMultipleDays),
                    formatDelay(record.getLateMinutes())));
        }

        return lateArrivals;
    }

    private List<AbsenteeDTO> buildAbsentees(List<AttendanceRecord> records) {
        List<AbsenteeDTO> absentees = new ArrayList<>();

        List<AttendanceRecord> absenteeRecords = records.stream()
                .filter(record -> {
                    String state = normalizeState(record.getState());
                    return "ABSENT".equals(state) || "LEAVE".equals(state);
                })
                .sorted(Comparator.comparing(AttendanceRecord::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        for (AttendanceRecord record : absenteeRecords) {
            String state = normalizeState(record.getState());
            Employee employee = record.getEmployee();
            String reason = normalizeOptional(record.getAbsenceReason());
            if (reason == null) {
                reason = "LEAVE".equals(state) ? "Leave" : "Unplanned";
            }
            boolean approved = Boolean.TRUE.equals(record.getAbsenceApproved());
            absentees.add(new AbsenteeDTO(
                    resolveName(employee),
                    normalizeDepartment(employee.getDepartment()),
                    record.getDate(),
                    reason,
                    approved));
        }

        return absentees;
    }

    private List<WeeklyTrendDTO> buildTrend(List<Employee> employees, List<AttendanceRecord> records,
            DashboardRangeSelection rangeSelection) {
        if ("YEAR".equals(rangeSelection.getLabel())) {
            return buildMonthlyTrend(employees, records, rangeSelection);
        }
        return buildDailyTrend(employees, records, rangeSelection);
    }

    private List<WeeklyTrendDTO> buildDailyTrend(List<Employee> employees, List<AttendanceRecord> records,
            DashboardRangeSelection rangeSelection) {
        Map<LocalDate, Map<Long, AttendanceRecord>> recordsByDate = new HashMap<>();
        for (AttendanceRecord record : records) {
            recordsByDate
                    .computeIfAbsent(record.getDate(), key -> new HashMap<>())
                    .put(record.getEmployee().getId(), record);
        }

        List<WeeklyTrendDTO> trend = new ArrayList<>();
        int totalEmployees = employees.size();

        LocalDate startDate = rangeSelection.getTrendStartDate();
        LocalDate endDate = rangeSelection.getEndDate();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            Map<Long, AttendanceRecord> dailyRecords = recordsByDate.getOrDefault(day, Collections.emptyMap());
            trend.add(buildTrendPoint(formatDayLabel(day, rangeSelection), totalEmployees, dailyRecords));
        }

        return trend;
    }

    private List<WeeklyTrendDTO> buildMonthlyTrend(List<Employee> employees, List<AttendanceRecord> records,
            DashboardRangeSelection rangeSelection) {
        Map<YearMonth, Map<Long, AttendanceRecord>> recordsByMonth = new LinkedHashMap<>();
        for (AttendanceRecord record : records) {
            YearMonth month = YearMonth.from(record.getDate());
            Map<Long, AttendanceRecord> snapshot = recordsByMonth.computeIfAbsent(month, key -> new HashMap<>());
            Long employeeId = record.getEmployee().getId();
            AttendanceRecord current = snapshot.get(employeeId);
            if (current == null || record.getDate().isAfter(current.getDate())) {
                snapshot.put(employeeId, record);
            }
        }

        List<WeeklyTrendDTO> trend = new ArrayList<>();
        int totalEmployees = employees.size();
        YearMonth startMonth = YearMonth.from(rangeSelection.getStartDate());
        YearMonth endMonth = YearMonth.from(rangeSelection.getEndDate());

        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            Map<Long, AttendanceRecord> monthlyRecords = recordsByMonth.getOrDefault(month, Collections.emptyMap());
            trend.add(buildTrendPoint(month.format(MONTH_FORMATTER), totalEmployees, monthlyRecords));
        }

        return trend;
    }

    private WeeklyTrendDTO buildTrendPoint(String label, int totalEmployees, Map<Long, AttendanceRecord> records) {
        int present = 0;
        int late = 0;
        int workFromHome = 0;
        int onLeave = 0;
        int halfDay = 0;

        for (AttendanceRecord record : records.values()) {
            String state = normalizeState(record.getState());
            if (null != state) switch (state) {
                case "PRESENT" -> present++;
                case "LATE" -> late++;
                case "WORK_FROM_HOME" -> workFromHome++;
                case "LEAVE" -> onLeave++;
                case "HALF_DAY" -> halfDay++;
                default -> {
                }
            }
        }

        int accounted = present + late + workFromHome + onLeave + halfDay;
        int absent = Math.max(0, totalEmployees - accounted);
        return new WeeklyTrendDTO(label, present, absent, late);
    }

    private DashboardRangeSelection resolveRange(LocalDate targetDate, String range) {
        String normalizedRange = normalizeOptional(range);
        if (normalizedRange == null) {
            return new DashboardRangeSelection("DAY", targetDate, targetDate, targetDate.minusDays(4));
        }

        String value = normalizedRange.toUpperCase(Locale.ENGLISH);
        return switch (value) {
            case "DAY", "DAILY" -> new DashboardRangeSelection("DAY", targetDate, targetDate, targetDate.minusDays(4));
            case "WEEK", "WEEKLY" -> new DashboardRangeSelection("WEEK", targetDate.minusDays(6), targetDate,
                    targetDate.minusDays(6));
            case "MONTH", "MONTHLY" -> new DashboardRangeSelection("MONTH", targetDate.withDayOfMonth(1), targetDate,
                    targetDate.withDayOfMonth(1));
            case "YEAR", "YEARLY" -> new DashboardRangeSelection("YEAR", targetDate.withDayOfYear(1), targetDate,
                    targetDate.withDayOfYear(1));
            default -> throw new BadRequestException("Invalid range. Supported values: day, week, month, year.");
        };
    }

    private String formatDayLabel(LocalDate day, DashboardRangeSelection rangeSelection) {
        if ("DAY".equals(rangeSelection.getLabel()) || "WEEK".equals(rangeSelection.getLabel())) {
            return day.format(DAY_FORMATTER);
        }
        return day.getDayOfMonth() + " " + day.format(MONTH_FORMATTER);
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

    private String formatRecordTime(LocalDateTime timestamp, boolean spansMultipleDays) {
        if (timestamp == null) {
            return null;
        }
        return spansMultipleDays ? timestamp.format(DATE_TIME_FORMATTER) : timestamp.format(TIME_FORMATTER);
    }

    private String formatActivityTime(LocalDateTime timestamp, boolean spansMultipleDays) {
        return formatRecordTime(timestamp, spansMultipleDays);
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

    private static class DepartmentAggregate {
        private int present;
        private int late;
        private int total;
    }

    private static class DashboardRangeSelection {
        private final String label;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final LocalDate trendStartDate;

        private DashboardRangeSelection(String label, LocalDate startDate, LocalDate endDate, LocalDate trendStartDate) {
            this.label = label;
            this.startDate = startDate;
            this.endDate = endDate;
            this.trendStartDate = trendStartDate;
        }

        private String getLabel() {
            return label;
        }

        private LocalDate getStartDate() {
            return startDate;
        }

        private LocalDate getEndDate() {
            return endDate;
        }

        private LocalDate getTrendStartDate() {
            return trendStartDate;
        }

        private boolean spansMultipleDays() {
            return !startDate.equals(endDate);
        }
    }
}
