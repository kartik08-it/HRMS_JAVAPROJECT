package com.kartik.hrms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kartik.hrms.dto.EmployeeRequestDTO;
import com.kartik.hrms.dto.EmployeeResponseDTO;
import com.kartik.hrms.entity.Employee;
import com.kartik.hrms.entity.User;
import com.kartik.hrms.exception.BadRequestException;
import com.kartik.hrms.exception.ForbiddenException;
import com.kartik.hrms.exception.ResourceNotFoundException;
import com.kartik.hrms.repository.EmployeeRepository;
import com.kartik.hrms.repository.UserRepository;
import com.kartik.hrms.security.AuthenticatedUser;
import com.kartik.hrms.security.CryptoService;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    public EmployeeService(EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CryptoService cryptoService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cryptoService = cryptoService;
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request, AuthenticatedUser actor) {
        ensureAdminAccess(actor);
        validateCreateInput(request);

        User admin = userRepository.findById(actor.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        Employee employee = new Employee();
        employee.setUser(admin);
        employee.setUsername(request.getUsername().trim());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmail(cryptoService.encrypt(request.getEmail()));
        employee.setEmailHash(cryptoService.hmacSha256(request.getEmail()));
        employee.setPhone(cryptoService.encrypt(request.getPhone()));
        employee.setPhoneHash(cryptoService.hmacSha256(request.getPhone()));
        employee.setJoiningDate(request.getJoiningDate());
        employee.setProfileImage(request.getProfileImage());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setProfileType(normalizeProfileType(request.getProfileType()));
        employee.setCreatedAt(LocalDateTime.now());
        employee.setIsDeleted(false);

        return mapToDTO(employeeRepository.save(employee));
    }

    public List<EmployeeResponseDTO> getAllActiveEmployees() {
        return employeeRepository.findByIsDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDTO getEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new ResourceNotFoundException("Employee not found");
        }

        return mapToDTO(employee);
    }

    public EmployeeResponseDTO updateEmployee(Long employeeId, EmployeeRequestDTO request, AuthenticatedUser actor) {
        ensureAdminAccess(actor);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new ResourceNotFoundException("Employee not found");
        }

        validateUpdateInput(employeeId, request);

        employee.setUsername(request.getUsername().trim());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmail(cryptoService.encrypt(request.getEmail()));
        employee.setEmailHash(cryptoService.hmacSha256(request.getEmail()));
        employee.setPhone(cryptoService.encrypt(request.getPhone()));
        employee.setPhoneHash(cryptoService.hmacSha256(request.getPhone()));
        employee.setJoiningDate(request.getJoiningDate());
        employee.setProfileImage(request.getProfileImage());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());
        employee.setProfileType(normalizeProfileType(request.getProfileType()));
        employee.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(employeeRepository.save(employee));
    }

    public void deleteEmployee(Long employeeId, AuthenticatedUser actor) {
        ensureAdminAccess(actor);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setIsDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO mapToDTO(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getUser().getId(),
                employee.getUser().getUsername(),
                employee.getUsername(),
                cryptoService.decrypt(employee.getEmail()),
                cryptoService.decrypt(employee.getPhone()),
                employee.getJoiningDate(),
                employee.getProfileImage(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getSalary(),
                employee.getProfileType());
    }

    private String normalizeProfileType(String profileType) {
        if (profileType == null || profileType.isBlank()) {
            throw new BadRequestException("Profile type is required");
        }

        String normalized = profileType.trim().toUpperCase();
        if (!"HR".equals(normalized) && !"EMPLOYEE".equals(normalized)) {
            throw new BadRequestException("Profile type must be HR or EMPLOYEE");
        }
        return normalized;
    }

    private void ensureAdminAccess(AuthenticatedUser actor) {
        if (actor == null || actor.getRole() == null || !"ADMIN".equalsIgnoreCase(actor.getRole())) {
            throw new ForbiddenException("Only admin can perform this operation");
        }
    }

    private void validateCreateInput(EmployeeRequestDTO request) {
        if (employeeRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new BadRequestException("Employee username already exists");
        }

        String emailHash = cryptoService.hmacSha256(request.getEmail());
        if (employeeRepository.findByEmailHash(emailHash).isPresent()) {
            throw new BadRequestException("Employee email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String phoneHash = cryptoService.hmacSha256(request.getPhone());
            if (employeeRepository.findByPhoneHash(phoneHash).isPresent()) {
                throw new BadRequestException("Employee phone number already exists");
            }
        }
    }

    private void validateUpdateInput(Long employeeId, EmployeeRequestDTO request) {
        employeeRepository.findByUsername(request.getUsername().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(employeeId)) {
                throw new BadRequestException("Employee username already exists");
            }
        });

        String emailHash = cryptoService.hmacSha256(request.getEmail());
        employeeRepository.findByEmailHash(emailHash).ifPresent(existing -> {
            if (!existing.getId().equals(employeeId)) {
                throw new BadRequestException("Employee email already exists");
            }
        });

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String phoneHash = cryptoService.hmacSha256(request.getPhone());
            employeeRepository.findByPhoneHash(phoneHash).ifPresent(existing -> {
                if (!existing.getId().equals(employeeId)) {
                    throw new BadRequestException("Employee phone number already exists");
                }
            });
        }
    }
}
