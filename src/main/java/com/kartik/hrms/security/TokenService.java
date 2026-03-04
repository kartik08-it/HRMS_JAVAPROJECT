package com.kartik.hrms.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kartik.hrms.entity.AuthToken;
import com.kartik.hrms.entity.User;
import com.kartik.hrms.repository.AuthTokenRepository;

@Service
public class TokenService {

    private final AuthTokenRepository authTokenRepository;
    private final long tokenExpiryHours;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(
            AuthTokenRepository authTokenRepository,
            @Value("${app.auth.token-expiry-hours:12}") long tokenExpiryHours) {
        this.authTokenRepository = authTokenRepository;
        this.tokenExpiryHours = tokenExpiryHours;
    }

    public String generateToken(User user) {
        authTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        String token = createRandomToken();
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUser(user);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(tokenExpiryHours));
        authToken.setRevoked(false);
        authTokenRepository.save(authToken);
        return token;
    }

    public Optional<User> validateAndGetUser(String token) {
        return authTokenRepository.findByToken(token)
                .filter(stored -> !stored.getRevoked())
                .filter(stored -> stored.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(AuthToken::getUser)
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .filter(user -> user.getStatus() != null && user.getStatus() == 1);
    }

    public void revokeToken(String token) {
        authTokenRepository.findByToken(token).ifPresent(stored -> {
            stored.setRevoked(true);
            authTokenRepository.save(stored);
        });
    }

    private String createRandomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
