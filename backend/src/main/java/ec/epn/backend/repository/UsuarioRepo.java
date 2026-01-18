package ec.epn.backend.repository;

import ec.epn.backend.domain.Usuario;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepo {
  private final JdbcTemplate jdbc;

  public UsuarioRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Usuario> findByCorreo(String correo) {
    var sql = """
      SELECT id, nombres, apellidos, correo, password_hash, rol, debe_cambiar_password
      FROM usuario
      WHERE correo = ?
      """;

    var list = jdbc.query(sql, (rs, rowNum) -> new Usuario(
        rs.getString("id"),
        rs.getString("nombres"),
        rs.getString("apellidos"),
        rs.getString("correo"),
        rs.getString("password_hash"),
        rs.getString("rol"),
        rs.getInt("debe_cambiar_password") == 1
    ), correo);

    return list.stream().findFirst();
  }
}
