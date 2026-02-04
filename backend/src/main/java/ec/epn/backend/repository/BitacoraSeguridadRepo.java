package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para validaciones de seguridad de Bitácora
 */
@Repository
public class BitacoraSeguridadRepo {

    private final JdbcTemplate jdbc;

    public BitacoraSeguridadRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean perteneceAContrato(String bitacoraId, String contratoId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(1)
            FROM bitacora_mensual
            WHERE id = ? AND contrato_id = ?
        """, Integer.class, bitacoraId, contratoId);
        return count != null && count > 0;
    }

    public boolean directorPuedeRevisarBitacora(String bitacoraId, String correoDirector) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(1)
            FROM bitacora_mensual b
            JOIN contrato c ON c.id = b.contrato_id
            JOIN proyecto p ON p.id = c.proyecto_id
            WHERE b.id = ?
              AND lower(p.director_correo) = lower(?)
        """, Integer.class, bitacoraId, correoDirector);
        return count != null && count > 0;
    }
}