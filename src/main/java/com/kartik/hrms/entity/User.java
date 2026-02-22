package com.kartik.hrms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Login credentials
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // Role: ADMIN / HR / EMPLOYEE
    @Column(nullable = false)
    private String role;

    @Column(unique = true)
    private String email;

    // Account status: 0=inactive,1=active,2=blocked
    @Column(nullable = false)
    private Integer status = 1;

    // Soft delete
    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    // Audit fields
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;

    // Constructors
      public User() {
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = 1;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
// ===== Email =====
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

// ===== Status =====
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

// ===== isDeleted =====
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

// ===== deletedAt =====
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

// ===== createdAt =====
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

// ===== updatedAt =====
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

// ===== createdBy =====
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

// ===== updatedBy =====
    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

}
