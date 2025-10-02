package com.example.demo.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class GeminiClient {
  private final ObjectMapper om = new ObjectMapper();

  @Value("${app.gemini.apiKey:}")
  private String apiKey;

  @Value("${app.gemini.model:gemini-1.5-flash}")
  private String model;

  public String summarize(String prompt) throws Exception {
    if (apiKey == null || apiKey.isBlank()){
      // Dev fallback so the UI still works without a key
      return "[Local fallback summary]\n" + (prompt.length() > 600 ? prompt.substring(0, 600) + "..." : prompt);
    }

    String url = String.format(
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
        model, apiKey);

    String bodyJson = om.createObjectNode()
        .set("contents", om.createArrayNode().add(
            om.createObjectNode().set("parts",
                om.createArrayNode().add(om.createObjectNode().put("text", prompt)))
        )).toString();

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
        .build();

    HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() / 100 != 2) {
      throw new RuntimeException("Gemini HTTP " + resp.statusCode() + " -> " + resp.body());
    }

    JsonNode root = om.readTree(resp.body());
    JsonNode cands = root.path("candidates");
    return (cands.isArray() && cands.size() > 0)
        ? cands.get(0).path("content").path("parts").get(0).path("text").asText()
        : "No summary returned.";
  }
}
