package org.example.nexfit.ai.llm.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.nexfit.ai.llm.LlmMessage;
import org.example.nexfit.ai.llm.LlmProvider;
import org.example.nexfit.ai.llm.LlmRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GrokLlmProvider implements LlmProvider {

    private static final String API_URL = "https://api.x.ai/v1/chat/completions";

    @Value("${ai.grok.api-key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String complete(LlmRequest request) {
        try {
            var body = buildRequestBody(request, false);
            var httpRequest = buildHttpRequest(body);
            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Grok API error: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Grok API returned " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Grok completion failed", e);
            throw new RuntimeException("Failed to get AI response", e);
        }
    }

    @Override
    public void stream(LlmRequest request, Consumer<String> onToken, Runnable onComplete) {
        try {
            var body = buildRequestBody(request, true);
            var httpRequest = buildHttpRequest(body);
            var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes());
                log.error("Grok streaming error: {} - {}", response.statusCode(), errorBody);
                onToken.accept("Sorry, I'm having trouble connecting right now. Please try again.");
                onComplete.run();
                return;
            }

            try (var reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data: ")) continue;
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) break;

                    JsonNode chunk = objectMapper.readTree(data);
                    JsonNode delta = chunk.path("choices").get(0).path("delta");
                    if (delta.has("content")) {
                        onToken.accept(delta.get("content").asText());
                    }
                }
            }
            onComplete.run();
        } catch (Exception e) {
            log.error("Grok streaming failed", e);
            onToken.accept("Sorry, something went wrong. Please try again.");
            onComplete.run();
        }
    }

    @Override
    public String getProviderName() {
        return "grok";
    }

    private String buildRequestBody(LlmRequest request, boolean stream) {
        try {
            List<Map<String, String>> messages = request.getMessages().stream()
                    .map(m -> {
                        var map = new LinkedHashMap<String, String>();
                        map.put("role", m.getRole().name().toLowerCase());
                        map.put("content", m.getContent());
                        return map;
                    })
                    .collect(Collectors.toList());

            var body = new LinkedHashMap<String, Object>();
            body.put("model", request.getModel());
            body.put("messages", messages);
            body.put("temperature", request.getTemperature());
            body.put("max_tokens", request.getMaxTokens());
            body.put("stream", stream);

            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private HttpRequest buildHttpRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
