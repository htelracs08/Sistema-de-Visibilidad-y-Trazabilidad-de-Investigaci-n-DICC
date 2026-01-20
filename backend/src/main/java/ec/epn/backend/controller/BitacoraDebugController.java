package ec.epn.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/debug/bitacora")
public class BitacoraDebugController {

  private final JdbcTemplate jdbc;

  public BitacoraDebugController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Inserta (o ignora si ya existe) una bitácora mensual APROBADA
   * SOLO PARA PRUEBAS DE SEMÁFORO
   */
  @PostMapping("/aprobar")
  public Object aprobar(@RequestBody Map<String, Object> body) {

    String contratoId = (String) body.get("contratoId");
    Integer anio = (Integer) body.get("anio");
    Integer mes = (Integer) body.get("mes");

    if (contratoId == null || anio == null || mes == null) {
      return Map.of(
        "ok", false,
        "msg", "Se requiere contratoId, anio y mes"
      );
    }

    if (mes < 1 || mes > 12) {
      return Map.of(
        "ok", false,
        "msg", "mes debe estar entre 1 y 12"
      );
    }

    jdbc.update("""
      INSERT OR IGNORE INTO bitacora_mensual
      (id, contrato_id, anio, mes, estado)
      VALUES (?, ?, ?, ?, 'APROBADA')
    """,
      UUID.randomUUID().toString(),
      contratoId,
      anio,
      mes
    );

    return Map.of(
      "ok", true,
      "contratoId", contratoId,
      "anio", anio,
      "mes", mes,
      "estado", "APROBADA"
    );
  }
}
