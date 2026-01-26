package ec.epn.dicc.ayudante.api;

import com.google.gson.*;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApiClient {
  private final String baseUrl;
  private final HttpClient http = HttpClient.newBuilder().build();
  private final Gson gson = new GsonBuilder().create();

  private String basicAuthHeader = null;

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
  }

  public void setAuth(String correo, String pass) {
    String raw = correo + ":" + pass;
    String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    this.basicAuthHeader = "Basic " + b64;
  }

  private JsonObject wrap(int status, String body) {
    JsonObject out = new JsonObject();
    out.addProperty("_httpStatus", status);
    try {
      JsonElement parsed = JsonParser.parseString(body == null ? "" : body);
      if (parsed.isJsonObject()) out.add("data", parsed.getAsJsonObject());
      else out.add("data", parsed);
    } catch (Exception e) {
      JsonObject err = new JsonObject();
      err.addProperty("raw", body);
      out.add("data", err);
    }
    return out;
  }

  private HttpRequest.Builder req(String method, String path) {
    String url = baseUrl + (path.startsWith("/") ? path : ("/" + path));
    HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(url));
    if (basicAuthHeader != null) b.header("Authorization", basicAuthHeader);
    b.header("Accept", "application/json");
    return b;
  }

  public JsonObject get(String path) {
    try {
      HttpRequest request = req("GET", path).GET().build();
      HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
      return wrap(resp.statusCode(), resp.body());
    } catch (Exception e) {
      return wrap(0, "{\"ok\":false,\"msg\":\"" + e.getMessage().replace("\"","'") + "\"}");
    }
  }

  public JsonObject post(String path, JsonObject json) {
    try {
      String body = json == null ? "" : gson.toJson(json);
      HttpRequest request = req("POST", path)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();
      HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
      return wrap(resp.statusCode(), resp.body());
    } catch (Exception e) {
      return wrap(0, "{\"ok\":false,\"msg\":\"" + e.getMessage().replace("\"","'") + "\"}");
    }
  }

  public JsonObject put(String path, JsonObject json) {
    try {
      String body = json == null ? "" : gson.toJson(json);
      HttpRequest request = req("PUT", path)
          .header("Content-Type", "application/json")
          .PUT(HttpRequest.BodyPublishers.ofString(body))
          .build();
      HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
      return wrap(resp.statusCode(), resp.body());
    } catch (Exception e) {
      return wrap(0, "{\"ok\":false,\"msg\":\"" + e.getMessage().replace("\"","'") + "\"}");
    }
  }

  // ---------- Endpoints específicos ----------
  public JsonObject getMe() { return get("/me"); }

  public JsonObject obtenerBitacoraActual() {
    return post("/ayudante/bitacoras/actual", new JsonObject());
  }

  public JsonObject verBitacora(String bitacoraId) {
    return get("/ayudante/bitacoras/" + bitacoraId);
  }

  public JsonObject listarBitacorasAprobadas() {
    return get("/ayudante/bitacoras/aprobadas");
  }

  public JsonObject crearSemana(String bitacoraId, String fi, String ff, String act, String obs, String anex) {
    JsonObject j = new JsonObject();
    j.addProperty("fechaInicioSemana", fi);
    j.addProperty("fechaFinSemana", ff);
    j.addProperty("actividadesRealizadas", act);
    if (obs != null) j.addProperty("observaciones", obs);
    if (anex != null) j.addProperty("anexos", anex);
    return post("/ayudante/bitacoras/" + bitacoraId + "/semanas", j);
  }

  public JsonObject crearActividad(String semanaId, String hIni, String hSal, String desc) {
    JsonObject j = new JsonObject();
    j.addProperty("horaInicio", hIni);
    j.addProperty("horaSalida", hSal);
    j.addProperty("descripcion", desc);
    return post("/ayudante/semanas/" + semanaId + "/actividades", j);
  }

  public JsonObject actualizarActividad(String actividadId, String hIni, String hFin, String desc) {
    JsonObject j = new JsonObject();
    j.addProperty("horaInicio", hIni);
    j.addProperty("horaFin", hFin);
    j.addProperty("descripcion", desc);
    return put("/ayudante/actividades/" + actividadId, j);
  }

  public JsonObject enviarBitacora(String bitacoraId) {
    return post("/ayudante/bitacoras/" + bitacoraId + "/enviar", new JsonObject());
  }
}
