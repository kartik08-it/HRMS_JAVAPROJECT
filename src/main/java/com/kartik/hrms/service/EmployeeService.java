package com.kartik.hrms.service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        employee.setEmployeeCode(normalizeOptional(request.getEmployeeCode()));
        employee.setFullName(normalizeOptional(request.getFullName()));
        employee.setStatus(normalizeStatus(request.getStatus()));
        employee.setAvatar(normalizeOptional(request.getAvatar()));
        employee.setManager(normalizeOptional(request.getManager()));
        employee.setLocation(normalizeOptional(request.getLocation()));
        employee.setCreatedAt(LocalDateTime.now());
        employee.setIsDeleted(false);

        return mapToDTO(employeeRepository.save(employee));
    }

    public Page<EmployeeResponseDTO> getEmployees(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Employee> employeePage;

        if (search != null && !search.isBlank()) {
            employeePage = employeeRepository
                    .findByUsernameContainingIgnoreCaseAndIsDeletedFalse(search, pageable);
        } else {
            employeePage = employeeRepository.findByIsDeletedFalse(pageable);
        }

        return employeePage.map(this::mapToDTO);
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
        employee.setEmployeeCode(normalizeOptional(request.getEmployeeCode()));
        employee.setFullName(normalizeOptional(request.getFullName()));
        employee.setStatus(normalizeStatus(request.getStatus()));
        employee.setAvatar(normalizeOptional(request.getAvatar()));
        employee.setManager(normalizeOptional(request.getManager()));
        employee.setLocation(normalizeOptional(request.getLocation()));
        employee.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(employeeRepository.save(employee));
    }

    public void deleteEmployee(Long employeeId, AuthenticatedUser actor) {
        ensureAdminAccess(actor);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setIsDeleted(true);
        employee.setStatus("inactive");
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO mapToDTO(Employee employee) {
        return new EmployeeResponseDTO(
                resolveEmployeeId(employee),
                resolveName(employee),
                cryptoService.decrypt(employee.getEmail()),
                cryptoService.decrypt(employee.getPhone()),
                employee.getDepartment(),
                resolvePosition(employee),
                resolveJoinDate(employee),
                resolveStatus(employee),
                resolveAvatar(employee),
                formatSalary(employee.getSalary()),
                employee.getManager(),
                employee.getLocation());
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

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeOptional(status);
        if (normalized == null) {
            throw new BadRequestException("Status is required");
        }
        return normalized.toLowerCase();
    }

    private String resolveName(Employee employee) {
        String fullName = normalizeOptional(employee.getFullName());
        if (fullName != null) {
            return fullName;
        }
        return employee.getUsername();
    }

    private String resolveEmployeeId(Employee employee) {
        String employeeCode = normalizeOptional(employee.getEmployeeCode());
        if (employeeCode != null) {
            return employeeCode;
        }
        if (employee.getId() == null) {
            return null;
        }
        return String.format("EMP%03d", employee.getId());
    }

    private String resolveJoinDate(Employee employee) {
        if (employee.getJoiningDate() == null) {
            return null;
        }
        return employee.getJoiningDate().toLocalDate().toString();
    }

    private String resolveAvatar(Employee employee) {
        String avatar = normalizeOptional(employee.getAvatar());
        if (avatar != null) {
            return avatar;
        }
        String name = resolveName(employee);
        if (name == null || name.isBlank()) {
            return null;
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String resolvePosition(Employee employee) {
        return employee.getDesignation();
    }

    private String resolveStatus(Employee employee) {
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            return "inactive";
        }
        String status = normalizeOptional(employee.getStatus());
        return status == null ? "active" : status.toLowerCase();
    }

    private String formatSalary(Double salary) {
        if (salary == null) {
            return null;
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(salary);
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
