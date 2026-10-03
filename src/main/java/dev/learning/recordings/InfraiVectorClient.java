package dev.learning.recordings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class InfraiVectorClient {
    private final HttpClient http;
    private final ObjectMapper json;
    private final InfraiConfig config;

    public InfraiVectorClient(HttpClient http, ObjectMapper json, InfraiConfig config) {
        this.http = http;
        this.json = json;
        this.config = config;
    }

    public JsonNode createCollection() {
        return post("/vector/collection/create", Map.of(
                "collection", config.getCollection(),
                "dimension", config.getEmbeddingDimension(),
                "metric", "cosine",
                "metadata", Map.of("purpose", "course-recording-search")
        ), "collection-" + config.getCollection());
    }

    public JsonNode upsert(String recordingId, java.util.List<Float> embedding, Map<String, Object> metadata) {
        return post("/vector/upsert", Map.of(
                "collection", config.getCollection(),
                "vectors", java.util.List.of(Map.of(
                        "id", recordingId,
                        "embedding", embedding,
                        "metadata", metadata
                ))
        ), "recording-" + recordingId);
    }

    public JsonNode query(java.util.List<Float> embedding, String courseId, int topK) {
        return post("/vector/query", Map.of(
                "collection", config.getCollection(),
                "embedding", embedding,
                "top_k", topK,
                "filter", Map.of("course_id", courseId, "educator_visible", true),
                "include_metadata", true
        ), null).path("items");
    }

    private JsonNode post(String path, Object body, String idempotencyKey) {
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.getBaseUrl() + path))
                        .timeout(Duration.ofSeconds(30))
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
                if (idempotencyKey != null) builder.header("Idempotency-Key", idempotencyKey);

                HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                JsonNode envelope = json.readTree(response.body());
                if (!envelope.path("ok").asBoolean(false)) {
                    if (response.statusCode() == 429 && attempt < 3) {
                        pause(response, attempt);
                        continue;
                    }
                    throw new InfraiRejectedException(response.statusCode(), envelope.path("error"));
                }
                if (response.statusCode() >= 500) throw new IllegalStateException("Infrai transport response " + response.statusCode());
                return envelope.path("data");
            } catch (IOException e) {
                throw new IllegalStateException("Could not decode the Infrai response", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Request interrupted", e);
            }
        }
        throw new IllegalStateException("Retry budget exhausted");
    }

    private static void pause(HttpResponse<?> response, int attempt) throws InterruptedException {
        long seconds = response.headers().firstValue("Retry-After")
                .map(InfraiVectorClient::parseSeconds)
                .orElse(1L << attempt);
        Thread.sleep(Duration.ofSeconds(Math.min(seconds, 30)).toMillis());
    }

    private static long parseSeconds(String value) {
        try { return Math.max(1, Long.parseLong(value)); }
        catch (NumberFormatException ignored) { return 1; }
    }
}
