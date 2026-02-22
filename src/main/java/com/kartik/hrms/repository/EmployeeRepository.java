package com.kartik.hrms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kartik.hrms.entity.Employee;
import com.kartik.hrms.entity.User;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find employee by linked user
    Optional<Employee> findByUser(User user);

    // Find all non-deleted employees
    List<Employee> findByIsDeletedFalse();

    // Find by department
    List<Employee> findByDepartment(String department);
}