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
        SELECT 
          id, 
          codigo, 
          nombre, 
          director_correo, 
          activo, 
          tipo, 
          subtipo, 
          fecha_inicio, 
          fecha_fin, 
          max_ayudantes, 
          max_articulos, 
          creado_en
        FROM proyecto
        WHERE director_correo = ?
        ORDER BY creado_en DESC
        """,
      (rs, rowNum) -> Map.ofEntries(
        Map.entry("id", rs.getString("id")),
        Map.entry("codigo", rs.getString("codigo")),
        Map.entry("nombre", rs.getString("nombre")),
        Map.entry("directorCorreo", rs.getString("director_correo")),
        Map.entry("activo", rs.getInt("activo") == 1),
        Map.entry("tipo", rs.getString("tipo") != null ? rs.getString("tipo") : ""),
        Map.entry("subtipo", rs.getString("subtipo") != null ? rs.getString("subtipo") : ""),
        Map.entry("fechaInicio", rs.getString("fecha_inicio") != null ? rs.getString("fecha_inicio") : ""),
        Map.entry("fechaFin", rs.getString("fecha_fin") != null ? rs.getString("fecha_fin") : ""),
        Map.entry("maxAyudantes", rs.getObject("max_ayudantes") != null ? rs.getInt("max_ayudantes") : 0),
        Map.entry("maxArticulos", rs.getObject("max_articulos") != null ? rs.getInt("max_articulos") : 0),
        Map.entry("creadoEn", rs.getString("creado_en"))
      ),
      correo
    );
  }
}