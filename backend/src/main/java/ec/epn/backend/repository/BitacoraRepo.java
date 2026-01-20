package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class BitacoraRepo {

  private final JdbcTemplate jdbc;

  public BitacoraRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // ✅ (TU METODO) para el semáforo: cuenta solo APROBADAS
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

  // ✅ (NUEVO) obtener o crear la bitácora mensual del mes actual para un contrato
  public String obtenerOCrearActual(String contratoId) {
    LocalDate hoy = LocalDate.now();
    int anio = hoy.getYear();
    int mes = hoy.getMonthValue();

    String existente = jdbc.query("""
        SELECT id
        FROM bitacora_mensual
        WHERE contrato_id = ? AND anio = ? AND mes = ?
        LIMIT 1
      """,
      (rs) -> rs.next() ? rs.getString("id") : null,
      contratoId, anio, mes
    );

    if (existente != null) return existente;

    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO bitacora_mensual (id, contrato_id, anio, mes, estado)
      VALUES (?, ?, ?, ?, 'BORRADOR')
    """, id, contratoId, anio, mes);

    return id;
  }

  // ✅ (NUEVO) obtener cabecera/detalle de bitácora (para ver bitácora completa)
  public java.util.Map<String, Object> obtenerDetalle(String bitacoraId) {
    return jdbc.query("""
      SELECT id, contrato_id, anio, mes, estado, comentario_revision, creado_en
      FROM bitacora_mensual
      WHERE id = ?
    """, rs -> {
      if (!rs.next()) return null;
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("bitacoraId", rs.getString("id"));
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      m.put("comentarioRevision", rs.getString("comentario_revision"));
      m.put("creadoEn", rs.getString("creado_en"));
      return m;
    }, bitacoraId);
  }

  public int enviar(String bitacoraId) {
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET estado = 'ENVIADA'
      WHERE id = ?
    """, bitacoraId);
  }

  public String estado(String bitacoraId) {
    return jdbc.queryForObject("""
      SELECT estado FROM bitacora_mensual WHERE id = ?
    """, String.class, bitacoraId);
  }

  public java.util.List<java.util.Map<String, Object>> listarPendientesPorProyecto(String proyectoId) {
    return jdbc.query("""
      SELECT
        b.id AS bitacora_id,
        b.contrato_id,
        b.anio,
        b.mes,
        b.estado,
        b.creado_en,
        a.id AS ayudante_id,
        a.nombres,
        a.apellidos,
        a.correo_institucional
      FROM bitacora_mensual b
      JOIN contrato c ON c.id = b.contrato_id
      JOIN ayudante a ON a.id = c.ayudante_id
      WHERE c.proyecto_id = ?
        AND b.estado = 'ENVIADA'
      ORDER BY b.creado_en DESC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("bitacoraId", rs.getString("bitacora_id"));
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      m.put("creadoEn", rs.getString("creado_en"));
      m.put("ayudanteId", rs.getString("ayudante_id"));
      m.put("nombres", rs.getString("nombres"));
      m.put("apellidos", rs.getString("apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      return m;
    }, proyectoId);
  }

  // ✅ NUEVO: obtener estado actual
  public String obtenerEstado(String bitacoraId) {
    return jdbc.queryForObject("""
      SELECT estado
      FROM bitacora_mensual
      WHERE id = ?
    """, String.class, bitacoraId);
  }

  // ✅ NUEVO: aprobar/rechazar + comentario_revision (tu columna real)
  public int revisar(String bitacoraId, String nuevoEstado, String comentarioRevision) {
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET
        estado = ?,
        comentario_revision = ?
      WHERE id = ?
    """, nuevoEstado, comentarioRevision, bitacoraId);
  }

}
