package org.example.nexfit.mapper;

import org.example.nexfit.entity.Trainer;
import org.example.nexfit.model.dto.TrainerDTO;
import org.example.nexfit.util.DistanceCalculator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrainerMapper {

    public TrainerDTO toDto(Trainer trainer) {
        return toDto(trainer, null, null);
    }

    public TrainerDTO toDto(Trainer trainer, Double userLat, Double userLng) {
        if (trainer == null) {
            return null;
        }

        TrainerDTO.TrainerDTOBuilder dtoBuilder = TrainerDTO.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .email(trainer.getEmail())
                .phone(trainer.getPhone())
                .profileImage(trainer.getProfileImage())
                .coverImage(trainer.getCoverImage())
                .specializations(trainer.getSpecializations() != null ? trainer.getSpecializations() : java.util.Set.of())
                .experience(trainer.getExperience())
                .rating(trainer.getRating())
                .reviewCount(trainer.getReviewCount())
                .hourlyRate(trainer.getHourlyRate())
                .bio(trainer.getBio())
                .certifications(trainer.getCertifications() != null ? trainer.getCertifications() : List.of())
                .instagramId(trainer.getInstagramId())
                .languages(trainer.getLanguages() != null ? trainer.getLanguages() : java.util.Set.of())
                .gymAffiliation(trainer.getGymAffiliation())
                .gallery(trainer.getGallery() != null
                        ? trainer.getGallery().stream().map(Trainer.TrainerImage::getUrl).toList()
                        : List.of())
                .location(TrainerDTO.LocationDTO.builder()
                        .latitude(trainer.getLatitude())
                        .longitude(trainer.getLongitude())
                        .address(trainer.getAddress())
                        .city(trainer.getCity())
                        .state(trainer.getState())
                        .country(trainer.getCountry())
                        .zipCode(trainer.getZipCode())
                        .build())
                .stats(TrainerDTO.StatsDTO.builder()
                        .totalClients(trainer.getTotalClients())
                        .transformations(trainer.getTransformations())
                        .sessionsCompleted(trainer.getSessionsCompleted())
                        .yearsActive(trainer.getYearsActive())
                        .build())
                .whatsapp(trainer.getWhatsapp())
                .website(trainer.getWebsite())
                .contactMethods(trainer.getContactMethods() != null
                        ? trainer.getContactMethods().stream()
                        .map(cm -> TrainerDTO.ContactMethodDTO.builder()
                                .type(cm.getType())
                                .value(cm.getValue())
                                .label(cm.getLabel())
                                .isPrimary(cm.getIsPrimary())
                                .build())
                        .toList()
                        : List.of());

        if (userLat != null && userLng != null && trainer.getLatitude() != null && trainer.getLongitude() != null) {
            double distance = DistanceCalculator.calculateDistance(
                    userLat, userLng,
                    trainer.getLatitude(), trainer.getLongitude()
            );
            dtoBuilder.distance(distance);
        }

        return dtoBuilder.build();
    }
}
