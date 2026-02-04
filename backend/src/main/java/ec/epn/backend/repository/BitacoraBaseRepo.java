package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio base para operaciones CRUD de Bitácora
 */
@Repository
public class BitacoraBaseRepo {

    private final JdbcTemplate jdbc;

    public BitacoraBaseRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String crear(String contratoId, int anio, int mes) {
        String id = UUID.randomUUID().toString();
        jdbc.update("""
            INSERT INTO bitacora_mensual (id, contrato_id, anio, mes, estado)
            VALUES (?, ?, ?, ?, 'BORRADOR')
        """, id, contratoId, anio, mes);
        return id;
    }

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

    public java.util.List<java.util.Map<String, Object>> listarPorContrato(String contratoId) {
        return jdbc.query("""
            SELECT id, anio, mes, estado, creado_en, comentario_revision
            FROM bitacora_mensual
            WHERE contrato_id = ?
            ORDER BY anio DESC, mes DESC
        """, (rs, rowNum) -> {
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("bitacoraId", rs.getString("id"));
            m.put("anio", rs.getInt("anio"));
            m.put("mes", rs.getInt("mes"));
            m.put("estado", rs.getString("estado"));
            m.put("creadoEn", rs.getString("creado_en"));
            m.put("comentarioRevision", rs.getString("comentario_revision"));
            return m;
        }, contratoId);
    }
}