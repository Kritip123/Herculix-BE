package org.example.nexfit.repository;

import org.example.nexfit.entity.TrainerMedia;
import org.example.nexfit.entity.enums.MediaType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerMediaRepository extends MongoRepository<TrainerMedia, String> {

    List<TrainerMedia> findByTrainerIdOrderByDisplayOrderAsc(String trainerId);

    List<TrainerMedia> findByTrainerIdAndTypeOrderByDisplayOrderAsc(String trainerId, MediaType type);

    // Get videos ordered by isFeatured (true first) then displayOrder for selecting featured video
    List<TrainerMedia> findByTrainerIdAndTypeOrderByIsFeaturedDescDisplayOrderAsc(String trainerId, MediaType type);

    List<TrainerMedia> findByTrainerIdInOrderByDisplayOrderAsc(List<String> trainerIds);

    long countByTrainerId(String trainerId);

    void deleteByTrainerId(String trainerId);

    List<TrainerMedia> findByTrainerIdAndIsDemoTrue(String trainerId);

    Optional<TrainerMedia> findByS3Key(String s3Key);
}
