package com.kartik.hrms.dto;

public class LateArrivalDTO {
    private String name;
    private String department;
    private String checkIn;
    private String delay;

    public LateArrivalDTO(String name, String department, String checkIn, String delay) {
        this.name = name;
        this.department = department;
        this.checkIn = checkIn;
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getDelay() {
        return delay;
    }
}
