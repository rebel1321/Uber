package com.uber.springserver.repositories;

import com.uber.springserver.models.Captain;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CaptainRepository extends MongoRepository<Captain, String> {
    Optional<Captain> findByEmail(String email);
}
