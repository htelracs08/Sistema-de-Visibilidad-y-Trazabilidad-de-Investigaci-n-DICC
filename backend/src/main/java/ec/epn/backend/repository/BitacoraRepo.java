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

  // ✅ NUEVO MÉTODO: Listar bitácoras aprobadas de un proyecto (para Director)
  public java.util.List<java.util.Map<String, Object>> listarAprobadasPorProyectoParaDirector(String proyectoId, String correoDirector) {
    return jdbc.query("""
      SELECT
        b.id AS bitacora_id,
        b.contrato_id,
        b.anio,
        b.mes,
        b.estado,
        b.comentario_revision,
        b.creado_en,
        a.nombres,
        a.apellidos,
        a.correo_institucional,
        c.fecha_inicio AS contrato_fecha_inicio,
        c.fecha_fin AS contrato_fecha_fin
      FROM bitacora_mensual b
      JOIN contrato c ON c.id = b.contrato_id
      JOIN proyecto p ON p.id = c.proyecto_id
      JOIN ayudante a ON a.id = c.ayudante_id
      WHERE p.id = ?
        AND lower(p.director_correo) = lower(?)
        AND b.estado = 'APROBADA'
      ORDER BY b.anio DESC, b.mes DESC, a.apellidos ASC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("bitacoraId", rs.getString("bitacora_id"));
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      m.put("comentarioRevision", rs.getString("comentario_revision"));
      m.put("creadoEn", rs.getString("creado_en"));
      m.put("nombres", rs.getString("nombres"));
      m.put("apellidos", rs.getString("apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      m.put("contratoFechaInicio", rs.getString("contrato_fecha_inicio"));
      m.put("contratoFechaFin", rs.getString("contrato_fecha_fin"));
      return m;
    }, proyectoId, correoDirector);
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

  // ✅ Obtener o crear la bitácora mensual para un contrato
  // Regla:
  // - Si existe BORRADOR => devolver esa (única activa).
  // - Si NO existe BORRADOR:
  //   - Si la última está RECHAZADA => devolver esa (no crear nueva).
  //   - Si la última está ENVIADA o APROBADA => crear la siguiente.
  public String obtenerOCrearActual(String contratoId) {
    String borradorId = obtenerBorradorActiva(contratoId);
    if (borradorId != null) return borradorId;

    var ultima = obtenerUltimaPorContrato(contratoId);
    if (ultima == null) {
      LocalDate hoy = LocalDate.now();
      return crearBitacora(contratoId, hoy.getYear(), hoy.getMonthValue());
    }

    String estado = String.valueOf(ultima.get("estado"));
    Number anioN = (Number) ultima.get("anio");
    Number mesN = (Number) ultima.get("mes");
    int anio = anioN == null ? LocalDate.now().getYear() : anioN.intValue();
    int mes = mesN == null ? LocalDate.now().getMonthValue() : mesN.intValue();

    if ("RECHAZADA".equalsIgnoreCase(estado)) {
      return String.valueOf(ultima.get("id"));
    }

    if ("APROBADA".equalsIgnoreCase(estado) || "ENVIADA".equalsIgnoreCase(estado)) {
      int[] next = siguientePeriodo(anio, mes);
      return crearBitacora(contratoId, next[0], next[1]);
    }

    return String.valueOf(ultima.get("id"));
  }

  public String obtenerBorradorActiva(String contratoId) {
    return jdbc.query("""
      SELECT id
      FROM bitacora_mensual
      WHERE contrato_id = ? AND UPPER(estado) = 'BORRADOR'
      ORDER BY anio DESC, mes DESC
      LIMIT 1
    """, rs -> rs.next() ? rs.getString("id") : null, contratoId);
  }

  private int[] siguientePeriodo(int anio, int mes) {
    int nextAnio = anio;
    int nextMes = mes + 1;
    if (nextMes > 12) {
      nextMes = 1;
      nextAnio++;
    }
    return new int[]{nextAnio, nextMes};
  }

  public java.util.Map<String, Object> obtenerUltimaPorContrato(String contratoId) {
    return jdbc.query("""
      SELECT id, anio, mes, estado
      FROM bitacora_mensual
      WHERE contrato_id = ?
      ORDER BY anio DESC, mes DESC
      LIMIT 1
    """, rs -> {
      if (!rs.next()) return null;
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("id", rs.getString("id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      return m;
    }, contratoId);
  }

  public String crearBitacora(String contratoId, int anio, int mes) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO bitacora_mensual (id, contrato_id, anio, mes, estado)
      VALUES (?, ?, ?, ?, 'BORRADOR')
    """, id, contratoId, anio, mes);
    return id;
  }

  public java.util.List<java.util.Map<String, Object>> listarAprobadasPorContrato(String contratoId) {
    return jdbc.query("""
      SELECT id, anio, mes, estado, creado_en
      FROM bitacora_mensual
      WHERE contrato_id = ?
        AND estado = 'APROBADA'
      ORDER BY anio DESC, mes DESC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("bitacoraId", rs.getString("id"));
      m.put("anio", rs.getInt("anio"));
      m.put("mes", rs.getInt("mes"));
      m.put("estado", rs.getString("estado"));
      m.put("creadoEn", rs.getString("creado_en"));
      return m;
    }, contratoId);
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

  // ✅ Estado actual por bitácora (incluye BORRADOR, ENVIADA, APROBADA, RECHAZADA)
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

  // ✅ Enviar (seguro): SOLO si está en BORRADOR o RECHAZADA
  public int enviar(String bitacoraId) {
    String estado = obtenerEstado(bitacoraId);
    if (estado == null) return 0;
    if (!"BORRADOR".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
      return 0;
    }
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET estado = 'ENVIADA'
      WHERE id = ?
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

  // ✅ Revisión del director (solo ENVIADA -> APROBADA/RECHAZADA)
  public int revisar(String bitacoraId, String nuevoEstado, String comentarioRevision) {
    String estadoActual = obtenerEstado(bitacoraId);
    if (estadoActual == null) return 0;
    if (!"ENVIADA".equalsIgnoreCase(estadoActual)) return 0;

    if (!"APROBADA".equalsIgnoreCase(nuevoEstado) && !"RECHAZADA".equalsIgnoreCase(nuevoEstado)) {
      return 0;
    }
    return jdbc.update("""
      UPDATE bitacora_mensual
      SET
        estado = ?,
        comentario_revision = ?
      WHERE id = ?
    """, nuevoEstado, comentarioRevision, bitacoraId);
  }

  // ✅ Reapertura para edición (solo RECHAZADA -> BORRADOR)
  public int reabrirRechazada(String bitacoraId) {
    String estadoActual = obtenerEstado(bitacoraId);
    if (estadoActual == null) return 0;
    if (!"RECHAZADA".equalsIgnoreCase(estadoActual)) return 0;

    return jdbc.update("""
      UPDATE bitacora_mensual
      SET estado = 'BORRADOR'
      WHERE id = ? AND estado = 'RECHAZADA'
    """, bitacoraId);
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
