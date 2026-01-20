package ec.epn.backend.controller;

import ec.epn.backend.repository.BitacoraRepo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorBitacoraController {

  private final BitacoraRepo bitacoraRepo;

  public DirectorBitacoraController(BitacoraRepo bitacoraRepo) {
    this.bitacoraRepo = bitacoraRepo;
  }

  public record RevisarBitacoraReq(String decision, String observacion) {}

  @PostMapping("/bitacoras/{bitacoraId}/revisar")
  public Object revisar(@PathVariable String bitacoraId, @RequestBody RevisarBitacoraReq req) {

    if (req == null || req.decision() == null || req.decision().isBlank()) {
      return Map.of("ok", false, "msg", "decision es requerido (APROBAR | RECHAZAR)");
    }

    String decision = req.decision().trim().toUpperCase();
    String nuevoEstado;

    if ("APROBAR".equals(decision)) nuevoEstado = "APROBADA";
    else if ("RECHAZAR".equals(decision)) nuevoEstado = "RECHAZADA";
    else return Map.of("ok", false, "msg", "decision inválida (APROBAR | RECHAZAR)");

    // ✅ Regla recomendada: solo se revisa si está ENVIADA
    String estadoActual;
    try {
      estadoActual = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Bitácora no encontrada");
    }

    if (!"ENVIADA".equalsIgnoreCase(estadoActual)) {
      return Map.of("ok", false, "msg", "Solo se puede revisar bitácoras en estado ENVIADA", "estadoActual", estadoActual);
    }

    int n = bitacoraRepo.revisar(
      bitacoraId.trim(),
      nuevoEstado,
      req.observacion() == null ? null : req.observacion().trim()
    );

    if (n == 0) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    return Map.of("ok", true, "nuevoEstado", nuevoEstado);
  }
}
