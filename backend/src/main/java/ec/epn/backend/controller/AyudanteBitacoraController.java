package ec.epn.backend.controller;

import ec.epn.backend.dto.CrearActividadReq;
import ec.epn.backend.dto.CrearInformeSemanalReq;
import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.BitacoraRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

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

  // 1) Obtener o crear bitácora mensual actual (por contrato ACTIVO del ayudante logueado)
  @PostMapping("/bitacoras/actual")
  public Object obtenerBitacoraActual(Principal principal) {
    String correo = principal.getName();

    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);
    if (contratoId == null) {
      return Map.of("ok", false, "msg", "No existe contrato activo para este usuario");
    }

    String bitacoraId = bitacoraRepo.obtenerOCrearActual(contratoId);
    return Map.of("ok", true, "bitacoraId", bitacoraId);
  }

  // 2) Crear informe semanal dentro de una bitácora
  @PostMapping("/bitacoras/{bitacoraId}/semanas")
  public Object crearSemana(@PathVariable String bitacoraId, @RequestBody CrearInformeSemanalReq req, Principal principal) {
    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);
    if (contratoId == null) return Map.of("ok", false, "msg", "No existe contrato activo");

    if (!bitacoraRepo.perteneceAContrato(bitacoraId.trim(), contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado: bitácora no pertenece a tu contrato activo");
    }



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

  // 3) Crear actividad dentro de una semana (VALIDACION CORRECTA)
  @PostMapping("/semanas/{semanaId}/actividades")
  public Object crearActividad(@PathVariable String semanaId, @RequestBody CrearActividadReq req, Principal principal) {

    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);
    if (contratoId == null) return Map.of("ok", false, "msg", "No existe contrato activo");

    String bitacoraId = informeRepo.obtenerBitacoraIdPorSemana(semanaId.trim());
    if (bitacoraId == null) return Map.of("ok", false, "msg", "Semana no encontrada");

    if (!bitacoraRepo.perteneceAContrato(bitacoraId, contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado");
    }

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

    // ✅ VALIDACION CORRECTA
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

  // 4) Consultar bitácora completa (detalle + semanas + actividades)
  @GetMapping("/bitacoras/{bitacoraId}")
  public Object verBitacora(@PathVariable String bitacoraId, Principal principal) {

    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);

    if (contratoId == null) {
      return Map.of("ok", false, "msg", "No existe contrato activo para este usuario");
    }

    if (!bitacoraRepo.perteneceAContrato(bitacoraId.trim(), contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado: bitácora no pertenece a tu contrato activo");
    }

    var cabecera = bitacoraRepo.obtenerDetalle(bitacoraId.trim());
    if (cabecera == null) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    var semanas = informeRepo.listarPorBitacora(bitacoraId.trim());
    for (var s : semanas) {
      String semanaId = (String) s.get("semanaId");
      s.put("actividades", actividadRepo.listarPorSemana(semanaId));
    }

    return Map.of("ok", true, "bitacora", cabecera, "semanas", semanas);
  }


  // ✅ 3.A.4 LISTAR SEMANAS DE UNA BITÁCORA
  @GetMapping("/bitacoras/{bitacoraId}/semanas")
  public Object listarSemanas(@PathVariable String bitacoraId, Principal principal) {

    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);

    if (contratoId == null) return Map.of("ok", false, "msg", "No existe contrato activo");
    if (!bitacoraRepo.perteneceAContrato(bitacoraId.trim(), contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado");
    }

    return Map.of("ok", true, "items", informeRepo.listarPorBitacora(bitacoraId.trim()));
  }


  // ✅ 3.A.5 LISTAR ACTIVIDADES DE UNA SEMANA
  @GetMapping("/semanas/{semanaId}/actividades")
  public Object listarActividades(@PathVariable String semanaId, Principal principal) {

    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);
    if (contratoId == null) return Map.of("ok", false, "msg", "No existe contrato activo");

    String bitacoraId = informeRepo.obtenerBitacoraIdPorSemana(semanaId.trim());
    if (bitacoraId == null) return Map.of("ok", false, "msg", "Semana no encontrada");

    if (!bitacoraRepo.perteneceAContrato(bitacoraId, contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado");
    }

    return Map.of("ok", true, "items", actividadRepo.listarPorSemana(semanaId.trim()));
  }

  @PostMapping("/bitacoras/{bitacoraId}/enviar")
  public Object enviar(@PathVariable String bitacoraId, Principal principal) {

    String correo = principal.getName();
    String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo);
    if (contratoId == null) return Map.of("ok", false, "msg", "No existe contrato activo");

    if (!bitacoraRepo.perteneceAContrato(bitacoraId.trim(), contratoId)) {
      return Map.of("ok", false, "msg", "No autorizado");
    }

    String estado = bitacoraRepo.obtenerEstado(bitacoraId.trim());
    if (!"BORRADOR".equalsIgnoreCase(estado)) {
      return Map.of("ok", false, "msg", "Solo puedes enviar si está en BORRADOR", "estadoActual", estado);
    }

    int semanas = informeRepo.contarPorBitacora(bitacoraId.trim());
    if (semanas <= 0) {
      return Map.of("ok", false, "msg", "No puedes enviar: la bitácora no tiene semanas");
    }

    int n = bitacoraRepo.enviar(bitacoraId.trim());
    if (n == 0) return Map.of("ok", false, "msg", "Bitácora no encontrada");

    return Map.of("ok", true);
  }

  private String contratoActivoOrNull(String correo) {
    return contratoRepo.obtenerContratoActivoPorCorreo(correo);
  }
}
