package org.example.herculix.service;

import org.example.herculix.model.request.TrainerProfileImageUploadUrlRequest;
import org.example.herculix.model.response.TrainerUploadUrlResponse;

public interface TrainerProfileImageService {

    TrainerUploadUrlResponse generateProfileImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request);

    TrainerUploadUrlResponse generateCoverImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request);
}
