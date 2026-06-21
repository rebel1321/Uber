package com.uber.springserver.services;

import com.uber.springserver.models.User;
import com.uber.springserver.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public void updateRefreshToken(String userId, String refreshToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
