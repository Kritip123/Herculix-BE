package org.example.herculix.service;

import org.example.herculix.model.request.AuthRequest;
import org.example.herculix.model.response.AuthResponse;

public interface AuthenticationService {

    boolean emailExists(String email);
    
    AuthResponse register(AuthRequest.RegisterRequest request);
    
    AuthResponse login(AuthRequest.LoginRequest request);
    
    AuthResponse refreshToken(AuthRequest.RefreshTokenRequest request);
    
    void logout(String token);
    
    void forgotPassword(AuthRequest.ForgotPasswordRequest request);
    
    void resetPassword(AuthRequest.ResetPasswordRequest request);
}
