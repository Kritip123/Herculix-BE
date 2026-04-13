package org.example.herculix.service;

import org.example.herculix.model.request.TrainerMediaCreateRequest;
import org.example.herculix.model.request.TrainerMediaUploadUrlRequest;
import org.example.herculix.model.response.TrainerUploadUrlResponse;

public interface TrainerProfileMediaService {

    TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerMediaUploadUrlRequest request);

    String createMedia(String trainerId, TrainerMediaCreateRequest request);

    void deleteMedia(String trainerId, String mediaId);
}
