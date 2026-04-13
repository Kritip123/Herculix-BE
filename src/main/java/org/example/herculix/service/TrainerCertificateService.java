package org.example.herculix.service;

import org.example.herculix.model.request.TrainerCertificateCreateRequest;
import org.example.herculix.model.request.TrainerCertificateUploadUrlRequest;
import org.example.herculix.model.response.TrainerUploadUrlResponse;

public interface TrainerCertificateService {

    TrainerUploadUrlResponse generateUploadUrl(String trainerId, TrainerCertificateUploadUrlRequest request);

    String createCertificate(String trainerId, TrainerCertificateCreateRequest request);

    void deleteCertificate(String trainerId, String certificateId);
}
