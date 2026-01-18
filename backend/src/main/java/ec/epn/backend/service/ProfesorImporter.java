package ec.epn.backend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
public class ProfesorImporter implements CommandLineRunner {

  private final JdbcTemplate jdbc;

  public ProfesorImporter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(String... args) throws Exception {
    var resource = new ClassPathResource("profesores.csv");
    if (!resource.exists()) return;

    try (var br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      boolean first = true;
      while ((line = br.readLine()) != null) {
        if (first) { first = false; continue; } // salta header

        var parts = line.split(",", -1);
        if (parts.length < 3) continue;

        var nombres = parts[0].trim();
        var apellidos = parts[1].trim();
        var correo = parts[2].trim();

        if (correo.isBlank()) continue;

        jdbc.update("""
          INSERT OR IGNORE INTO profesor (id, nombres, apellidos, correo)
          VALUES (?, ?, ?, ?)
        """, UUID.randomUUID().toString(), nombres, apellidos, correo);
      }
    }
  }
}
