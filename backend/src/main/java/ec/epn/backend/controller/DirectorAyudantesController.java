package ec.epn.backend.controller;

import ec.epn.backend.domain.Ayudante;
import ec.epn.backend.dto.RegistrarAyudanteReq;
import ec.epn.backend.repository.AyudanteRepo;
import ec.epn.backend.repository.ContratoRepo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorAyudantesController {

  private final AyudanteRepo ayudanteRepo;
  private final ContratoRepo contratoRepo;

  // POR AHORA: límite fijo (luego lo tomamos del proyecto: max_ayudantes)
  private static final int LIMITE_AYUDANTES_ACTIVOS = 2;

  public DirectorAyudantesController(AyudanteRepo ayudanteRepo, ContratoRepo contratoRepo) {
    this.ayudanteRepo = ayudanteRepo;
    this.contratoRepo = contratoRepo;
  }

  @PostMapping("/proyectos/{proyectoId}/ayudantes")
  public Object registrar(@PathVariable String proyectoId, @RequestBody RegistrarAyudanteReq req) {

    // Validaciones mínimas
    if (req.correoInstitucional() == null || req.correoInstitucional().isBlank()) {
      return Map.of("ok", false, "msg", "correoInstitucional es requerido");
    }
    if (req.nombres() == null || req.nombres().isBlank()) {
      return Map.of("ok", false, "msg", "nombres es requerido");
    }
    if (req.apellidos() == null || req.apellidos().isBlank()) {
      return Map.of("ok", false, "msg", "apellidos es requerido");
    }
    if (req.facultad() == null || req.facultad().isBlank()) {
      return Map.of("ok", false, "msg", "facultad es requerido");
    }
    if (req.quintil() == null) {
      return Map.of("ok", false, "msg", "quintil es requerido");
    }
    if (req.tipoAyudante() == null || req.tipoAyudante().isBlank()) {
      return Map.of("ok", false, "msg", "tipoAyudante es requerido");
    }
    if (req.fechaInicioContrato() == null || req.fechaInicioContrato().isBlank()) {
      return Map.of("ok", false, "msg", "fechaInicioContrato es requerido (YYYY-MM-DD)");
    }
    if (req.fechaFinContrato() == null || req.fechaFinContrato().isBlank()) {
      return Map.of("ok", false, "msg", "fechaFinContrato es requerido (YYYY-MM-DD)");
    }

    // Validar cupo (por ahora fijo)
    int activos = contratoRepo.contarActivosPorProyecto(proyectoId);
    if (activos >= LIMITE_AYUDANTES_ACTIVOS) {
      return Map.of("ok", false, "msg", "No se puede: cupo de ayudantes activos alcanzado", "activos", activos);
    }

    String correo = req.correoInstitucional().trim().toLowerCase();

    // Crear o reutilizar Ayudante
    var existente = ayudanteRepo.findByCorreoInstitucional(correo).orElse(null);
    String ayudanteId;
    if (existente == null) {
      ayudanteId = ayudanteRepo.crear(new Ayudante(
        null,
        req.nombres().trim(),
        req.apellidos().trim(),
        correo,
        req.facultad().trim(),
        req.quintil(),
        req.tipoAyudante().trim()
      ));
    } else {
      ayudanteId = existente.id();
    }

    // Crear contrato ACTIVO
    String contratoId = contratoRepo.crear(proyectoId, ayudanteId, req.fechaInicioContrato().trim(), req.fechaFinContrato().trim());

    return Map.of("ok", true, "ayudanteId", ayudanteId, "contratoId", contratoId);
  }
}