package com.kartik.hrms.dto;

import java.time.LocalDate;

public class LateArrivalDTO {
    private String name;
    private String department;
    private LocalDate date;
    private String checkIn;
    private String delay;

    public LateArrivalDTO(String name, String department, LocalDate date, String checkIn, String delay) {
        this.name = name;
        this.department = department;
        this.date = date;
        this.checkIn = checkIn;
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getDelay() {
        return delay;
    }
}
