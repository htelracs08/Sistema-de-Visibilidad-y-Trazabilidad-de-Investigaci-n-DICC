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
      id, proyectoId, ayudanteId, fechaInicio, fechaFin
    );
    return id;
  }
}