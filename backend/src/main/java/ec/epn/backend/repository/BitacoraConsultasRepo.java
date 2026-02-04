package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repositorio para consultas complejas de Bitácora
 */
@Repository
public class BitacoraConsultasRepo {

    private final JdbcTemplate jdbc;

    public BitacoraConsultasRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int contarAprobadasEnRango(
        String contratoId,
        int anioDesde,
        int mesDesde,
        int anioHasta,
        int mesHasta
    ) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(1)
            FROM bitacora_mensual
            WHERE contrato_id = ?
              AND estado = 'APROBADA'
              AND (
                (anio > ? OR (anio = ? AND mes >= ?))
                AND
                (anio < ? OR (anio = ? AND mes <= ?))
              )
        """, Integer.class, 
            contratoId, 
            anioDesde, anioDesde, mesDesde, 
            anioHasta, anioHasta, mesHasta
        );
        return count == null ? 0 : count;
    }

    public List<Map<String, Object>> listarPendientesParaDirector(
        String proyectoId,
        String correoDirector
    ) {
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

    public List<Map<String, Object>> listarTodasParaDirector(
        String proyectoId,
        String correoDirector
    ) {
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
              a.correo_institucional
            FROM bitacora_mensual b
            JOIN contrato c ON c.id = b.contrato_id
            JOIN proyecto p ON p.id = c.proyecto_id
            JOIN ayudante a ON a.id = c.ayudante_id
            WHERE p.id = ?
              AND lower(p.director_correo) = lower(?)
            ORDER BY b.estado DESC, b.creado_en DESC
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
            return m;
        }, proyectoId, correoDirector);
    }

    public List<Map<String, Object>> listarAprobadasPorContrato(String contratoId) {
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

    public Map<String, Object> obtenerDetalleParaDirector(String bitacoraId, String correoDirector) {
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
}