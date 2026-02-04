package ec.epn.backend.controller;

import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.BitacoraRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorBitacoraController {

  private final BitacoraRepo bitacoraRepo;
  private final InformeSemanalRepo informeRepo;
  private final ActividadRepo actividadRepo;

  public DirectorBitacoraController(BitacoraRepo bitacoraRepo, InformeSemanalRepo informeRepo, ActividadRepo actividadRepo) {
    this.bitacoraRepo = bitacoraRepo;
    this.informeRepo = informeRepo;
    this.actividadRepo = actividadRepo;
  }

  @GetMapping("/bitacoras/{bitacoraId}")
  public Object verBitacora(@PathVariable String bitacoraId, Principal principal) {
    String correoDirector = principal.getName() == null ? "" : principal.getName().trim().toLowerCase();

    var cabecera = bitacoraRepo.obtenerDetalleParaDirector(bitacoraId.trim(), correoDirector);
    if (cabecera == null) {
      return Map.of("ok", false, "msg", "Bitácora no encontrada o no autorizada");
    }

    var semanas = informeRepo.listarPorBitacora(bitacoraId.trim());
    for (var s : semanas) {
      String semanaId = (String) s.get("semanaId");
      s.put("actividades", actividadRepo.listarPorSemana(semanaId));
    }

    return Map.of("ok", true, "bitacora", cabecera, "semanas", semanas);
  }

  public record RevisarBitacoraReq(String decision, String observacion) {}

  @PostMapping("/bitacoras/{bitacoraId}/revisar")
  public Object revisar(@PathVariable String bitacoraId,
                        @RequestBody RevisarBitacoraReq req,
                        Principal principal) {

    if (req == null || req.decision() == null || req.decision().isBlank()) {
      return Map.of("ok", false, "msg", "decision es requerido (APROBAR | RECHAZAR)");
    }

    String correoDirector = principal.getName() == null ? "" : principal.getName().trim().toLowerCase();

    // Seguridad: solo si pertenece a su proyecto
    if (!bitacoraRepo.directorPuedeRevisarBitacora(bitacoraId.trim(), correoDirector)) {
      return Map.of("ok", false, "msg", "No autorizado: no puedes revisar esta bitácora");
    }

    String decision = req.decision().trim().toUpperCase();
    String nuevoEstado;

    if ("APROBAR".equals(decision)) {
      nuevoEstado = "APROBADA";       // bloqueada
    } else if ("RECHAZAR".equals(decision)) {
      nuevoEstado = "RECHAZADA";      // queda pendiente de corrección
    } else {
      return Map.of("ok", false, "msg", "decision inválida (APROBAR | RECHAZAR)");
    }

    String estadoActual;
    try {
      estadoActual = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Bitácora no encontrada");
    }

    // Solo se revisa si está ENVIADA
    if (!"ENVIADA".equalsIgnoreCase(estadoActual)) {
      return Map.of("ok", false, "msg", "Solo se puede revisar bitácoras en estado ENVIADA", "estadoActual", estadoActual);
    }

    int n = bitacoraRepo.revisar(
      bitacoraId.trim(),
      nuevoEstado,
      (req.observacion() == null || req.observacion().isBlank()) ? null : req.observacion().trim()
    );

    if (n == 0) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    return Map.of("ok", true, "nuevoEstado", nuevoEstado);
  }

  @GetMapping("/proyectos/{proyectoId}/bitacoras/pendientes")
  public Object pendientes(@PathVariable String proyectoId, Principal principal) {
    String correoDirector = principal.getName() == null ? "" : principal.getName().trim().toLowerCase();
    
    List<Map<String, Object>> lista = bitacoraRepo.listarPendientesPorProyectoParaDirector(proyectoId.trim(), correoDirector);
    
    return lista;
  }

  // ✅ NUEVO ENDPOINT: Historial de bitácoras aprobadas del proyecto
  @GetMapping("/proyectos/{proyectoId}/bitacoras/aprobadas")
  public Object aprobadasDelProyecto(@PathVariable String proyectoId, Principal principal) {
    String correoDirector = principal.getName() == null ? "" : principal.getName().trim().toLowerCase();
    
    List<Map<String, Object>> lista = bitacoraRepo.listarAprobadasPorProyectoParaDirector(proyectoId.trim(), correoDirector);
    
    return lista;
  }
}