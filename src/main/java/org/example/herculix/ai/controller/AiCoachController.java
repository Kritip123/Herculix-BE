package org.example.herculix.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herculix.ai.model.ChatRequest;
import org.example.herculix.ai.service.AiCoachService;
import org.example.herculix.entity.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai-coach")
@RequiredArgsConstructor
public class AiCoachController {

    private final AiCoachService aiCoachService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request) {

        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        String userId = user.getId();
        log.debug("AI Coach stream request from user: {}", userId);

        new Thread(() -> {
            try {
                aiCoachService.chat(userId, request,
                        token -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("token")
                                        .data(Map.of("content", token)));
                            } catch (Exception e) {
                                log.debug("Client disconnected during streaming");
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                            } catch (Exception e) {
                                log.debug("Error completing SSE emitter");
                            }
                        });
            } catch (Exception e) {
                log.error("AI Coach streaming error", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request) {

        String response = aiCoachService.chatSync(user.getId(), request);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(aiCoachService.getSuggestions(user.getId()));
    }
}
