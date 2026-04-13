package org.example.herculix.service;

import org.example.herculix.entity.TrainerMedia;
import org.example.herculix.model.request.ConfirmUploadRequest;
import org.example.herculix.model.request.UploadUrlRequest;
import org.example.herculix.model.response.UploadUrlResponse;

import java.util.List;

public interface TrainerMediaService {

    List<TrainerMedia> getTrainerMedia(String trainerId);

    UploadUrlResponse generateUploadUrl(String trainerId, UploadUrlRequest request);

    TrainerMedia confirmUpload(String trainerId, ConfirmUploadRequest request);

    void deleteMedia(String trainerId, String mediaId);

    List<TrainerMedia> getMediaForTrainers(List<String> trainerIds);
}
