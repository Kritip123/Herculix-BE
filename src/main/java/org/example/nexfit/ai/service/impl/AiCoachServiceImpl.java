package org.example.nexfit.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.nexfit.ai.llm.LlmMessage;
import org.example.nexfit.ai.llm.LlmProvider;
import org.example.nexfit.ai.llm.LlmRequest;
import org.example.nexfit.ai.model.ChatRequest;
import org.example.nexfit.ai.service.AiCoachService;
import org.example.nexfit.entity.SavedTrainer;
import org.example.nexfit.entity.Trainer;
import org.example.nexfit.entity.User;
import org.example.nexfit.repository.SavedTrainerRepository;
import org.example.nexfit.repository.TrainerRepository;
import org.example.nexfit.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoachServiceImpl implements AiCoachService {

    private final LlmProvider activeLlmProvider;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final SavedTrainerRepository savedTrainerRepository;

    @Override
    public void chat(String userId, ChatRequest request, Consumer<String> onToken, Runnable onComplete) {
        List<LlmMessage> messages = buildMessages(userId, request);
        LlmRequest llmRequest = LlmRequest.builder()
                .messages(messages)
                .stream(true)
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        activeLlmProvider.stream(llmRequest, onToken, onComplete);
    }

    @Override
    public String chatSync(String userId, ChatRequest request) {
        List<LlmMessage> messages = buildMessages(userId, request);
        LlmRequest llmRequest = LlmRequest.builder()
                .messages(messages)
                .stream(false)
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        return activeLlmProvider.complete(llmRequest);
    }

    @Override
    public List<String> getSuggestions(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        List<String> suggestions = new ArrayList<>();

        suggestions.add("Create a workout plan for me");
        suggestions.add("What should I eat today?");

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getFitnessGoals() != null && !user.getFitnessGoals().isEmpty()) {
                String goal = user.getFitnessGoals().get(0);
                suggestions.add("How do I achieve my " + goal.toLowerCase() + " goal?");
            }

            if (user.getExperienceLevel() != null) {
                suggestions.add("Best exercises for a " + user.getExperienceLevel().name().toLowerCase() + "?");
            }

            List<SavedTrainer> saved = savedTrainerRepository.findByUserIdOrderBySavedAtDesc(userId);
            if (!saved.isEmpty()) {
                suggestions.add("Compare my saved trainers");
            } else {
                suggestions.add("Help me find the right trainer");
            }
        }

        suggestions.add("Tips for staying motivated");
        suggestions.add("How to avoid injuries while training?");

        return suggestions.stream().limit(6).collect(Collectors.toList());
    }

    private List<LlmMessage> buildMessages(String userId, ChatRequest request) {
        String systemPrompt = buildSystemPrompt(userId);
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(systemPrompt));

        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                LlmMessage.Role role = "assistant".equalsIgnoreCase(msg.getRole())
                        ? LlmMessage.Role.ASSISTANT : LlmMessage.Role.USER;
                messages.add(new LlmMessage(role, msg.getContent()));
            }
        }

        messages.add(LlmMessage.user(request.getMessage()));
        return messages;
    }

    private String buildSystemPrompt(String userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are NexFit AI Coach — a friendly, knowledgeable fitness and wellness assistant ");
        sb.append("built into the NexFit platform. You help users with workout plans, nutrition guidance, ");
        sb.append("trainer recommendations, exercise form and technique, motivation, recovery and general fitness questions.\n\n");

        sb.append("STRICT formatting rules you MUST follow:\n");
        sb.append("- Keep responses concise — a short paragraph for simple questions, a few bullet points for detailed ones. Never more than 6-8 lines total.\n");
        sb.append("- NEVER use markdown formatting like ** for bold, # for headers, or any other markdown syntax\n");
        sb.append("- Use plain bullet points (just a dash -) only when listing items\n");
        sb.append("- Write in a conversational, friendly tone like a personal trainer texting a client\n");
        sb.append("- Be concise and actionable — no filler, no long intros\n");
        sb.append("- When recommending trainers, use real data from the platform\n");
        sb.append("- If asked about medical conditions, suggest consulting a healthcare professional\n");
        sb.append("- Use emojis sparingly for warmth\n\n");

        appendUserContext(sb, userId);
        appendTrainerContext(sb, userId);

        return sb.toString();
    }

    private void appendUserContext(StringBuilder sb, String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        sb.append("--- USER PROFILE ---\n");

        if (StringUtils.isNotBlank(user.getName())) {
            sb.append("Name: ").append(user.getName()).append("\n");
        }
        if (user.getGender() != null) {
            sb.append("Gender: ").append(user.getGender()).append("\n");
        }
        if (user.getExperienceLevel() != null) {
            sb.append("Experience Level: ").append(user.getExperienceLevel()).append("\n");
        }
        if (user.getFitnessGoals() != null && !user.getFitnessGoals().isEmpty()) {
            sb.append("Fitness Goals: ").append(String.join(", ", user.getFitnessGoals())).append("\n");
        }
        if (user.getPreferredActivities() != null && !user.getPreferredActivities().isEmpty()) {
            sb.append("Preferred Activities: ").append(String.join(", ", user.getPreferredActivities())).append("\n");
        }
        if (user.getSelectedCategories() != null && !user.getSelectedCategories().isEmpty()) {
            sb.append("Interested Categories: ").append(String.join(", ", user.getSelectedCategories())).append("\n");
        }
        sb.append("\n");
    }

    private void appendTrainerContext(StringBuilder sb, String userId) {
        List<SavedTrainer> savedTrainers = savedTrainerRepository.findByUserIdOrderBySavedAtDesc(userId);
        if (savedTrainers.isEmpty()) return;

        sb.append("--- SAVED TRAINERS ---\n");
        sb.append("The user has saved these trainers on NexFit:\n\n");

        for (SavedTrainer saved : savedTrainers) {
            Optional<Trainer> trainerOpt = trainerRepository.findById(saved.getTrainerId());
            if (trainerOpt.isEmpty()) continue;

            Trainer trainer = trainerOpt.get();
            sb.append("• **").append(trainer.getName()).append("**");
            if (trainer.getHourlyRate() != null) {
                sb.append(" — $").append(trainer.getHourlyRate()).append("/hr");
            }
            sb.append("\n");

            if (!trainer.getSpecializations().isEmpty()) {
                sb.append("  Specializations: ").append(String.join(", ", trainer.getSpecializations())).append("\n");
            }
            if (trainer.getExperience() != null) {
                sb.append("  Experience: ").append(trainer.getExperience()).append(" years\n");
            }
            if (StringUtils.isNotBlank(trainer.getAddress())) {
                sb.append("  Location: ").append(trainer.getAddress()).append("\n");
            }
            if (StringUtils.isNotBlank(trainer.getGymAffiliation())) {
                sb.append("  Gym: ").append(trainer.getGymAffiliation()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("When the user asks about trainers or recommendations, reference this data.\n\n");
    }
}
