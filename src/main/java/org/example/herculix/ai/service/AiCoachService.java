package org.example.herculix.ai.service;

import org.example.herculix.ai.model.ChatRequest;

import java.util.function.Consumer;

public interface AiCoachService {

    /** Streaming response — pushes chunks to the consumer. */
    void chat(String userId, ChatRequest request, Consumer<String> onToken, Runnable onComplete);

    /** Blocking response for non-streaming clients. */
    String chatSync(String userId, ChatRequest request);

    /** Quick suggestion chips based on user context. */
    java.util.List<String> getSuggestions(String userId);
}
