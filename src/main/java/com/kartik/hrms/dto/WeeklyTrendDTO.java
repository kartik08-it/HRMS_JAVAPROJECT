package com.kartik.hrms.dto;

public class WeeklyTrendDTO {
    private String day;
    private int present;
    private int absent;
    private int late;

    public WeeklyTrendDTO(String day, int present, int absent, int late) {
        this.day = day;
        this.present = present;
        this.absent = absent;
        this.late = late;
    }

    public String getDay() {
        return day;
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
}
