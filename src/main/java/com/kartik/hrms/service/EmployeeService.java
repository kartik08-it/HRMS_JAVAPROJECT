package com.kartik.hrms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kartik.hrms.entity.Employee;
import com.kartik.hrms.entity.User;
import com.kartik.hrms.repository.EmployeeRepository;
import com.kartik.hrms.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
            UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // Create Employee
    public Employee createEmployee(Long userId, Employee employeeData) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();

        // Check if employee already exists for this user
        Optional<Employee> existingEmployee = employeeRepository.findByUser(user);

        if (existingEmployee.isPresent()) {
            throw new RuntimeException("Employee profile already exists for this user");
        }

        employeeData.setUser(user);
        employeeData.setCreatedAt(LocalDateTime.now());
        employeeData.setIsDeleted(false);

        return employeeRepository.save(employeeData);
    }

    // Get all active employees
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByIsDeletedFalse();
    }

    // Soft delete employee
    public void deleteEmployee(Long employeeId) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setIsDeleted(true);
            employee.setDeletedAt(LocalDateTime.now());
            employeeRepository.save(employee);
        }
    }
}
