package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ContratoRepo {

  private final JdbcTemplate jdbc;

  public ContratoRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int contarActivosPorProyecto(String proyectoId) {
    Integer count = jdbc.queryForObject("""
        SELECT COUNT(1)
        FROM contrato
        WHERE proyecto_id = ? AND estado = 'ACTIVO'
        """, Integer.class, proyectoId);
    return count == null ? 0 : count;
  }

  public String crear(String proyectoId, String ayudanteId, String fechaInicio, String fechaFin) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
        INSERT INTO contrato (id, proyecto_id, ayudante_id, fecha_inicio, fecha_fin, estado)
        VALUES (?, ?, ?, ?, ?, 'ACTIVO')
        """,
        id, proyectoId, ayudanteId, fechaInicio, fechaFin);
    return id;
  }

  public java.util.List<java.util.Map<String, Object>> listarPorProyecto(String proyectoId) {
    return jdbc.query("""
          SELECT
            c.id AS contrato_id,
            c.proyecto_id,
            c.fecha_inicio,
            c.fecha_fin,
            c.estado,
            c.motivo_inactivo,
            a.id AS ayudante_id,
            a.nombres,
            a.apellidos,
            a.correo_institucional,
            a.facultad,
            a.quintil,
            a.tipo_ayudante
          FROM contrato c
          JOIN ayudante a ON a.id = c.ayudante_id
          WHERE c.proyecto_id = ?
          ORDER BY c.creado_en DESC
        """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("proyectoId", rs.getString("proyecto_id"));
      m.put("fechaInicio", rs.getString("fecha_inicio"));
      m.put("fechaFin", rs.getString("fecha_fin"));
      m.put("estado", rs.getString("estado"));
      m.put("motivoInactivo", rs.getString("motivo_inactivo"));
      m.put("ayudanteId", rs.getString("ayudante_id"));
      m.put("nombres", rs.getString("nombres"));
      m.put("apellidos", rs.getString("apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      m.put("facultad", rs.getString("facultad"));
      m.put("quintil", rs.getInt("quintil"));
      m.put("tipoAyudante", rs.getString("tipo_ayudante"));
      return m;
    },
        proyectoId);
  }

  public void finalizar(String contratoId, String motivo) {
    jdbc.update("""
      UPDATE contrato
      SET estado = 'INACTIVO', motivo_inactivo = ?
      WHERE id = ?
    """, motivo, contratoId);
  }

  public int contarActivosGlobal() {
    Integer n = jdbc.queryForObject("""
      SELECT COUNT(1)
      FROM contrato
      WHERE estado='ACTIVO'
    """, Integer.class);
    return n == null ? 0 : n;
  }

  public java.util.List<java.util.Map<String, Object>> contarActivosPorTipoAyudante() {
    return jdbc.query("""
      SELECT
        a.tipo_ayudante AS tipo,
        COUNT(1) AS total
      FROM contrato c
      JOIN ayudante a ON a.id = c.ayudante_id
      WHERE c.estado = 'ACTIVO'
      GROUP BY a.tipo_ayudante
      ORDER BY total DESC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("tipo", rs.getString("tipo"));
      m.put("total", rs.getInt("total"));
      return m;
    });
  }

  public java.util.List<java.util.Map<String, Object>> listarActivosDetallado() {
    return jdbc.query("""
      SELECT
        c.id AS contrato_id,
        c.proyecto_id,
        c.ayudante_id,
        c.fecha_inicio,
        c.fecha_fin,
        p.codigo AS proyecto_codigo,
        p.nombre AS proyecto_nombre,
        a.nombres AS ayudante_nombres,
        a.apellidos AS ayudante_apellidos,
        a.correo_institucional
      FROM contrato c
      JOIN proyecto p ON p.id = c.proyecto_id
      JOIN ayudante a ON a.id = c.ayudante_id
      WHERE c.estado = 'ACTIVO'
      ORDER BY p.creado_en DESC, a.apellidos ASC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("contratoId", rs.getString("contrato_id"));
      m.put("proyectoId", rs.getString("proyecto_id"));
      m.put("ayudanteId", rs.getString("ayudante_id"));
      m.put("fechaInicio", rs.getString("fecha_inicio"));
      m.put("fechaFin", rs.getString("fecha_fin"));
      m.put("proyectoCodigo", rs.getString("proyecto_codigo"));
      m.put("proyectoNombre", rs.getString("proyecto_nombre"));
      m.put("ayudanteNombres", rs.getString("ayudante_nombres"));
      m.put("ayudanteApellidos", rs.getString("ayudante_apellidos"));
      m.put("correoInstitucional", rs.getString("correo_institucional"));
      return m;
    });
  }

  public String obtenerContratoActivoIdPorCorreo(String correoInstitucional) {
    return jdbc.query("""
        SELECT c.id
        FROM contrato c
        JOIN ayudante a ON a.id = c.ayudante_id
        WHERE a.correo_institucional = ?
          AND c.estado = 'ACTIVO'
        ORDER BY c.creado_en DESC
        LIMIT 1
      """,
      (rs) -> rs.next() ? rs.getString("id") : null,
      correoInstitucional
    );
  }

  public String obtenerContratoActivoPorCorreo(String correoInstitucional) {
    return jdbc.query("""
        SELECT c.id
        FROM contrato c
        JOIN ayudante a ON a.id = c.ayudante_id
        WHERE a.correo_institucional = ?
          AND c.estado = 'ACTIVO'
        ORDER BY c.creado_en DESC
        LIMIT 1
      """,
      rs -> rs.next() ? rs.getString("id") : null,
      correoInstitucional.trim().toLowerCase()
    );
  }


}