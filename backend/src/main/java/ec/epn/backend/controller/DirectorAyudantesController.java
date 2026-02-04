package ec.epn.backend.controller;

import ec.epn.backend.domain.Ayudante;
import ec.epn.backend.dto.RegistrarAyudanteReq;
import ec.epn.backend.repository.AyudanteRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.repository.UsuarioRepo;
import ec.epn.backend.service.NotificacionPort;
import org.springframework.web.bind.annotation.*;
import ec.epn.backend.util.PasswordGenerator;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorAyudantesController {

  private final AyudanteRepo ayudanteRepo;
  private final ContratoRepo contratoRepo;
  private final UsuarioRepo usuarioRepo;
  private final ProyectoRepo proyectoRepo;
  private final NotificacionPort notificacion;

  public DirectorAyudantesController(
      AyudanteRepo ayudanteRepo,
      ContratoRepo contratoRepo,
      UsuarioRepo usuarioRepo,
      ProyectoRepo proyectoRepo,
      NotificacionPort notificacion
  ) {
    this.ayudanteRepo = ayudanteRepo;
    this.contratoRepo = contratoRepo;
    this.usuarioRepo = usuarioRepo;
    this.proyectoRepo = proyectoRepo;
    this.notificacion = notificacion;
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

  // ✅ REGISTRAR AYUDANTE + CONTRATO + CREAR USUARIO (si no existe) + ENVIAR CORREO
  @PostMapping("/proyectos/{proyectoId}/ayudantes")
  public Object registrar(@PathVariable String proyectoId, @RequestBody RegistrarAyudanteReq req) {

    if (req == null) return Map.of("ok", false, "msg", "body requerido");

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

    String pid = proyectoId.trim();
    String correo = req.correoInstitucional().trim().toLowerCase();
    String nombres = req.nombres().trim();
    String apellidos = req.apellidos().trim();

    LocalDate fci;
    LocalDate fcf;
    try {
      fci = LocalDate.parse(req.fechaInicioContrato().trim());
      fcf = LocalDate.parse(req.fechaFinContrato().trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Fechas del contrato deben estar en formato YYYY-MM-DD");
    }

    if (fcf.isBefore(fci)) {
      return Map.of("ok", false, "msg", "fechaFinContrato no puede ser antes que fechaInicioContrato");
    }

    Integer max = proyectoRepo.obtenerMaxAyudantes(pid);
    if (max == null) return Map.of("ok", false, "msg", "Proyecto no existe");
    if (max <= 0) {
      return Map.of("ok", false, "msg", "El proyecto aún no tiene maxAyudantes definido. Complete los detalles del proyecto primero.");
    }

    int activos = contratoRepo.contarActivosPorProyecto(pid);
    if (activos >= max) {
      return Map.of("ok", false, "msg", "No se puede: cupo de ayudantes activos alcanzado", "activos", activos, "max", max);
    }

    var existente = ayudanteRepo.findByCorreoInstitucional(correo).orElse(null);
    String ayudanteId;

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
    }

    String contratoId = contratoRepo.crear(pid, ayudanteId, fci.toString(), fcf.toString());

    boolean seCreoUsuarioAyudante = false;
    String tempPass = PasswordGenerator.generar();

    if (!usuarioRepo.existsByCorreo(correo)) {
      usuarioRepo.crearUsuario(nombres, apellidos, correo, tempPass, "AYUDANTE");
      seCreoUsuarioAyudante = true;

      try {
        notificacion.enviarCredencialesTemporalesAyudante(
            correo,
            nombres,
            apellidos,
            tempPass,
            pid,
            fci.toString(),
            fcf.toString()
        );
      } catch (Exception e) {
        System.out.println("[MAIL ERROR] No se pudo enviar correo a AYUDANTE: " + e.getMessage());
      }
    }

    return Map.of(
        "ok", true,
        "ayudanteId", ayudanteId,
        "contratoId", contratoId,
        "ayudanteCreado", (existente == null),
        "usuarioAyudanteCreado", seCreoUsuarioAyudante
    );
  }
}
