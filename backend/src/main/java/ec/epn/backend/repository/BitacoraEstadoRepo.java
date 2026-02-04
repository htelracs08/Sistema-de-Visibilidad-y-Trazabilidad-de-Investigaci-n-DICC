package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestión de estados de Bitácora
 */
@Repository
public class BitacoraEstadoRepo {

    private final JdbcTemplate jdbc;

    public BitacoraEstadoRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String obtenerEstado(String bitacoraId) {
        return jdbc.queryForObject("""
            SELECT estado
            FROM bitacora_mensual
            WHERE id = ?
        """, String.class, bitacoraId);
    }

    public void cambiarEstado(String bitacoraId, String nuevoEstado) {
        jdbc.update("""
            UPDATE bitacora_mensual
            SET estado = ?
            WHERE id = ?
        """, nuevoEstado, bitacoraId);
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

    public int enviar(String bitacoraId) {
        return jdbc.update("""
            UPDATE bitacora_mensual
            SET estado = 'ENVIADA', comentario_revision = NULL
            WHERE id = ? AND estado IN ('BORRADOR', 'RECHAZADA')
        """, bitacoraId);
    }

    public int revisar(String bitacoraId, String nuevoEstado, String comentario) {
        return jdbc.update("""
            UPDATE bitacora_mensual
            SET estado = ?, comentario_revision = ?
            WHERE id = ? AND estado = 'ENVIADA'
        """, nuevoEstado, comentario, bitacoraId);
    }

    public int reabrirRechazada(String bitacoraId) {
        return jdbc.update("""
            UPDATE bitacora_mensual
            SET estado = 'BORRADOR'
            WHERE id = ? AND estado = 'RECHAZADA'
        """, bitacoraId);
    }
}