package com.uber.springserver.services;

import com.uber.springserver.models.BlacklistToken;
import com.uber.springserver.repositories.BlacklistTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BlacklistService {

    private final BlacklistTokenRepository blacklistTokenRepository;

    public BlacklistService(BlacklistTokenRepository blacklistTokenRepository) {
        this.blacklistTokenRepository = blacklistTokenRepository;
    }

    public void add(String token) {
        BlacklistToken blacklistToken = new BlacklistToken();
        blacklistToken.setToken(token);
        blacklistToken.setCreatedAt(Instant.now());
        blacklistTokenRepository.save(blacklistToken);
    }

    public boolean isBlacklisted(String token) {
        return blacklistTokenRepository.existsByToken(token);
    }
}
