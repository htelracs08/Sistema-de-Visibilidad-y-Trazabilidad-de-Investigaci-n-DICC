package ec.epn.backend.repository;

import ec.epn.backend.domain.Profesor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

  public Optional<Profesor> findByCorreo(String correo) {
    var list = jdbc.query("""
        SELECT id, nombres, apellidos, correo
        FROM profesor
        WHERE correo = ?
        """,
      (rs, rowNum) -> new Profesor(
        rs.getString("id"),
        rs.getString("nombres"),
        rs.getString("apellidos"),
        rs.getString("correo")
      ),
      correo
    );
    return list.stream().findFirst();
  }
}
