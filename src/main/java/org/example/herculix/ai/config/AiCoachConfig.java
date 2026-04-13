package org.example.herculix.ai.config;

import lombok.RequiredArgsConstructor;
import org.example.herculix.ai.llm.LlmProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AiCoachConfig {

    @Value("${ai.provider:gemini}")
    private String activeProvider;

    @Bean
    public LlmProvider activeLlmProvider(List<LlmProvider> providers) {
        return providers.stream()
                .filter(p -> p.getProviderName().equals(activeProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No LLM provider found for: " + activeProvider));
    }
}
