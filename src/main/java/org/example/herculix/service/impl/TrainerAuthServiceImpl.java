package org.example.herculix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herculix.entity.Trainer;
import org.example.herculix.entity.enums.TrainerStatus;
import org.example.herculix.exception.BusinessException;
import org.example.herculix.model.request.TrainerAuthRequest;
import org.example.herculix.model.response.TrainerAuthResponse;
import org.example.herculix.repository.TrainerRepository;
import org.example.herculix.security.JwtService;
import org.example.herculix.service.TrainerAuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerAuthServiceImpl implements TrainerAuthService {

    private static final String TOKEN_TYPE_TRAINER = "trainer";

    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public TrainerAuthResponse register(TrainerAuthRequest.RegisterRequest request) {
        if (trainerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        Trainer trainer = Trainer.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .city(request.getCity())
                .status(TrainerStatus.DRAFT)
                .pricingMonthlySubscriptionUsd(BigDecimal.ONE)
                .hasDiscoverVideo(false)
                .profileInitialized(false)
                .isActive(true)
                .build();

        trainer = trainerRepository.save(trainer);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("trainerId", trainer.getId());
        extraClaims.put("tokenType", TOKEN_TYPE_TRAINER);

        String jwtToken = jwtService.generateToken(extraClaims, trainer);
        String refreshToken = jwtService.generateRefreshToken(trainer);

        return TrainerAuthResponse.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .email(trainer.getEmail())
                .role("TRAINER")
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Override
    public TrainerAuthResponse login(TrainerAuthRequest.LoginRequest request) {
        Trainer trainer = trainerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Trainer not found"));

        if (!Boolean.TRUE.equals(trainer.getIsActive())) {
            throw new BusinessException("Trainer account is inactive");
        }

        if (trainer.getPassword() == null || trainer.getPassword().isBlank()) {
            throw new BusinessException("Trainer account is not configured for login");
        }

        if (!passwordEncoder.matches(request.getPassword(), trainer.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("trainerId", trainer.getId());
        extraClaims.put("tokenType", TOKEN_TYPE_TRAINER);

        String jwtToken = jwtService.generateToken(extraClaims, trainer);
        String refreshToken = jwtService.generateRefreshToken(trainer);

        return TrainerAuthResponse.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .email(trainer.getEmail())
                .role("TRAINER")
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Override
    public void logout(String token) {
        // Stateless logout; client removes token
        log.info("Trainer logged out");
    }
}
