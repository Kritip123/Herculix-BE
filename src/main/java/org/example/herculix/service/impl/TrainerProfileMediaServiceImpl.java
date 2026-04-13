package org.example.herculix.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.herculix.entity.TrainerMedia;
import org.example.herculix.entity.enums.MediaType;
import org.example.herculix.exception.BusinessException;
import org.example.herculix.model.request.ConfirmUploadRequest;
import org.example.herculix.model.request.TrainerMediaCreateRequest;
import org.example.herculix.model.request.TrainerMediaUploadUrlRequest;
import org.example.herculix.model.request.UploadUrlRequest;
import org.example.herculix.model.response.TrainerUploadUrlResponse;
import org.example.herculix.service.S3Service;
import org.example.herculix.service.TrainerMediaService;
import org.example.herculix.service.TrainerProfileMediaService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerProfileMediaServiceImpl implements TrainerProfileMediaService {

    private final TrainerMediaService trainerMediaService;
    private final S3Service s3Service;

    @Override
    public TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerMediaUploadUrlRequest request) {
        validateMediaType(request.getType());

        UploadUrlRequest uploadRequest = UploadUrlRequest.builder()
                .filename(request.getFileName())
                .contentType(request.getContentType())
                .mediaType(request.getType())
                .fileSize(request.getSizeBytes())
                .build();

        var uploadResponse = trainerMediaService.generateUploadUrl(trainerId, uploadRequest);
        return TrainerUploadUrlResponse.builder()
                .uploadUrl(uploadResponse.getUploadUrl())
                .fileKey(uploadResponse.getS3Key())
                .fileUrl(s3Service.getMediaUrl(uploadResponse.getS3Key()))
                .build();
    }

    @Override
    public String createMedia(String trainerId, TrainerMediaCreateRequest request) {
        validateMediaType(request.getType());

        ConfirmUploadRequest confirmRequest = ConfirmUploadRequest.builder()
                .s3Key(request.getFileKey())
                .mediaType(request.getType())
                .thumbnailUrl(request.getThumbnailUrl())
                .durationSeconds(request.getDurationSec())
                .displayOrder(request.getOrder())
                .fileSizeBytes(request.getSizeBytes())
                .build();

        TrainerMedia media = trainerMediaService.confirmUpload(trainerId, confirmRequest);
        return media.getId();
    }

    @Override
    public void deleteMedia(String trainerId, String mediaId) {
        trainerMediaService.deleteMedia(trainerId, mediaId);
    }

    private void validateMediaType(MediaType type) {
        if (type == null || type == MediaType.TRANSFORMATION) {
            throw new BusinessException("Only image or video media types are supported");
        }
    }
}
