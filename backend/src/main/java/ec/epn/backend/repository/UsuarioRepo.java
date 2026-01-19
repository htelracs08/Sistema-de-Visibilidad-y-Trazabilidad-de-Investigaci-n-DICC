package ec.epn.backend.repository;

import ec.epn.backend.domain.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepo {

  private final JdbcTemplate jdbc;

  public UsuarioRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Usuario> findByCorreo(String correo) {
    var list = jdbc.query("""
        SELECT id, nombres, apellidos, correo, password, rol, debe_cambiar_password
        FROM usuario
        WHERE correo = ?
        """,
      (rs, rowNum) -> new Usuario(
        rs.getString("id"),
        rs.getString("nombres"),
        rs.getString("apellidos"),
        rs.getString("correo"),
        rs.getString("password"),
        rs.getString("rol"),
        rs.getInt("debe_cambiar_password") == 1
      ),
      correo
    );
    return list.stream().findFirst();
  }

  public boolean existsByCorreo(String correo) {
    Integer count = jdbc.queryForObject(
      "SELECT COUNT(1) FROM usuario WHERE correo = ?",
      Integer.class,
      correo
    );
    return count != null && count > 0;
  }

  public void crearUsuario(String nombres, String apellidos, String correo, String password, String rol) {
    jdbc.update("""
      INSERT INTO usuario (id, nombres, apellidos, correo, password, rol, debe_cambiar_password)
      VALUES (?, ?, ?, ?, ?, ?, 1)
      """,
      UUID.randomUUID().toString(),
      nombres, apellidos, correo, password, rol
    );
  }

  public void cambiarPassword(String correo, String nuevaPassword) {
    jdbc.update("""
      UPDATE usuario
      SET password = ?, debe_cambiar_password = 0
      WHERE correo = ?
      """, nuevaPassword, correo);
  }


}
