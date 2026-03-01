package org.example.nexfit.ai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    private List<LlmMessage> messages;

    @Builder.Default
    private String model = "gemini-2.0-flash-lite";

    @Builder.Default
    private double temperature = 0.7;

    @Builder.Default
    private int maxTokens = 1024;

    @Builder.Default
    private boolean stream = true;
}
