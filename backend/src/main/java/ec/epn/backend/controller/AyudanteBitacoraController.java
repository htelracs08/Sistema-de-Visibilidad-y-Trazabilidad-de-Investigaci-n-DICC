package ec.epn.backend.controller;

import ec.epn.backend.dto.CrearActividadReq;
import ec.epn.backend.dto.CrearInformeSemanalReq;
import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.BitacoraRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ayudante")
public class AyudanteBitacoraController {

  private final ContratoRepo contratoRepo;
  private final BitacoraRepo bitacoraRepo;
  private final InformeSemanalRepo informeRepo;
  private final ActividadRepo actividadRepo;

  public AyudanteBitacoraController(
      ContratoRepo contratoRepo,
      BitacoraRepo bitacoraRepo,
      InformeSemanalRepo informeRepo,
      ActividadRepo actividadRepo
  ) {
    this.contratoRepo = contratoRepo;
    this.bitacoraRepo = bitacoraRepo;
    this.informeRepo = informeRepo;
    this.actividadRepo = actividadRepo;
  }

  // =========================
  // Helpers
  // =========================

  private String correo(Principal principal) {
    return principal == null ? null : principal.getName();
  }

  private String contratoActivoOrNull(String correo) {
    if (correo == null || correo.isBlank()) return null;
    return contratoRepo.obtenerContratoActivoPorCorreo(correo.trim().toLowerCase());
  }

  private Object noContrato() {
    return Map.of("ok", false, "msg", "No existe contrato activo para este usuario");
  }

  private Object noAutorizado() {
    return Map.of("ok", false, "msg", "No autorizado");
  }

  private Object validarQueBitacoraEsDelContratoActivo(String bitacoraId, String contratoId) {
    if (bitacoraId == null || bitacoraId.isBlank()) return Map.of("ok", false, "msg", "bitacoraId requerido");
    if (contratoId == null || contratoId.isBlank()) return noContrato();

    var detalle = bitacoraRepo.obtenerDetalle(bitacoraId.trim());
    if (detalle == null) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    String contratoBitacora = (String) detalle.get("contratoId");
    if (contratoBitacora == null || !contratoBitacora.equalsIgnoreCase(contratoId.trim())) {
      return Map.of("ok", false, "msg", "No autorizado: bitácora no pertenece a tu contrato activo");
    }
    return null; // válido
  }

  private Object validarBitacoraEditable(String bitacoraId) {
    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"BORRADOR".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "estadoActual", estado, "msg", "Solo puedes editar si está en BORRADOR o RECHAZADA");
    }
    return null;
  }

  // =========================
  // 1) Obtener o crear bitácora actual
  // =========================
  @PostMapping("/bitacoras/actual")
  public Object obtenerBitacoraActual(Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);

    String bitacoraId = bitacoraRepo.obtenerOCrearActual(contratoId);
    return Map.of("ok", true, "bitacoraId", bitacoraId);
  }

  // =========================
  // 1b) Listar bitácoras aprobadas (solo para imprimir/ver)
  // =========================
  @GetMapping("/bitacoras/aprobadas")
  public Object listarAprobadas(Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    var rows = bitacoraRepo.listarAprobadasPorContrato(contratoId);
    return Map.of("ok", true, "bitacoras", rows);
  }

  // =========================
  // 2) Crear semana (solo si BORRADOR + pertenece a su contrato)
  // =========================
  @PostMapping("/bitacoras/{bitacoraId}/semanas")
  public Object crearSemana(@PathVariable String bitacoraId,
                            @RequestBody CrearInformeSemanalReq req,
                            Principal principal) {

    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    Object err;
    // seguridad: bitácora debe pertenecer al contrato activo
    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"RECHAZADA".equalsIgnoreCase(estado)) {
      err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
      if (err != null) return err;
    }

    // bloqueo: SOLO BORRADOR (antes de insertar)
    err = validarBitacoraEditable(bitacoraId);
    if (err != null) return err;

    // validaciones body
    if (req == null) return Map.of("ok", false, "msg", "body requerido");
    if (req.fechaInicioSemana() == null || req.fechaInicioSemana().isBlank()) {
      return Map.of("ok", false, "msg", "fechaInicioSemana es requerido (YYYY-MM-DD)");
    }
    if (req.fechaFinSemana() == null || req.fechaFinSemana().isBlank()) {
      return Map.of("ok", false, "msg", "fechaFinSemana es requerido (YYYY-MM-DD)");
    }
    if (req.actividadesRealizadas() == null || req.actividadesRealizadas().isBlank()) {
      return Map.of("ok", false, "msg", "actividadesRealizadas es requerido");
    }

    LocalDate fi;
    LocalDate ff;
    try {
      fi = LocalDate.parse(req.fechaInicioSemana().trim());
      ff = LocalDate.parse(req.fechaFinSemana().trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Formato de fecha inválido. Usa YYYY-MM-DD");
    }

    if (ff.isBefore(fi)) {
      return Map.of("ok", false, "msg", "fechaFinSemana no puede ser anterior a fechaInicioSemana");
    }

    String semanaId;
    try {
      semanaId = informeRepo.crear(
          bitacoraId.trim(),
          fi.toString(),
          ff.toString(),
          req.actividadesRealizadas().trim(),
          req.observaciones(),
          req.anexos()
      );
    } catch (IllegalStateException e) {
      return Map.of("ok", false, "msg", e.getMessage());
    }

    if (semanaId == null) {
      return Map.of("ok", false, "msg", "No se pudo crear la semana");
    }

    return Map.of("ok", true, "semanaId", semanaId);
  }

  // =========================
  // 3) Crear actividad (solo si BORRADOR + pertenece a su contrato)
  // =========================
  @PostMapping("/semanas/{semanaId}/actividades")
  public Object crearActividad(@PathVariable String semanaId,
                               @RequestBody CrearActividadReq req,
                               Principal principal) {

    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    if (semanaId == null || semanaId.isBlank()) {
      return Map.of("ok", false, "msg", "semanaId requerido");
    }

    // ubicar bitacoraId desde semana
    String bitacoraId = informeRepo.obtenerBitacoraIdPorSemana(semanaId.trim());
    if (bitacoraId == null) return Map.of("ok", false, "msg", "Semana no encontrada");

    // seguridad: la bitácora de esa semana debe ser del contrato activo
    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    // bloqueo: SOLO BORRADOR
    err = validarBitacoraEditable(bitacoraId);
    if (err != null) return err;

    // validaciones body
    if (req == null) return Map.of("ok", false, "msg", "body requerido");
    if (req.horaInicio() == null || req.horaInicio().isBlank()) {
      return Map.of("ok", false, "msg", "horaInicio es requerido (HH:mm)");
    }
    if (req.horaSalida() == null || req.horaSalida().isBlank()) {
      return Map.of("ok", false, "msg", "horaSalida es requerido (HH:mm)");
    }
    if (req.descripcion() == null || req.descripcion().isBlank()) {
      return Map.of("ok", false, "msg", "descripcion es requerido");
    }

    LocalTime hi, hs;
    try {
      hi = LocalTime.parse(req.horaInicio().trim());
      hs = LocalTime.parse(req.horaSalida().trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Formato de hora inválido. Usa HH:mm (ej: 08:30)");
    }

    if (!hs.isAfter(hi)) {
      return Map.of("ok", false, "msg", "horaSalida debe ser mayor que horaInicio");
    }

    long minutos = Duration.between(hi, hs).toMinutes();
    double totalHoras = minutos / 60.0;

    String actividadId;
    try {
      actividadId = actividadRepo.crear(
          semanaId.trim(),
          hi.toString(),
          hs.toString(),
          totalHoras,
          req.descripcion().trim()
      );
    } catch (IllegalStateException e) {
      return Map.of("ok", false, "msg", e.getMessage());
    }

    if (actividadId == null) {
      return Map.of("ok", false, "msg", "No se pudo crear la actividad");
    }

    return Map.of("ok", true, "actividadId", actividadId, "totalHoras", totalHoras);
  }

  public record ActualizarActividadReq(String horaInicio, String horaFin, String descripcion) {}

  // =========================
  // 3.B) Actualizar actividad (solo si BORRADOR o RECHAZADA + pertenece a su contrato)
  // =========================
  @PutMapping("/actividades/{actividadId}")
  public Object actualizarActividad(@PathVariable String actividadId,
                                    @RequestBody ActualizarActividadReq req,
                                    Principal principal) {

    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    if (actividadId == null || actividadId.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("ok", false, "msg", "actividadId requerido"));
    }

    String bitacoraId = actividadRepo.obtenerBitacoraIdPorActividad(actividadId.trim());
    if (bitacoraId == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("ok", false, "msg", "Actividad no encontrada"));
    }

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    err = validarBitacoraEditable(bitacoraId);
    if (err != null) return err;

    if (req == null) return Map.of("ok", false, "msg", "body requerido");
    if (req.horaInicio() == null || req.horaInicio().isBlank()) {
      return Map.of("ok", false, "msg", "horaInicio es requerido (HH:mm)");
    }
    if (req.horaFin() == null || req.horaFin().isBlank()) {
      return Map.of("ok", false, "msg", "horaFin es requerido (HH:mm)");
    }
    if (req.descripcion() == null || req.descripcion().isBlank()) {
      return Map.of("ok", false, "msg", "descripcion es requerido");
    }

    LocalTime hi, hf;
    try {
      hi = LocalTime.parse(req.horaInicio().trim());
      hf = LocalTime.parse(req.horaFin().trim());
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Formato de hora inválido. Usa HH:mm (ej: 08:30)");
    }

    if (!hf.isAfter(hi)) {
      return Map.of("ok", false, "msg", "horaFin debe ser mayor que horaInicio");
    }

    int n = actividadRepo.updateActividad(
        actividadId.trim(),
        hi.toString(),
        hf.toString(),
        req.descripcion().trim()
    );

    if (n == 0) return Map.of("ok", false, "msg", "Actividad no encontrada");

    return Map.of("ok", true);
  }

  // =========================
  // 4) Ver bitácora completa (cabecera + semanas + actividades)
  // =========================
  @GetMapping("/bitacoras/{bitacoraId}")
  public Object verBitacora(@PathVariable String bitacoraId, Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    var cabecera = bitacoraRepo.obtenerDetalle(bitacoraId.trim());
    if (cabecera == null) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    var semanas = informeRepo.listarPorBitacora(bitacoraId.trim());
    for (var s : semanas) {
      String semanaId = (String) s.get("semanaId");
      s.put("actividades", actividadRepo.listarPorSemana(semanaId));
    }

    String contratoIdBitacora = (String) cabecera.get("contratoId");
    var estudiante = contratoIdBitacora == null ? null : contratoRepo.obtenerEstudiantePorContrato(contratoIdBitacora);
    if (estudiante == null) {
      estudiante = new java.util.LinkedHashMap<String, Object>();
      estudiante.put("nombres", "-");
      estudiante.put("apellidos", "-");
      estudiante.put("correoInstitucional", "-");
    } else {
      estudiante.putIfAbsent("nombres", "-");
      estudiante.putIfAbsent("apellidos", "-");
      estudiante.putIfAbsent("correoInstitucional", "-");
    }

    cabecera.put("estudiante", estudiante);

    var resp = new java.util.LinkedHashMap<String, Object>();
    resp.put("ok", true);
    resp.put("bitacora", cabecera);
    resp.put("semanas", semanas);
    return resp;
  }

  // =========================
  // 3.A.4 Listar semanas de una bitácora
  // =========================
  @GetMapping("/bitacoras/{bitacoraId}/semanas")
  public Object listarSemanas(@PathVariable String bitacoraId, Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    return Map.of("ok", true, "items", informeRepo.listarPorBitacora(bitacoraId.trim()));
  }

  // =========================
  // 3.A.5 Listar actividades de una semana
  // =========================
  @GetMapping("/semanas/{semanaId}/actividades")
  public Object listarActividades(@PathVariable String semanaId, Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    if (semanaId == null || semanaId.isBlank()) {
      return Map.of("ok", false, "msg", "semanaId requerido");
    }

    String bitacoraId = informeRepo.obtenerBitacoraIdPorSemana(semanaId.trim());
    if (bitacoraId == null) return Map.of("ok", false, "msg", "Semana no encontrada");

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    return Map.of("ok", true, "items", actividadRepo.listarPorSemana(semanaId.trim()));
  }

  // =========================
  // Enviar bitácora (solo si BORRADOR y tiene semanas y actividades)
  // =========================
  @PostMapping("/bitacoras/{bitacoraId}/enviar")
  public Object enviar(@PathVariable String bitacoraId, Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    // 1) Solo BORRADOR o RECHAZADA
    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"BORRADOR".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "estadoActual", estado, "msg", "Solo puedes enviar si está en BORRADOR o RECHAZADA");
    }

    // 2) Debe tener semanas
    int semanas = informeRepo.contarPorBitacora(bitacoraId.trim());
    if (semanas <= 0) {
      return Map.of("ok", false, "msg", "No puedes enviar: la bitácora no tiene semanas");
    }

    // 3) Debe tener actividades
    int actividades = actividadRepo.contarPorBitacora(bitacoraId.trim());
    if (actividades <= 0) {
      return Map.of("ok", false, "msg", "No puedes enviar: la bitácora no tiene actividades");
    }

    // 4) Recién aquí cambiamos estado
    int n = bitacoraRepo.enviar(bitacoraId.trim());
    if (n == 0) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    return Map.of("ok", true);
  }

  // =========================
  // Reabrir bitácora rechazada (RECHAZADA -> BORRADOR)
  // =========================
  @PostMapping("/bitacoras/{bitacoraId}/reabrir")
  public Object reabrir(@PathVariable String bitacoraId, Principal principal) {
    String correo = correo(principal);
    if (correo == null || correo.isBlank()) return noAutorizado();

    String contratoId = contratoActivoOrNull(correo);
    if (contratoId == null) return noContrato();

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"RECHAZADA".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "estadoActual", estado, "msg", "Solo puedes reabrir si está en RECHAZADA");
    }

    int n = bitacoraRepo.reabrirRechazada(bitacoraId.trim());
    if (n == 0) return Map.of("ok", false, "msg", "Bitácora no encontrada o no reabierta");

    return Map.of("ok", true);
  }
}
