package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public class BitacoraRepo {

  private final JdbcTemplate jdbc;

  public BitacoraRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // ✅ Para semáforo: cuenta solo APROBADAS
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

  // ✅ Obtener o crear la bitácora mensual del mes actual para un contrato
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
      rs -> rs.next() ? rs.getString("id") : null,
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

  // ✅ Obtener cabecera/detalle de bitácora (Ayudante)
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

  // ✅ Estado actual por bitácora
  public String obtenerEstado(String bitacoraId) {
    return jdbc.queryForObject("""
      SELECT estado
      FROM bitacora_mensual
      WHERE id = ?
    """, String.class, bitacoraId);
  }

  // ✅ Estado de bitácora a partir de una semanaId
  public String obtenerEstadoPorSemana(String semanaId) {
    return jdbc.queryForObject("""
      SELECT b.estado
      FROM informe_semanal s
      JOIN bitacora_mensual b ON b.id = s.bitacora_id
      WHERE s.id = ?
    """, String.class, semanaId);
  }

  // ✅ Seguridad: pertenece al contrato
  public boolean perteneceAContrato(String bitacoraId, String contratoId) {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM bitacora_mensual
      WHERE id = ? AND contrato_id = ?
    """, Integer.class, bitacoraId, contratoId);
    return n != null && n > 0;
  }

  // ✅ Enviar (seguro): SOLO si está en BORRADOR
  public int enviar(String bitacoraId) {
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET estado = 'ENVIADA'
      WHERE id = ? AND estado = 'BORRADOR'
    """, bitacoraId);
  }

  // ✅ Enviar + limpiar comentario del director (recomendado)
  public int enviarYLimpiarComentario(String bitacoraId) {
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET estado = 'ENVIADA',
          comentario_revision = NULL
      WHERE id = ? AND estado = 'BORRADOR'
    """, bitacoraId);
  }

  // ✅ Aprobar / Rechazar
  // Regla recomendada:
  // - Si APROBADA => queda APROBADA
  // - Si RECHAZADA => vuelve a BORRADOR (para que el ayudante pueda editar)
  public int revisar(String bitacoraId, String nuevoEstado, String comentarioRevision) {
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET
        estado = ?,
        comentario_revision = ?
      WHERE id = ?
    """, nuevoEstado, comentarioRevision, bitacoraId);
  }

  // ==========================
  // Seguridad Director
  // ==========================

  public boolean directorPuedeRevisarBitacora(String bitacoraId, String correoDirector) {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM bitacora_mensual b
      JOIN contrato c ON c.id = b.contrato_id
      JOIN proyecto p ON p.id = c.proyecto_id
      WHERE b.id = ?
        AND lower(p.director_correo) = lower(?)
    """, Integer.class, bitacoraId, correoDirector);
    return n != null && n > 0;
  }

  public java.util.Map<String, Object> obtenerDetalleParaDirector(String bitacoraId, String correoDirector) {
    var rows = jdbc.query("""
      SELECT
        b.id AS bitacora_id,
        b.contrato_id,
        b.anio,
        b.mes,
        b.estado,
        b.comentario_revision,
        b.creado_en,

        c.proyecto_id,
        a.id AS ayudante_id,
        a.nombres AS ayudante_nombres,
        a.apellidos AS ayudante_apellidos,
        a.correo_institucional,

        p.codigo AS proyecto_codigo,
        p.nombre AS proyecto_nombre,
        p.director_correo

      FROM bitacora_mensual b
      JOIN contrato c ON c.id = b.contrato_id
      JOIN ayudante a ON a.id = c.ayudante_id
      JOIN proyecto p ON p.id = c.proyecto_id
      WHERE b.id = ?
        AND lower(p.director_correo) = lower(?)
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("bitacoraId", rs.getString("bitacora_id"));
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      m.put("comentarioRevision", rs.getString("comentario_revision"));
      m.put("creadoEn", rs.getString("creado_en"));

      m.put("proyectoId", rs.getString("proyecto_id"));
      m.put("proyectoCodigo", rs.getString("proyecto_codigo"));
      m.put("proyectoNombre", rs.getString("proyecto_nombre"));
      m.put("correoDirector", rs.getString("director_correo"));

      m.put("ayudanteId", rs.getString("ayudante_id"));
      m.put("ayudanteNombres", rs.getString("ayudante_nombres"));
      m.put("ayudanteApellidos", rs.getString("ayudante_apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      return m;
    }, bitacoraId, correoDirector);

    return rows.isEmpty() ? null : rows.get(0);
  }

  public java.util.List<java.util.Map<String, Object>> listarPendientesPorProyectoParaDirector(String proyectoId, String correoDirector) {
    return jdbc.query("""
      SELECT
        b.id AS bitacora_id,
        b.contrato_id,
        b.anio,
        b.mes,
        b.estado,
        b.creado_en,
        a.nombres,
        a.apellidos,
        a.correo_institucional
      FROM bitacora_mensual b
      JOIN contrato c ON c.id = b.contrato_id
      JOIN proyecto p ON p.id = c.proyecto_id
      JOIN ayudante a ON a.id = c.ayudante_id
      WHERE p.id = ?
        AND lower(p.director_correo) = lower(?)
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
      m.put("nombres", rs.getString("nombres"));
      m.put("apellidos", rs.getString("apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      return m;
    }, proyectoId, correoDirector);
  }
}
