package ec.epn.dicc.director.api;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApiClient {
  private final String baseUrl;
  private String correo;
  private String password;

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setAuth(String correo, String password) {
    this.correo = correo;
    this.password = password;
  }

  public JsonObject getMe() {
    return get("/api/v1/me");
  }

  public JsonObject get(String path) {
    return request("GET", path, null);
  }

  public JsonObject postJson(String path, JsonObject body) {
    return request("POST", path, body);
  }

  public JsonObject putJson(String path, JsonObject body) {
    return request("PUT", path, body);
  }

  public JsonObject request(String method, String path, JsonObject body) {
    try {
      URL url = URI.create(baseUrl + path).toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(20000);

      // Basic Auth
      String token = Base64.getEncoder().encodeToString((correo + ":" + password).getBytes(StandardCharsets.UTF_8));
      conn.setRequestProperty("Authorization", "Basic " + token);
      conn.setRequestProperty("Accept", "application/json");

      if (body != null) {
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
          os.write(bytes);
        }
      }

      int code = conn.getResponseCode();
      InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
      String resp = readAll(is);

      JsonObject out = new JsonObject();
      out.addProperty("_httpStatus", code);

      // Si el backend responde JSON, parsea; si no, guarda como texto.
      try {
        JsonElement parsed = JsonParser.parseString(resp);
        if (parsed.isJsonObject()) out.add("data", parsed.getAsJsonObject());
        else out.add("data", parsed);
      } catch (Exception e) {
        out.addProperty("raw", resp);
      }

      return out;
    } catch (Exception e) {
      JsonObject err = new JsonObject();
      err.addProperty("_httpStatus", 0);
      err.addProperty("error", e.getClass().getSimpleName() + ": " + e.getMessage());
      return err;
    }
  }

  private String readAll(InputStream is) throws IOException {
    if (is == null) return "";
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) sb.append(line);
      return sb.toString();
    }
  }
}
