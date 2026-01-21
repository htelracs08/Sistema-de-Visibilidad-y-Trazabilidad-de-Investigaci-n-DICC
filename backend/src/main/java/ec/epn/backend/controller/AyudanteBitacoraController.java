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

    boolean ok = bitacoraRepo.perteneceAContrato(bitacoraId.trim(), contratoId.trim());
    if (!ok) return Map.of("ok", false, "msg", "No autorizado: bitácora no pertenece a tu contrato activo");
    return null; // válido
  }

  private Object validarBitacoraEnBorrador(String bitacoraId) {
    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"BORRADOR".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "estadoActual", estado, "msg", "Solo puedes editar si está en BORRADOR");
    }
    return null;
  }

  // =========================
  // 1) Obtener o crear bitácora actual
  // =========================
  @PostMapping("/bitacoras/actual")
  public Object obtenerBitacoraActual(Principal principal) {
    String contratoId = contratoActivoOrNull(correo(principal));
    if (contratoId == null) return noContrato();

    String bitacoraId = bitacoraRepo.obtenerOCrearActual(contratoId);
    return Map.of("ok", true, "bitacoraId", bitacoraId);
  }

  // =========================
  // 2) Crear semana (solo si BORRADOR + pertenece a su contrato)
  // =========================
  @PostMapping("/bitacoras/{bitacoraId}/semanas")
  public Object crearSemana(@PathVariable String bitacoraId,
                            @RequestBody CrearInformeSemanalReq req,
                            Principal principal) {

    String contratoId = contratoActivoOrNull(correo(principal));
    if (contratoId == null) return noContrato();

    // seguridad: bitácora debe pertenecer al contrato activo
    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    // bloqueo: SOLO BORRADOR (antes de insertar)
    err = validarBitacoraEnBorrador(bitacoraId);
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

    String semanaId = informeRepo.crear(
        bitacoraId.trim(),
        fi.toString(),
        ff.toString(),
        req.actividadesRealizadas().trim(),
        req.observaciones(),
        req.anexos()
    );

    return Map.of("ok", true, "semanaId", semanaId);
  }

  // =========================
  // 3) Crear actividad (solo si BORRADOR + pertenece a su contrato)
  // =========================
  @PostMapping("/semanas/{semanaId}/actividades")
  public Object crearActividad(@PathVariable String semanaId,
                               @RequestBody CrearActividadReq req,
                               Principal principal) {

    String contratoId = contratoActivoOrNull(correo(principal));
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
    err = validarBitacoraEnBorrador(bitacoraId);
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

    String actividadId = actividadRepo.crear(
        semanaId.trim(),
        hi.toString(),
        hs.toString(),
        totalHoras,
        req.descripcion().trim()
    );

    return Map.of("ok", true, "actividadId", actividadId, "totalHoras", totalHoras);
  }

  // =========================
  // 4) Ver bitácora completa (cabecera + semanas + actividades)
  // =========================
  @GetMapping("/bitacoras/{bitacoraId}")
  public Object verBitacora(@PathVariable String bitacoraId, Principal principal) {
    String contratoId = contratoActivoOrNull(correo(principal));
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

    return Map.of("ok", true, "bitacora", cabecera, "semanas", semanas);
  }

  // =========================
  // 3.A.4 Listar semanas de una bitácora
  // =========================
  @GetMapping("/bitacoras/{bitacoraId}/semanas")
  public Object listarSemanas(@PathVariable String bitacoraId, Principal principal) {
    String contratoId = contratoActivoOrNull(correo(principal));
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
    String contratoId = contratoActivoOrNull(correo(principal));
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
    String contratoId = contratoActivoOrNull(correo(principal));
    if (contratoId == null) return noContrato();

    Object err = validarQueBitacoraEsDelContratoActivo(bitacoraId, contratoId);
    if (err != null) return err;

    // 1) Solo BORRADOR
    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"BORRADOR".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "estadoActual", estado, "msg", "Solo puedes enviar si está en BORRADOR");
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
}
