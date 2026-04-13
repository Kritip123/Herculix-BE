package org.example.herculix.ai.llm.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.herculix.ai.llm.LlmMessage;
import org.example.herculix.ai.llm.LlmProvider;
import org.example.herculix.ai.llm.LlmRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Component
public class GeminiLlmProvider implements LlmProvider {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-flash-latest}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String complete(LlmRequest request) {
        try {
            String url = BASE_URL + model + ":generateContent";
            String body = buildRequestBody(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API returned " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Gemini completion failed", e);
            throw new RuntimeException("Failed to get AI response", e);
        }
    }

    @Override
    public void stream(LlmRequest request, Consumer<String> onToken, Runnable onComplete) {
        try {
            String url = BASE_URL + model + ":streamGenerateContent?alt=sse";
            String body = buildRequestBody(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes());
                log.error("Gemini streaming error: {} - {}", response.statusCode(), errorBody);
                onToken.accept("Sorry, I'm having trouble connecting right now. Please try again.");
                onComplete.run();
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data: ")) continue;
                    String data = line.substring(6).trim();
                    if (data.isEmpty()) continue;

                    JsonNode chunk = objectMapper.readTree(data);
                    JsonNode candidates = chunk.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode parts = candidates.get(0).path("content").path("parts");
                        if (parts.isArray() && !parts.isEmpty()) {
                            String text = parts.get(0).path("text").asText("");
                            if (!text.isEmpty()) {
                                onToken.accept(text);
                            }
                        }
                    }
                }
            }
            onComplete.run();
        } catch (Exception e) {
            log.error("Gemini streaming failed", e);
            onToken.accept("Sorry, something went wrong. Please try again.");
            onComplete.run();
        }
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    /**
     * Gemini uses a different format: system instruction is separate,
     * and messages use "user"/"model" roles in a "contents" array.
     */
    private String buildRequestBody(LlmRequest request) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();

            String systemText = null;
            List<Map<String, Object>> contents = new ArrayList<>();

            for (LlmMessage msg : request.getMessages()) {
                if (msg.getRole() == LlmMessage.Role.SYSTEM) {
                    systemText = msg.getContent();
                    continue;
                }

                String role = msg.getRole() == LlmMessage.Role.ASSISTANT ? "model" : "user";
                Map<String, Object> content = new LinkedHashMap<>();
                content.put("role", role);
                content.put("parts", List.of(Map.of("text", msg.getContent())));
                contents.add(content);
            }

            if (systemText != null) {
                body.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemText))
                ));
            }

            body.put("contents", contents);
            body.put("generationConfig", Map.of(
                    "temperature", request.getTemperature(),
                    "maxOutputTokens", request.getMaxTokens()
            ));

            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Gemini request body", e);
        }
    }
}
