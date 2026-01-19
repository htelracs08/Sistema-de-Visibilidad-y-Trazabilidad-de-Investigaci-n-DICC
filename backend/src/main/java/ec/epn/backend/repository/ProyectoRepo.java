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
}
