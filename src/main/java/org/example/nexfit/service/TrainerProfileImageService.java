package org.example.nexfit.service;

import org.example.nexfit.model.request.TrainerProfileImageUploadUrlRequest;
import org.example.nexfit.model.response.TrainerUploadUrlResponse;

public interface TrainerProfileImageService {

    TrainerUploadUrlResponse generateProfileImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request);

    TrainerUploadUrlResponse generateCoverImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request);
}
