package com.kartik.hrms.dto;

public class AttendanceSummaryDTO {
    private int totalEmployees;
    private int present;
    private int absent;
    private int late;
    private int workFromHome;
    private int onLeave;
    private int halfDay;

    public AttendanceSummaryDTO(int totalEmployees, int present, int absent, int late,
            int workFromHome, int onLeave, int halfDay) {
        this.totalEmployees = totalEmployees;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.workFromHome = workFromHome;
        this.onLeave = onLeave;
        this.halfDay = halfDay;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public int getPresent() {
        return present;
    }

    public int getAbsent() {
        return absent;
    }

    public int getLate() {
        return late;
    }

    public int getWorkFromHome() {
        return workFromHome;
    }

    public int getOnLeave() {
        return onLeave;
    }

    public int getHalfDay() {
        return halfDay;
    }
}
