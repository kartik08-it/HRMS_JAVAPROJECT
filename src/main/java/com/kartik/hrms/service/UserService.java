package com.kartik.hrms.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kartik.hrms.entity.User;
import com.kartik.hrms.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Constructor Injection (Best Practice)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Create User
    public User createUser(User user, Long createdBy) {

        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(1);
        user.setIsDeleted(false);
        user.setCreatedBy(createdBy);

        return userRepository.save(user);
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