package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProyectoRepo {

  private final JdbcTemplate jdbc;

  public ProyectoRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String crear(String codigo, String nombre, String correoDirector) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO proyecto (id, codigo, nombre, director_correo, activo)
      VALUES (?, ?, ?, ?, 1)
      """, id, codigo, nombre, correoDirector);
    return id;
  }

  public java.util.List<java.util.Map<String, Object>> findAll() {
    return jdbc.query("""
        SELECT id, codigo, nombre, director_correo, activo, creado_en
        FROM proyecto
        ORDER BY creado_en DESC
        """,
      (rs, rowNum) -> java.util.Map.<String, Object>of(
        "id", rs.getString("id"),
        "codigo", rs.getString("codigo"),
        "nombre", rs.getString("nombre"),
        "correoDirector", rs.getString("director_correo"),
        "activo", rs.getInt("activo") == 1,
        "creadoEn", rs.getString("creado_en")
      )
    );
  }


}
