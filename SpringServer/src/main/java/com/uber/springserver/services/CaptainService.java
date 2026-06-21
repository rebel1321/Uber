package com.uber.springserver.services;

import com.uber.springserver.models.Captain;
import com.uber.springserver.models.Location;
import com.uber.springserver.repositories.CaptainRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class CaptainService {

    private final CaptainRepository captainRepository;

    public CaptainService(CaptainRepository captainRepository) {
        this.captainRepository = captainRepository;
    }

    public Captain create(Captain captain) {
        captain.setEmail(normalizeEmail(captain.getEmail()));
        captain.setCreatedAt(Instant.now());
        return captainRepository.save(captain);
    }

    public Optional<Captain> findByEmail(String email) {
        return captainRepository.findByEmail(normalizeEmail(email));
    }

    public Optional<Captain> findById(String id) {
        return captainRepository.findById(id);
    }

    public void updateRefreshToken(String captainId, String refreshToken) {
        captainRepository.findById(captainId).ifPresent(captain -> {
            captain.setRefreshToken(refreshToken);
            captainRepository.save(captain);
        });
    }

    public void updateLocation(String captainId, Location location) {
        captainRepository.findById(captainId).ifPresent(captain -> {
            captain.setLocation(location);
            captainRepository.save(captain);
        });
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
