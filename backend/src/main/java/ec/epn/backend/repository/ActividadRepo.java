package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ActividadRepo {

  private final JdbcTemplate jdbc;

  public ActividadRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String crear(String semanaId,
                      String horaInicio,
                      String horaSalida,
                      double totalHoras,
                      String descripcion) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO actividad (id, semana_id, hora_inicio, hora_salida, total_horas, descripcion)
      VALUES (?, ?, ?, ?, ?, ?)
    """, id, semanaId, horaInicio, horaSalida, totalHoras, descripcion);

    return id;
  }

  public java.util.List<java.util.Map<String, Object>> listarPorSemana(String semanaId) {
    return jdbc.query("""
      SELECT id, semana_id, hora_inicio, hora_salida, total_horas, descripcion, creado_en
      FROM actividad
      WHERE semana_id = ?
      ORDER BY creado_en ASC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("actividadId", rs.getString("id"));
      m.put("semanaId", rs.getString("semana_id"));
      m.put("horaInicio", rs.getString("hora_inicio"));
      m.put("horaSalida", rs.getString("hora_salida"));
      m.put("totalHoras", rs.getDouble("total_horas"));
      m.put("descripcion", rs.getString("descripcion"));
      m.put("creadoEn", rs.getString("creado_en"));
      return m;
    }, semanaId);
  }
  
  //bloqueo
  public int contarPorBitacora(String bitacoraId) {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM actividad a
      JOIN informe_semanal s ON s.id = a.semana_id
      WHERE s.bitacora_id = ?
    """, Integer.class, bitacoraId);
    return n == null ? 0 : n;
  }

}
