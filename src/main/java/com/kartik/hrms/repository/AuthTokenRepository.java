package com.kartik.hrms.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kartik.hrms.entity.AuthToken;
import com.kartik.hrms.entity.User;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime now);

    void deleteByUser(User user);
}
