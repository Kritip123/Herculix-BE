package org.example.herculix.service;

import org.example.herculix.entity.TrainerInteraction;
import org.example.herculix.entity.enums.InteractionType;
import org.example.herculix.model.request.InteractionRequest;

import java.util.List;

public interface TrainerInteractionService {

    TrainerInteraction recordInteraction(String userId, InteractionRequest request);

    List<TrainerInteraction> getUserInteractions(String userId);

    List<TrainerInteraction> getRecentInteractions(String userId, InteractionType type, int hoursBack);

    List<TrainerInteraction> getSkippedWithHighMatchScore(String userId, int minMatchScore);

    boolean hasRecentInteraction(String userId, String trainerId, InteractionType type, int hoursBack);
}
