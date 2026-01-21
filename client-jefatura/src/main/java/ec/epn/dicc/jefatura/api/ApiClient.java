package ec.epn.dicc.jefatura.api;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class ApiClient {

  private final HttpClient http;
  private final String baseUrl;
  private String authHeader; // "Basic xxx"

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
    this.http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();
  }

  public void setBasicAuth(String correo, String password) {
    String raw = correo + ":" + password;
    String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    this.authHeader = "Basic " + b64;
  }

  public String get(String path) throws Exception {
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .GET()
        .header("Authorization", authHeader == null ? "" : authHeader)
        .header("Accept", "application/json")
        .timeout(Duration.ofSeconds(20))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() >= 200 && res.statusCode() < 300) return res.body();
    throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
  }

  public String postJson(String path, String jsonBody) throws Exception {
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .header("Authorization", authHeader == null ? "" : authHeader)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .timeout(Duration.ofSeconds(20))
        .build();

    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() >= 200 && res.statusCode() < 300) return res.body();
    throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
  }
}
