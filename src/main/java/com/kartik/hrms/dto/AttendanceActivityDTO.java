package com.kartik.hrms.dto;

public class AttendanceActivityDTO {
    private Long id;
    private String employee;
    private String action;
    private String time;
    private String status;
    private String department;

    public AttendanceActivityDTO(Long id, String employee, String action, String time,
            String status, String department) {
        this.id = id;
        this.employee = employee;
        this.action = action;
        this.time = time;
        this.status = status;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getEmployee() {
        return employee;
    }

    public String getAction() {
        return action;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getDepartment() {
        return department;
    }
}
