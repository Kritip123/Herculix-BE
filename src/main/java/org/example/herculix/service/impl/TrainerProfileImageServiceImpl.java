package org.example.herculix.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.herculix.exception.BusinessException;
import org.example.herculix.model.request.TrainerProfileImageUploadUrlRequest;
import org.example.herculix.model.response.TrainerUploadUrlResponse;
import org.example.herculix.model.response.UploadUrlResponse;
import org.example.herculix.service.S3Service;
import org.example.herculix.service.TrainerProfileImageService;
import org.example.herculix.util.S3KeyPrefixer;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerProfileImageServiceImpl implements TrainerProfileImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
    );

    private final S3Service s3Service;
    private final S3KeyPrefixer s3KeyPrefixer;

    @Override
    public TrainerUploadUrlResponse generateProfileImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request) {
        return generateUploadUrl(trainerId, "profile", request);
    }

    @Override
    public TrainerUploadUrlResponse generateCoverImageUploadUrl(String trainerId, TrainerProfileImageUploadUrlRequest request) {
        return generateUploadUrl(trainerId, "cover", request);
    }

    private TrainerUploadUrlResponse generateUploadUrl(
            String trainerId,
            String imageType,
            TrainerProfileImageUploadUrlRequest request
    ) {
        if (!s3Service.isEnabled()) {
            throw new BusinessException("S3 uploads are not enabled. Please configure AWS S3.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(request.getContentType())) {
            throw new BusinessException("Invalid content type. Only JPG, PNG, WEBP, or HEIC are allowed.");
        }

        String extension = getExtension(request.getFileName());
        String s3Key = String.format(
                "trainers/%s/profile/%s/%s%s",
                trainerId,
                imageType,
                UUID.randomUUID(),
                extension
        );
        s3Key = s3KeyPrefixer.applyPrefix(s3Key);

        UploadUrlResponse upload = s3Service.generatePresignedUploadUrl(s3Key, request.getContentType());
        return TrainerUploadUrlResponse.builder()
                .uploadUrl(upload.getUploadUrl())
                .fileKey(upload.getS3Key())
                .fileUrl(s3Service.getMediaUrl(upload.getS3Key()))
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
