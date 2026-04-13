package org.example.herculix.repository;

import org.example.herculix.entity.TrainerCertificate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerCertificateRepository extends MongoRepository<TrainerCertificate, String> {

    List<TrainerCertificate> findByTrainerIdOrderByIssuedDateDesc(String trainerId);

    long countByTrainerId(String trainerId);
}
