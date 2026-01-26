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

    int n = jdbc.update("""
      INSERT INTO actividad (id, semana_id, hora_inicio, hora_salida, total_horas, descripcion)
      SELECT ?, ?, ?, ?, ?, ?
      WHERE EXISTS (
        SELECT 1
        FROM informe_semanal s
        JOIN bitacora_mensual b ON b.id = s.bitacora_id
        WHERE s.id = ? AND (b.estado = 'BORRADOR' OR b.estado = 'RECHAZADA')
      )
    """, id, semanaId, horaInicio, horaSalida, totalHoras, descripcion, semanaId);

    if (n == 0) throw new IllegalStateException("No se pudo crear la actividad: la bitácora no existe o no está en BORRADOR/RECHAZADA");
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

  public String obtenerBitacoraIdPorActividad(String actividadId) {
    return jdbc.query("""
      SELECT s.bitacora_id
      FROM actividad a
      JOIN informe_semanal s ON s.id = a.semana_id
      WHERE a.id = ?
    """, rs -> rs.next() ? rs.getString("bitacora_id") : null, actividadId);
  }

  public int updateActividad(String actividadId,
                             String horaInicio,
                             String horaFin,
                             String descripcion) {
    return jdbc.update("""
      UPDATE actividad
      SET hora_inicio = ?,
          hora_salida = ?,
          descripcion = ?
      WHERE id = ?
    """, horaInicio, horaFin, descripcion, actividadId);
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
