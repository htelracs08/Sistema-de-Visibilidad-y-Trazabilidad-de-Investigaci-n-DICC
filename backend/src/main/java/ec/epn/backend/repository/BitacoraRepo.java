package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BitacoraRepo {
  private final JdbcTemplate jdbc;

  public BitacoraRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int contarAprobadasEnRango(String contratoId, int anioDesde, int mesDesde, int anioHasta, int mesHasta) {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM bitacora_mensual
      WHERE contrato_id = ?
        AND estado = 'APROBADA'
        AND (
          (anio > ? OR (anio = ? AND mes >= ?))
          AND
          (anio < ? OR (anio = ? AND mes <= ?))
        )
    """, Integer.class, contratoId, anioDesde, anioDesde, mesDesde, anioHasta, anioHasta, mesHasta);

    return n == null ? 0 : n;
  }
}
