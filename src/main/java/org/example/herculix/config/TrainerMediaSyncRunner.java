package org.example.herculix.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herculix.service.TrainerMediaSyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerMediaSyncRunner implements ApplicationRunner {

    @Value("${media.sync.on-startup:false}")
    private boolean syncOnStartup;

    private final TrainerMediaSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        if (!syncOnStartup) {
            return;
        }

        try {
            log.info("Starting trainer media sync from S3...");
            var result = syncService.syncFromS3();
            log.info("Trainer media sync result: {}", result);
        } catch (Exception e) {
            log.error("Trainer media sync failed", e);
        }
    }
}
