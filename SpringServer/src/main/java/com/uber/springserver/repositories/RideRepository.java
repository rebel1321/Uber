package com.uber.springserver.repositories;

import com.uber.springserver.models.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RideRepository extends MongoRepository<Ride, String> {
    Optional<Ride> findByIdAndUser(String id, String user);
    Optional<Ride> findByIdAndCaptain(String id, String captain);
}
