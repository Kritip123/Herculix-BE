package org.example.herculix.repository;

import org.example.herculix.entity.TrainerAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerAvailabilityRepository extends MongoRepository<TrainerAvailability, String> {
    
    Optional<TrainerAvailability> findByTrainerId(String trainerId);
}
