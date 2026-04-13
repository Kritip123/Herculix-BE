package org.example.herculix.service;

import org.example.herculix.model.request.TrainerAuthRequest;
import org.example.herculix.model.response.TrainerAuthResponse;

public interface TrainerAuthService {

    TrainerAuthResponse register(TrainerAuthRequest.RegisterRequest request);

    TrainerAuthResponse login(TrainerAuthRequest.LoginRequest request);

    void logout(String token);
}
