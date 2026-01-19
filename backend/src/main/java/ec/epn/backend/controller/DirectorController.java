package ec.epn.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorController {

  private final JdbcTemplate jdbc;

  public DirectorController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/mis-proyectos")
  public List<Map<String, Object>> misProyectos(Principal principal) {
    String correo = principal.getName();

    return jdbc.query("""
        SELECT id, codigo, nombre, activo, creado_en
        FROM proyecto
        WHERE director_correo = ?
        ORDER BY creado_en DESC
        """,
      (rs, rowNum) -> Map.<String, Object>of(
        "id", rs.getString("id"),
        "codigo", rs.getString("codigo"),
        "nombre", rs.getString("nombre"),
        "activo", rs.getInt("activo") == 1,
        "creadoEn", rs.getString("creado_en")
      ),
      correo
    );
  }
}
