package com.uber.springserver.repositories;

import com.uber.springserver.models.BlacklistToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlacklistTokenRepository extends MongoRepository<BlacklistToken, String> {
    boolean existsByToken(String token);
}
