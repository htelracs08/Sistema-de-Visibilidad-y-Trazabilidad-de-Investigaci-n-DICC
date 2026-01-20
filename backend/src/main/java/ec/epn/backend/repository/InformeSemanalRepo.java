package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class InformeSemanalRepo {

  private final JdbcTemplate jdbc;

  public InformeSemanalRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String crear(String bitacoraId,
                      String fechaInicioSemana,
                      String fechaFinSemana,
                      String actividadesRealizadas,
                      String observaciones,
                      String anexos) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO informe_semanal
        (id, bitacora_id, fecha_inicio_semana, fecha_fin_semana, actividades_realizadas, observaciones, anexos)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    """, id, bitacoraId, fechaInicioSemana, fechaFinSemana, actividadesRealizadas, observaciones, anexos);

    return id;
  }

  public java.util.List<java.util.Map<String, Object>> listarPorBitacora(String bitacoraId) {
    return jdbc.query("""
      SELECT id, bitacora_id, fecha_inicio_semana, fecha_fin_semana, actividades_realizadas, observaciones, anexos, creado_en
      FROM informe_semanal
      WHERE bitacora_id = ?
      ORDER BY fecha_inicio_semana ASC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("semanaId", rs.getString("id"));
      m.put("bitacoraId", rs.getString("bitacora_id"));
      m.put("fechaInicioSemana", rs.getString("fecha_inicio_semana"));
      m.put("fechaFinSemana", rs.getString("fecha_fin_semana"));
      m.put("actividadesRealizadas", rs.getString("actividades_realizadas"));
      m.put("observaciones", rs.getString("observaciones"));
      m.put("anexos", rs.getString("anexos"));
      m.put("creadoEn", rs.getString("creado_en"));
      return m;
    }, bitacoraId);
  }

  public int contarPorBitacora(String bitacoraId) {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM informe_semanal
      WHERE bitacora_id = ?
    """, Integer.class, bitacoraId);
    return n == null ? 0 : n;
  }

}
