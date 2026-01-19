package ec.epn.backend.repository;

import ec.epn.backend.domain.Profesor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfesorRepo {
  private final JdbcTemplate jdbc;

  public ProfesorRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Profesor> findAll() {
    return jdbc.query("""
        SELECT id, nombres, apellidos, correo
        FROM profesor
        ORDER BY apellidos, nombres
        """,
      (rs, rowNum) -> new Profesor(
        rs.getString("id"),
        rs.getString("nombres"),
        rs.getString("apellidos"),
        rs.getString("correo")
      )
    );
  }
}
