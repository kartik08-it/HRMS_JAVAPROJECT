package com.kartik.hrms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kartik.hrms.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find all non-deleted employees
    List<Employee> findByIsDeletedFalse();

    List<Employee> findByUserIdAndIsDeletedFalse(Long userId);

    List<Employee> findByUserIdAndIsDeletedFalseAndDepartment(Long userId, String department);

    // Find by department
    List<Employee> findByDepartment(String department);

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByEmailHash(String emailHash);

    Optional<Employee> findByPhoneHash(String phoneHash);

    Page<Employee> findByUsernameContainingIgnoreCaseAndIsDeletedFalse(
            String username,
            Pageable pageable
    );
    Page<Employee> findByIsDeletedFalse(Pageable pageable);
}
