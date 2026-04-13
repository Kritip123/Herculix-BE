package org.example.herculix.service;

import org.example.herculix.model.request.TrainerProfileCreateRequest;
import org.example.herculix.model.request.TrainerProfileUpdateRequest;
import org.example.herculix.model.response.TrainerProfileResponse;

public interface TrainerProfileService {

    TrainerProfileResponse getProfile(String trainerId);

    TrainerProfileResponse createProfile(String trainerId, TrainerProfileCreateRequest request);

    TrainerProfileResponse updateProfile(String trainerId, TrainerProfileUpdateRequest request);

    TrainerProfileResponse submitProfile(String trainerId);
}
