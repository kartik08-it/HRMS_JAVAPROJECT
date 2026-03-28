package com.kartik.hrms.dto;

public class AttendanceDepartmentDTO {
    private String name;
    private int present;
    private int absent;
    private int late;
    private int total;

    public AttendanceDepartmentDTO(String name, int present, int absent, int late, int total) {
        this.name = name;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.total = total;
    }

    public String getName() {
        return name;
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

    public int getTotal() {
        return total;
    }
}
