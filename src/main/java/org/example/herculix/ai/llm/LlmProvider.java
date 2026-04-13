package org.example.herculix.ai.llm;

import java.util.function.Consumer;

/**
 * Provider-agnostic interface for LLM interactions.
 * Implement this for each LLM vendor (Grok, OpenAI, Claude, etc.)
 */
public interface LlmProvider {

    /** Blocking call — returns full response. */
    String complete(LlmRequest request);

    /** Streaming call — pushes token chunks to the consumer. */
    void stream(LlmRequest request, Consumer<String> onToken, Runnable onComplete);

    /** Provider identifier for factory routing. */
    String getProviderName();
}
