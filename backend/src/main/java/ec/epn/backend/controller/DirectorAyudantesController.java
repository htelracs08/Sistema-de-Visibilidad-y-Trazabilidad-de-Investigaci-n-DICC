package ec.epn.backend.controller;

import ec.epn.backend.domain.Ayudante;
import ec.epn.backend.dto.RegistrarAyudanteReq;
import ec.epn.backend.repository.AyudanteRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.UsuarioRepo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorAyudantesController {

  private final AyudanteRepo ayudanteRepo;
  private final ContratoRepo contratoRepo;
  private final UsuarioRepo usuarioRepo;

  // POR AHORA: límite fijo (luego lo tomamos del proyecto: max_ayudantes)
  private static final int LIMITE_AYUDANTES_ACTIVOS = 2;

  public DirectorAyudantesController(AyudanteRepo ayudanteRepo, ContratoRepo contratoRepo, UsuarioRepo usuarioRepo) {
    this.ayudanteRepo = ayudanteRepo;
    this.contratoRepo = contratoRepo;
    this.usuarioRepo = usuarioRepo;
  }

  @GetMapping("/proyectos/{proyectoId}/ayudantes")
  public Object listar(@PathVariable String proyectoId) {
    return contratoRepo.listarPorProyecto(proyectoId.trim());
  }

  public record FinalizarContratoReq(String motivo) {}

  @PostMapping("/contratos/{contratoId}/finalizar")
  public Object finalizar(@PathVariable String contratoId, @RequestBody FinalizarContratoReq req) {
    if (req == null || req.motivo() == null || req.motivo().isBlank()) {
      return Map.of("ok", false, "msg", "motivo es requerido (RENUNCIA | FIN_CONTRATO | DESPIDO)");
    }
    contratoRepo.finalizar(contratoId.trim(), req.motivo().trim());
    return Map.of("ok", true);
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

    // Normalizar
    String pid = proyectoId.trim();
    String correo = req.correoInstitucional().trim().toLowerCase();

    // Validar cupo (por ahora fijo)
    int activos = contratoRepo.contarActivosPorProyecto(pid);
    if (activos >= LIMITE_AYUDANTES_ACTIVOS) {
      return Map.of("ok", false, "msg", "No se puede: cupo de ayudantes activos alcanzado", "activos", activos);
    }

    // Crear o reutilizar Ayudante
    var existente = ayudanteRepo.findByCorreoInstitucional(correo).orElse(null);
    String ayudanteId;

    String nombres = req.nombres().trim();
    String apellidos = req.apellidos().trim();

    if (existente == null) {
      ayudanteId = ayudanteRepo.crear(new Ayudante(
        null,
        nombres,
        apellidos,
        correo,
        req.facultad().trim(),
        req.quintil(),
        req.tipoAyudante().trim()
      ));
    } else {
      ayudanteId = existente.id();
      // si ya existe en BD, igual nos quedamos con los nombres/apellidos del request para crear usuario si falta
    }

    // Crear contrato ACTIVO
    String contratoId = contratoRepo.crear(
      pid,
      ayudanteId,
      req.fechaInicioContrato().trim(),
      req.fechaFinContrato().trim()
    );

    // ✅ NUEVO: crear usuario AYUDANTE solo si NO existe (recontratación)
    if (!usuarioRepo.existsByCorreo(correo)) {
      String tempPass = "Temp123*"; // luego lo hacemos random
      usuarioRepo.crearUsuario(nombres, apellidos, correo, tempPass, "AYUDANTE");

      // por ahora solo log (luego Outlook SMTP)
      System.out.println("[CREDENCIALES AYUDANTE] correo=" + correo + " pass=" + tempPass);
    }

    return Map.of("ok", true, "ayudanteId", ayudanteId, "contratoId", contratoId);
  }
}
