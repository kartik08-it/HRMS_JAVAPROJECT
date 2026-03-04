package com.kartik.hrms.dto;

import java.time.LocalDateTime;

public class EmployeeResponseDTO {
    private Long id;
    private Long adminUserId;
    private String adminUsername;
    private String username;
    private String email;
    private String phone;
    private LocalDateTime joiningDate;
    private String profileImage;
    private String department;
    private String designation;
    private Double salary;
    private String profileType;

    public EmployeeResponseDTO(Long id, Long adminUserId, String adminUsername, String username, String email,
            String phone, LocalDateTime joiningDate, String profileImage, String department, String designation,
            Double salary, String profileType) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.adminUsername = adminUsername;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.joiningDate = joiningDate;
        this.profileImage = profileImage;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.profileType = profileType;
    }

    public Long getId() {
        return id;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getJoiningDate() {
        return joiningDate;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public Double getSalary() {
        return salary;
    }

    public String getProfileType() {
        return profileType;
    }
}
