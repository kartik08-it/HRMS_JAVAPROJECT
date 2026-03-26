package com.kartik.hrms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmployeeResponseDTO {
    private Long id;
    @JsonProperty("profile_image")
    private String profileImage;
    private String employeeCode;
    private String name;
    private String email;
    private String phone;
    private String joinDate;
    private String avatar;
    private String department;
    private String position;
    private String status;
    private String salary;
    private String manager;
    private String location;

    public EmployeeResponseDTO(Long id, String profileImage, String employeeCode, String name, String email, String phone, String department,
            String position, String joinDate, String status, String avatar, String salary, String manager,
            String location) {
        this.id = id;
        this.profileImage = profileImage;
        this.employeeCode = employeeCode;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.name = name;
        this.joinDate = joinDate;
        this.avatar = avatar;
        this.position = position;
        this.status = status;
        this.salary = salary;
        this.manager = manager;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getStatus() {
        return status;
    }

    public String getSalary() {
        return salary;
    }

    public String getManager() {
        return manager;
    }

    public String getLocation() {
        return location;
    }
}
