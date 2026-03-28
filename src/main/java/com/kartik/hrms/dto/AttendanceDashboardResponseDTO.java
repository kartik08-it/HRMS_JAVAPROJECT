package com.kartik.hrms.dto;

import java.util.List;

public class AttendanceDashboardResponseDTO {
    private AttendanceSummaryDTO summary;
    private List<AttendanceDepartmentDTO> departments;
    private List<AttendanceActivityDTO> recentActivity;
    private List<LateArrivalDTO> lateArrivals;
    private List<AbsenteeDTO> absentees;
    private List<WeeklyTrendDTO> weeklyTrend;

    public AttendanceDashboardResponseDTO(AttendanceSummaryDTO summary,
            List<AttendanceDepartmentDTO> departments,
            List<AttendanceActivityDTO> recentActivity,
            List<LateArrivalDTO> lateArrivals,
            List<AbsenteeDTO> absentees,
            List<WeeklyTrendDTO> weeklyTrend) {
        this.summary = summary;
        this.departments = departments;
        this.recentActivity = recentActivity;
        this.lateArrivals = lateArrivals;
        this.absentees = absentees;
        this.weeklyTrend = weeklyTrend;
    }

    public AttendanceSummaryDTO getSummary() {
        return summary;
    }

    public List<AttendanceDepartmentDTO> getDepartments() {
        return departments;
    }

    public List<AttendanceActivityDTO> getRecentActivity() {
        return recentActivity;
    }

    public List<LateArrivalDTO> getLateArrivals() {
        return lateArrivals;
    }

    public List<AbsenteeDTO> getAbsentees() {
        return absentees;
    }

    public List<WeeklyTrendDTO> getWeeklyTrend() {
        return weeklyTrend;
    }
}
