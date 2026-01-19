package ec.epn.backend.repository;

import ec.epn.backend.domain.Ayudante;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AyudanteRepo {

  private final JdbcTemplate jdbc;

  public AyudanteRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Ayudante> findByCorreoInstitucional(String correo) {
    var list = jdbc.query("""
        SELECT id, nombres, apellidos, correo_institucional, facultad, quintil, tipo_ayudante
        FROM ayudante
        WHERE correo_institucional = ?
        """,
      (rs, rowNum) -> new Ayudante(
        rs.getString("id"),
        rs.getString("nombres"),
        rs.getString("apellidos"),
        rs.getString("correo_institucional"),
        rs.getString("facultad"),
        rs.getInt("quintil"),
        rs.getString("tipo_ayudante")
      ),
      correo
    );
    return list.stream().findFirst();
  }

  public String crear(Ayudante a) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO ayudante (id, nombres, apellidos, correo_institucional, facultad, quintil, tipo_ayudante)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """,
      id, a.nombres(), a.apellidos(), a.correoInstitucional(), a.facultad(), a.quintil(), a.tipoAyudante()
    );
    return id;
  }
}