package org.example.herculix.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herculix.entity.Trainer;
import org.example.herculix.entity.TrainerMedia;
import org.example.herculix.entity.enums.MediaType;
import org.example.herculix.exception.BusinessException;
import org.example.herculix.repository.TrainerMediaRepository;
import org.example.herculix.repository.TrainerRepository;
import org.example.herculix.service.S3Service;
import org.example.herculix.service.TrainerMediaSyncService;
import org.example.herculix.util.S3KeyPrefixer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerMediaSyncServiceImpl implements TrainerMediaSyncService {

    private final S3Service s3Service;
    private final S3KeyPrefixer s3KeyPrefixer;
    private final TrainerMediaRepository mediaRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public Map<String, Object> syncFromS3() {
        String basePrefix = s3KeyPrefixer.applyPrefix("trainers/");
        if (basePrefix == null || basePrefix.isBlank()) {
            throw new BusinessException("Invalid S3 prefix for trainer media sync");
        }

        List<String> keys = s3Service.listKeys(basePrefix);
        int processed = 0;
        int created = 0;
        int skipped = 0;
        int invalid = 0;
        Map<String, Integer> displayOrderMap = new HashMap<>();
        Map<String, Boolean> hasVideoMap = new HashMap<>();
        Set<String> trainersWithVideo = new HashSet<>();

        for (String key : keys) {
            processed++;
            if (key == null || key.endsWith("/")) {
                skipped++;
                continue;
            }

            ParsedKey parsed = parseKey(basePrefix, key);
            if (parsed == null) {
                invalid++;
                continue;
            }

            if (mediaRepository.findByS3Key(parsed.s3Key).isPresent()) {
                skipped++;
                continue;
            }

            int displayOrder = displayOrderMap.computeIfAbsent(parsed.trainerId,
                    id -> (int) mediaRepository.countByTrainerId(id));

            boolean hasVideo = hasVideoMap.computeIfAbsent(parsed.trainerId,
                    id -> !mediaRepository.findByTrainerIdAndTypeOrderByIsFeaturedDescDisplayOrderAsc(id, MediaType.VIDEO).isEmpty());

            boolean isFeatured = parsed.type == MediaType.VIDEO && !hasVideo;
            if (isFeatured) {
                hasVideoMap.put(parsed.trainerId, true);
            }

            TrainerMedia media = TrainerMedia.builder()
                    .trainerId(parsed.trainerId)
                    .type(parsed.type)
                    .s3Key(parsed.s3Key)
                    .mediaUrl(s3Service.getMediaUrl(parsed.s3Key))
                    .thumbnailUrl(parsed.type == MediaType.IMAGE ? s3Service.getMediaUrl(parsed.s3Key) : null)
                    .displayOrder(displayOrder)
                    .isDemo(false)
                    .isFeatured(isFeatured)
                    .build();

            mediaRepository.save(media);
            displayOrderMap.put(parsed.trainerId, displayOrder + 1);
            if (parsed.type == MediaType.VIDEO) {
                trainersWithVideo.add(parsed.trainerId);
            }
            created++;
        }

        if (!trainersWithVideo.isEmpty()) {
            List<Trainer> trainers = trainerRepository.findAllById(trainersWithVideo);
            trainers.forEach(trainer -> trainer.setHasDiscoverVideo(true));
            trainerRepository.saveAll(trainers);
        }

        log.info("Trainer media sync complete. processed={}, created={}, skipped={}, invalid={}",
                processed, created, skipped, invalid);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processed", processed);
        result.put("created", created);
        result.put("skipped", skipped);
        result.put("invalid", invalid);
        result.put("updatedDiscoverVideo", trainersWithVideo.size());
        return result;
    }

    private ParsedKey parseKey(String basePrefix, String s3Key) {
        String normalized = s3Key.startsWith(basePrefix)
                ? s3Key.substring(basePrefix.length())
                : null;
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        String[] parts = normalized.split("/");
        if (parts.length < 4) {
            return null;
        }

        String trainerId = parts[0];
        String section = parts[1];
        String typeFolder = parts[2];
        if (!"media".equals(section)) {
            return null;
        }

        MediaType type = switch (typeFolder.toLowerCase()) {
            case "video" -> MediaType.VIDEO;
            case "image" -> MediaType.IMAGE;
            case "transformation" -> MediaType.TRANSFORMATION;
            default -> null;
        };
        if (type == null) {
            return null;
        }

        return new ParsedKey(trainerId, type, s3Key);
    }

    private record ParsedKey(String trainerId, MediaType type, String s3Key) {}
}
