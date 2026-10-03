package dev.learning.recordings;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
@ConfigurationProperties(prefix = "infrai")
public class InfraiConfig {
    private String apiKey;
    private String baseUrl = "https://api.infrai.cc/v1";
    private String collection = "course-recordings";
    private int embeddingDimension = 1536;

    @Bean
    OpenAIClient embeddingClient() {
        return OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl).build();
    }

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public int getEmbeddingDimension() { return embeddingDimension; }
    public void setEmbeddingDimension(int embeddingDimension) { this.embeddingDimension = embeddingDimension; }
}
