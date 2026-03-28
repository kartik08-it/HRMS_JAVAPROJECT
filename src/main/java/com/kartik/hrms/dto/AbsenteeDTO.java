package com.kartik.hrms.dto;

public class AbsenteeDTO {
    private String name;
    private String department;
    private String reason;
    private boolean approved;

    public AbsenteeDTO(String name, String department, String reason, boolean approved) {
        this.name = name;
        this.department = department;
        this.reason = reason;
        this.approved = approved;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getReason() {
        return reason;
    }

    public boolean isApproved() {
        return approved;
    }
}
