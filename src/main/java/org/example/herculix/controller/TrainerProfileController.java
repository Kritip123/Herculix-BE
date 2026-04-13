package org.example.herculix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.herculix.entity.Trainer;
import org.example.herculix.model.request.TrainerCertificateCreateRequest;
import org.example.herculix.model.request.TrainerCertificateUploadUrlRequest;
import org.example.herculix.model.request.TrainerMediaCreateRequest;
import org.example.herculix.model.request.TrainerMediaUploadUrlRequest;
import org.example.herculix.model.request.TrainerProfileImageUploadUrlRequest;
import org.example.herculix.model.request.TrainerProfileCreateRequest;
import org.example.herculix.model.request.TrainerProfileUpdateRequest;
import org.example.herculix.model.response.TrainerProfileResponse;
import org.example.herculix.model.response.TrainerUploadUrlResponse;
import org.example.herculix.service.TrainerCertificateService;
import org.example.herculix.service.TrainerProfileImageService;
import org.example.herculix.service.TrainerProfileMediaService;
import org.example.herculix.service.TrainerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/trainer")
@RequiredArgsConstructor
@Tag(name = "Trainer Profile", description = "Trainer profile onboarding APIs")
public class TrainerProfileController {

    private final TrainerProfileService trainerProfileService;
    private final TrainerCertificateService trainerCertificateService;
    private final TrainerProfileMediaService trainerProfileMediaService;
    private final TrainerProfileImageService trainerProfileImageService;

    @GetMapping("/profile")
    @Operation(summary = "Get current trainer profile")
    public ResponseEntity<TrainerProfileResponse> getProfile(@AuthenticationPrincipal Trainer trainer) {
        return ResponseEntity.ok(trainerProfileService.getProfile(trainer.getId()));
    }

    @PostMapping("/profile")
    @Operation(summary = "Create trainer profile draft")
    public ResponseEntity<TrainerProfileResponse> createProfile(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                trainerProfileService.createProfile(trainer.getId(), request)
        );
    }

    @PostMapping("/profile-image/upload-url")
    @Operation(summary = "Get signed URL for profile image upload")
    public ResponseEntity<TrainerUploadUrlResponse> getProfileImageUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileImageUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerProfileImageService.generateProfileImageUploadUrl(trainer.getId(), request));
    }

    @PostMapping("/cover-image/upload-url")
    @Operation(summary = "Get signed URL for cover image upload")
    public ResponseEntity<TrainerUploadUrlResponse> getCoverImageUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileImageUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerProfileImageService.generateCoverImageUploadUrl(trainer.getId(), request));
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update trainer profile draft")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(trainerProfileService.updateProfile(trainer.getId(), request));
    }

    @PostMapping("/certificates/upload-url")
    @Operation(summary = "Get signed URL for certificate upload")
    public ResponseEntity<TrainerUploadUrlResponse> getCertificateUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerCertificateUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerCertificateService.generateUploadUrl(trainer.getId(), request));
    }

    @PostMapping("/certificates")
    @Operation(summary = "Register certificate metadata")
    public ResponseEntity<Map<String, String>> createCertificate(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerCertificateCreateRequest request
    ) {
        String id = trainerCertificateService.createCertificate(trainer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @DeleteMapping("/certificates/{id}")
    @Operation(summary = "Delete certificate")
    public ResponseEntity<Void> deleteCertificate(
            @AuthenticationPrincipal Trainer trainer,
            @PathVariable String id
    ) {
        trainerCertificateService.deleteCertificate(trainer.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/media/upload-url")
    @Operation(summary = "Get signed URL for media upload")
    public ResponseEntity<TrainerUploadUrlResponse> getMediaUploadUrl(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerMediaUploadUrlRequest request
    ) {
        return ResponseEntity.ok(trainerProfileMediaService.generateUploadUrl(trainer.getId(), request));
    }

    @PostMapping("/media")
    @Operation(summary = "Register media metadata")
    public ResponseEntity<Map<String, String>> createMedia(
            @AuthenticationPrincipal Trainer trainer,
            @Valid @RequestBody TrainerMediaCreateRequest request
    ) {
        String id = trainerProfileMediaService.createMedia(trainer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @DeleteMapping("/media/{id}")
    @Operation(summary = "Delete media")
    public ResponseEntity<Void> deleteMedia(
            @AuthenticationPrincipal Trainer trainer,
            @PathVariable String id
    ) {
        trainerProfileMediaService.deleteMedia(trainer.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit trainer profile for approval")
    public ResponseEntity<Map<String, String>> submitProfile(@AuthenticationPrincipal Trainer trainer) {
        TrainerProfileResponse response = trainerProfileService.submitProfile(trainer.getId());
        return ResponseEntity.ok(Map.of("status", response.getStatus()));
    }

    @GetMapping("/status")
    @Operation(summary = "Get trainer profile status")
    public ResponseEntity<Map<String, String>> getStatus(@AuthenticationPrincipal Trainer trainer) {
        String status = trainer.getStatus() != null ? trainer.getStatus().name().toLowerCase() : "draft";
        return ResponseEntity.ok(Map.of("status", status));
    }
}
