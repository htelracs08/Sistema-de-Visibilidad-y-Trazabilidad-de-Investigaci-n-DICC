package ec.epn.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProyectoRepo {

  private final JdbcTemplate jdbc;

  public ProyectoRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String crear(String codigo, String nombre, String correoDirector) {
    String id = UUID.randomUUID().toString();
    jdbc.update("""
      INSERT INTO proyecto (id, codigo, nombre, director_correo, activo)
      VALUES (?, ?, ?, ?, 1)
      """, id, codigo, nombre, correoDirector);
    return id;
  }

  public java.util.List<java.util.Map<String, Object>> findAll() {
    return jdbc.query("""
        SELECT id, codigo, nombre, director_correo, activo, tipo, subtipo, creado_en
        FROM proyecto
        ORDER BY creado_en DESC
        """,
      (rs, rowNum) -> java.util.Map.<String, Object>of(
        "id", rs.getString("id"),
        "codigo", rs.getString("codigo"),
        "nombre", rs.getString("nombre"),
        "correoDirector", rs.getString("director_correo"),
        "activo", rs.getInt("activo") == 1,
        "tipo", rs.getString("tipo") != null ? rs.getString("tipo") : "",
        "subtipo", rs.getString("subtipo") != null ? rs.getString("subtipo") : "",
        "creadoEn", rs.getString("creado_en")
      )
    );
  }

  public java.util.List<java.util.Map<String, Object>> listarResumen() {
    return jdbc.query("""
      SELECT
        p.id AS proyecto_id,
        p.codigo,
        p.nombre,
        p.director_correo AS correo_director,
        p.activo,
        p.tipo,
        p.subtipo,

        -- ayudantes activos
        (SELECT COUNT(1)
        FROM contrato c
        WHERE c.proyecto_id = p.id AND c.estado = 'ACTIVO') AS ayudantes_activos,

        -- contratos totales (histórico)
        (SELECT COUNT(1)
        FROM contrato c
        WHERE c.proyecto_id = p.id) AS contratos_total

      FROM proyecto p
      ORDER BY p.creado_en DESC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("proyectoId", rs.getString("proyecto_id"));
      m.put("codigo", rs.getString("codigo"));
      m.put("nombre", rs.getString("nombre"));
      m.put("correoDirector", rs.getString("correo_director"));
      m.put("activo", rs.getInt("activo") == 1);
      m.put("tipoProyecto", rs.getString("tipo"));
      m.put("subtipoProyecto", rs.getString("subtipo"));
      m.put("ayudantesActivos", rs.getInt("ayudantes_activos"));
      m.put("contratosTotal", rs.getInt("contratos_total"));
      return m;
    });
  }

  public java.util.List<java.util.Map<String, Object>> estadisticasPorTipoYEstado() {
    return jdbc.query("""
      SELECT
        COALESCE(tipo, 'SIN_TIPO') AS tipo,
        activo,
        COUNT(1) AS total
      FROM proyecto
      GROUP BY COALESCE(tipo, 'SIN_TIPO'), activo
      ORDER BY tipo, activo DESC
    """, (rs, rowNum) -> {
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("tipo", rs.getString("tipo"));
      m.put("activo", rs.getInt("activo") == 1);
      m.put("total", rs.getInt("total"));
      return m;
    });
  }

  public int actualizarDetalles(String proyectoId,
                                String fechaInicio,
                                String fechaFin,
                                String tipo,
                                String subtipo,
                                Integer maxAyudantes,
                                Integer maxArticulos) {
    return jdbc.update("""
      UPDATE proyecto
      SET
        fecha_inicio = ?,
        fecha_fin = ?,
        tipo = ?,
        subtipo = ?,
        max_ayudantes = ?,
        max_articulos = ?
      WHERE id = ?
    """, fechaInicio, fechaFin, tipo, subtipo, maxAyudantes, maxArticulos, proyectoId);
  }
  
  public Integer obtenerMaxAyudantes(String proyectoId) {
    return jdbc.queryForObject("""
      SELECT max_ayudantes
      FROM proyecto
      WHERE id = ?
    """, Integer.class, proyectoId);
  }

  public java.util.Map<String, Object> obtenerBasicoPorId(String proyectoId) {
    return jdbc.query("""
      SELECT id, codigo, nombre
      FROM proyecto
      WHERE id = ?
      LIMIT 1
    """, rs -> {
      if (!rs.next()) return null;
      var m = new java.util.LinkedHashMap<String, Object>();
      m.put("id", rs.getString("id"));
      m.put("codigo", rs.getString("codigo"));
      m.put("nombre", rs.getString("nombre"));
      return m;
    }, proyectoId);
  }

}