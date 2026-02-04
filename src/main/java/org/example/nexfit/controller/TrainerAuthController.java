package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.exception.BusinessException;
import org.example.nexfit.model.request.TrainerAuthRequest;
import org.example.nexfit.model.response.TrainerAuthResponse;
import org.example.nexfit.service.TrainerAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainer/auth")
@RequiredArgsConstructor
@Tag(name = "Trainer Authentication", description = "Trainer authentication APIs")
public class TrainerAuthController {

    private final TrainerAuthService trainerAuthService;

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer")
    public ResponseEntity<TrainerAuthResponse> register(@Valid @RequestBody TrainerAuthRequest.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerAuthService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Trainer login")
    public ResponseEntity<TrainerAuthResponse> login(@Valid @RequestBody TrainerAuthRequest.LoginRequest request) {
        return ResponseEntity.ok(trainerAuthService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Trainer logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Invalid Authorization header");
        }
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        trainerAuthService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
