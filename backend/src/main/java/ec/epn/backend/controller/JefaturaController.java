package ec.epn.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jefatura")
public class JefaturaController {

  private final JdbcTemplate jdbc;

  public JefaturaController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/profesores")
  public List<Map<String, Object>> listarProfesores(@RequestParam(name="buscar", required=false) String buscar) {
    if (buscar == null || buscar.isBlank()) {
      return jdbc.queryForList("SELECT nombres, apellidos, correo FROM profesor ORDER BY apellidos, nombres");
    }
    var like = "%" + buscar.trim().toLowerCase() + "%";
    return jdbc.queryForList("""
      SELECT nombres, apellidos, correo
      FROM profesor
      WHERE lower(nombres) LIKE ? OR lower(apellidos) LIKE ? OR lower(correo) LIKE ?
      ORDER BY apellidos, nombres
      """, like, like, like);
  }
}
