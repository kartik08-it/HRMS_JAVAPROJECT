package com.kartik.hrms.dto;

import java.time.LocalDate;

public class AbsenteeDTO {
    private String name;
    private String department;
    private LocalDate date;
    private String reason;
    private boolean approved;

    public AbsenteeDTO(String name, String department, LocalDate date, String reason, boolean approved) {
        this.name = name;
        this.department = department;
        this.date = date;
        this.reason = reason;
        this.approved = approved;
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

    public String getReason() {
        return reason;
    }

    public boolean isApproved() {
        return approved;
    }
}
