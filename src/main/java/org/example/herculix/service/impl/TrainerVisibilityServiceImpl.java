package org.example.herculix.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.herculix.entity.Trainer;
import org.example.herculix.entity.enums.TrainerStatus;
import org.example.herculix.service.LaunchDarklyService;
import org.example.herculix.service.TrainerVisibilityService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerVisibilityServiceImpl implements TrainerVisibilityService {

    private final LaunchDarklyService launchDarklyService;

    @Override
    public boolean isVisibleToUsers(Trainer trainer) {
        if (trainer == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(trainer.getIsActive())) {
            return false;
        }
        return launchDarklyService.isTrainerVerified(trainer.getEmail());
    }
}
