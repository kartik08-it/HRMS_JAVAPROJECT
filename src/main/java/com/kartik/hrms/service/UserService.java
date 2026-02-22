package com.kartik.hrms.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kartik.hrms.dto.UserRequestDTO;
import com.kartik.hrms.dto.UserResponseDTO;
import com.kartik.hrms.entity.User;
import com.kartik.hrms.exception.BadRequestException;
import com.kartik.hrms.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Constructor Injection (Best Practice)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Create User
    public UserResponseDTO createUser(UserRequestDTO request, Long createdBy) {

        // Check duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(1);
        user.setIsDeleted(false);
        user.setCreatedBy(createdBy);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // Get User by Username
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Soft Delete User
    public void deleteUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setIsDeleted(true);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
