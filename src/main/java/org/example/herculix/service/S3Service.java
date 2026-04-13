package org.example.herculix.service;

import org.example.herculix.model.response.UploadUrlResponse;

public interface S3Service {

    boolean isEnabled();

    UploadUrlResponse generatePresignedUploadUrl(String key, String contentType);

    String getMediaUrl(String s3Key);

    void deleteObject(String s3Key);

    boolean objectExists(String s3Key);

    void uploadObject(String s3Key, byte[] content, String contentType);

    java.util.List<String> listKeys(String prefix);
}
