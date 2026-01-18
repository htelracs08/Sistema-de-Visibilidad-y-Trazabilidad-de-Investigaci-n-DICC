package ec.epn.backend.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CommonController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
      "ok", true,
      "service", "dicc-proyectos-backend",
      "timestamp", Instant.now().toString()
    );
  }
}
