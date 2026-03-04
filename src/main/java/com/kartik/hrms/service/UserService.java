package com.kartik.hrms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kartik.hrms.dto.LoginRequestDTO;
import com.kartik.hrms.dto.LoginResponseDTO;
import com.kartik.hrms.dto.UserRequestDTO;
import com.kartik.hrms.dto.UserResponseDTO;
import com.kartik.hrms.entity.User;
import com.kartik.hrms.exception.BadRequestException;
import com.kartik.hrms.exception.ResourceNotFoundException;
import com.kartik.hrms.exception.UnauthorizedException;
import com.kartik.hrms.repository.UserRepository;
import com.kartik.hrms.security.CryptoService;
import com.kartik.hrms.security.TokenService;

@Service
public class UserService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;
    private final TokenService tokenService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CryptoService cryptoService,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cryptoService = cryptoService;
        this.tokenService = tokenService;
    }

    public UserResponseDTO bootstrapAdmin(UserRequestDTO request) {
        if (userRepository.count() > 0) {
            throw new BadRequestException("Bootstrap admin is only allowed when no users exist");
        }
        User savedUser = createAdminUserEntity(request, null);
        return mapToDTO(savedUser);
    }

    public UserResponseDTO createUser(UserRequestDTO request, Long createdBy) {
        User savedUser = createAdminUserEntity(request, createdBy);
        return mapToDTO(savedUser);
    }

    public User createAdminUserEntity(UserRequestDTO request, Long createdBy) {
        validateCreateInput(request);

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(cryptoService.encrypt(request.getEmail()));
        user.setEmailHash(cryptoService.hmacSha256(request.getEmail()));
        user.setPhone(cryptoService.encrypt(request.getPhone()));
        user.setPhoneHash(cryptoService.hmacSha256(request.getPhone()));
        user.setRole(ADMIN_ROLE);
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(1);
        user.setIsDeleted(false);
        user.setCreatedBy(createdBy);

        return userRepository.save(user);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String emailHash = cryptoService.hmacSha256(request.getEmail());

        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (Boolean.TRUE.equals(user.getIsDeleted()) || user.getStatus() == null || user.getStatus() != 1) {
            throw new UnauthorizedException("User account is inactive or blocked");
        }

        if (!ADMIN_ROLE.equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedException("Only admin users can login in this phase");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = tokenService.generateToken(user);
        return new LoginResponseDTO(token, user.getId(), user.getUsername(), user.getRole());
    }

    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        tokenService.revokeToken(token);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findByIsDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User not found");
        }

        return mapToDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User not found");
        }

        validateUpdateInput(id, request);

        user.setUsername(request.getUsername().trim());
        user.setRole(ADMIN_ROLE);
        user.setEmail(cryptoService.encrypt(request.getEmail()));
        user.setEmailHash(cryptoService.hmacSha256(request.getEmail()));
        user.setPhone(cryptoService.encrypt(request.getPhone()));
        user.setPhoneHash(cryptoService.hmacSha256(request.getPhone()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void hardDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
    }

    private void validateCreateInput(UserRequestDTO request) {
        if (!ADMIN_ROLE.equalsIgnoreCase(request.getRole())) {
            throw new BadRequestException("Only ADMIN role is allowed in users table");
        }

        if (userRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        String emailHash = cryptoService.hmacSha256(request.getEmail());
        if (userRepository.findByEmailHash(emailHash).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String phoneHash = cryptoService.hmacSha256(request.getPhone());
            if (userRepository.findByPhoneHash(phoneHash).isPresent()) {
                throw new BadRequestException("Phone number already exists");
            }
        }
    }

    private void validateUpdateInput(Long id, UserRequestDTO request) {
        if (!ADMIN_ROLE.equalsIgnoreCase(request.getRole())) {
            throw new BadRequestException("Only ADMIN role is allowed in users table");
        }

        userRepository.findByUsername(request.getUsername().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Username already exists");
            }
        });

        String emailHash = cryptoService.hmacSha256(request.getEmail());
        userRepository.findByEmailHash(emailHash).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Email already exists");
            }
        });

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String phoneHash = cryptoService.hmacSha256(request.getPhone());
            userRepository.findByPhoneHash(phoneHash).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("Phone number already exists");
                }
            });
        }
    }

    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                cryptoService.decrypt(user.getEmail()),
                cryptoService.decrypt(user.getPhone()),
                user.getRole());
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Authorization token is required");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
