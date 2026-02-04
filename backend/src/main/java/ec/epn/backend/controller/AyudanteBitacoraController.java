package ec.epn.backend.controller;

import ec.epn.backend.dto.CrearActividadReq;
import ec.epn.backend.dto.CrearInformeSemanalReq;
import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.BitacoraBaseRepo;
import ec.epn.backend.repository.BitacoraConsultasRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import ec.epn.backend.service.BitacoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ayudante")
public class AyudanteBitacoraController {

    private final BitacoraService bitacoraService;
    private final BitacoraBaseRepo bitacoraBaseRepo;
    private final BitacoraConsultasRepo bitacoraConsultasRepo;
    private final ContratoRepo contratoRepo;
    private final InformeSemanalRepo informeRepo;
    private final ActividadRepo actividadRepo;

    public AyudanteBitacoraController(
        BitacoraService bitacoraService,
        BitacoraBaseRepo bitacoraBaseRepo,
        BitacoraConsultasRepo bitacoraConsultasRepo,
        ContratoRepo contratoRepo,
        InformeSemanalRepo informeRepo,
        ActividadRepo actividadRepo
    ) {
        this.bitacoraService = bitacoraService;
        this.bitacoraBaseRepo = bitacoraBaseRepo;
        this.bitacoraConsultasRepo = bitacoraConsultasRepo;
        this.contratoRepo = contratoRepo;
        this.informeRepo = informeRepo;
        this.actividadRepo = actividadRepo;
    }

    @PostMapping("/bitacoras/actual")
    public ResponseEntity<?> obtenerBitacoraActual(Principal principal) {
        String bitacoraId = bitacoraService.obtenerBitacoraActual(principal.getName());
        return ResponseEntity.ok(Map.of("ok", true, "bitacoraId", bitacoraId));
    }

    @GetMapping("/bitacoras/aprobadas")
    public ResponseEntity<?> listarAprobadas(Principal principal) {
        String correo = principal.getName();
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo.trim().toLowerCase());

        if (contratoId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "No existe contrato activo"));
        }

        var rows = bitacoraConsultasRepo.listarAprobadasPorContrato(contratoId);
        return ResponseEntity.ok(Map.of("ok", true, "bitacoras", rows));
    }

    @GetMapping("/bitacoras/historial")
    public ResponseEntity<?> listarHistorial(Principal principal) {
        String correo = principal.getName();
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo.trim().toLowerCase());

        if (contratoId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "No existe contrato activo"));
        }

        var rows = bitacoraBaseRepo.listarPorContrato(contratoId);
        return ResponseEntity.ok(Map.of("ok", true, "bitacoras", rows));
    }

    @PostMapping("/bitacoras/{bitacoraId}/semanas")
    public ResponseEntity<?> crearSemana(
        @PathVariable String bitacoraId,
        @RequestBody CrearInformeSemanalReq req,
        Principal principal
    ) {
        bitacoraService.validarBitacoraEditable(bitacoraId.trim(), principal.getName());

        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "body requerido"));
        }
        if (req.fechaInicioSemana() == null || req.fechaInicioSemana().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "fechaInicioSemana requerido"));
        }
        if (req.fechaFinSemana() == null || req.fechaFinSemana().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "fechaFinSemana requerido"));
        }
        if (req.actividadesRealizadas() == null || req.actividadesRealizadas().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "actividadesRealizadas requerido"));
        }

        LocalDate fi = LocalDate.parse(req.fechaInicioSemana().trim());
        LocalDate ff = LocalDate.parse(req.fechaFinSemana().trim());

        if (ff.isBefore(fi)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "fechaFinSemana no puede ser anterior"));
        }

        String semanaId = informeRepo.crear(
            bitacoraId.trim(),
            fi.toString(),
            ff.toString(),
            req.actividadesRealizadas().trim(),
            req.observaciones(),
            req.anexos()
        );

        return ResponseEntity.ok(Map.of("ok", true, "semanaId", semanaId));
    }

    @PostMapping("/semanas/{semanaId}/actividades")
    public ResponseEntity<?> crearActividad(
        @PathVariable String semanaId,
        @RequestBody CrearActividadReq req,
        Principal principal
    ) {
        String bitacoraId = informeRepo.obtenerBitacoraIdPorSemana(semanaId.trim());
        if (bitacoraId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "Semana no encontrada"));
        }

        bitacoraService.validarBitacoraEditable(bitacoraId, principal.getName());

        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "body requerido"));
        }
        if (req.horaInicio() == null || req.horaInicio().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "horaInicio requerido"));
        }
        if (req.horaSalida() == null || req.horaSalida().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "horaSalida requerido"));
        }
        if (req.descripcion() == null || req.descripcion().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "descripcion requerido"));
        }

        LocalTime hi = LocalTime.parse(req.horaInicio().trim());
        LocalTime hs = LocalTime.parse(req.horaSalida().trim());

        if (!hs.isAfter(hi)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "horaSalida debe ser mayor"));
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

        return ResponseEntity.ok(Map.of("ok", true, "actividadId", actividadId, "totalHoras", totalHoras));
    }

    public record ActualizarActividadReq(String horaInicio, String horaFin, String descripcion) {}

    @PutMapping("/actividades/{actividadId}")
    public ResponseEntity<?> actualizarActividad(
        @PathVariable String actividadId,
        @RequestBody ActualizarActividadReq req,
        Principal principal
    ) {
        String bitacoraId = actividadRepo.obtenerBitacoraIdPorActividad(actividadId.trim());
        if (bitacoraId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "Actividad no encontrada"));
        }

        bitacoraService.validarBitacoraEditable(bitacoraId, principal.getName());

        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "body requerido"));
        }

        LocalTime hi = LocalTime.parse(req.horaInicio().trim());
        LocalTime hf = LocalTime.parse(req.horaFin().trim());

        if (!hf.isAfter(hi)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "horaFin debe ser mayor"));
        }

        actividadRepo.updateActividad(actividadId.trim(), hi.toString(), hf.toString(), req.descripcion().trim());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/bitacoras/{bitacoraId}")
    public ResponseEntity<?> verBitacora(@PathVariable String bitacoraId, Principal principal) {
        String correo = principal.getName();
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(correo.trim().toLowerCase());

        if (contratoId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "No existe contrato activo"));
        }

        var cabecera = bitacoraBaseRepo.obtenerDetalle(bitacoraId.trim());
        if (cabecera == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "Bitácora no encontrada"));
        }

        var semanas = informeRepo.listarPorBitacora(bitacoraId.trim());
        for (var s : semanas) {
            String semanaId = (String) s.get("semanaId");
            s.put("actividades", actividadRepo.listarPorSemana(semanaId));
        }

        String contratoIdBitacora = (String) cabecera.get("contratoId");
        var estudiante = contratoRepo.obtenerEstudiantePorContrato(contratoIdBitacora);
        if (estudiante == null) {
            estudiante = Map.of("nombres", "-", "apellidos", "-", "correoInstitucional", "-");
        }

        cabecera.put("estudiante", estudiante);
        return ResponseEntity.ok(Map.of("ok", true, "bitacora", cabecera, "semanas", semanas));
    }

    @GetMapping("/bitacoras/{bitacoraId}/semanas")
    public ResponseEntity<?> listarSemanas(@PathVariable String bitacoraId) {
        return ResponseEntity.ok(Map.of("ok", true, "items", informeRepo.listarPorBitacora(bitacoraId.trim())));
    }

    @GetMapping("/semanas/{semanaId}/actividades")
    public ResponseEntity<?> listarActividades(@PathVariable String semanaId) {
        return ResponseEntity.ok(Map.of("ok", true, "items", actividadRepo.listarPorSemana(semanaId.trim())));
    }

    @PostMapping("/bitacoras/{bitacoraId}/enviar")
    public ResponseEntity<?> enviar(@PathVariable String bitacoraId, Principal principal) {
        bitacoraService.enviarBitacora(bitacoraId.trim(), principal.getName());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/bitacoras/{bitacoraId}/reabrir")
    public ResponseEntity<?> reabrir(@PathVariable String bitacoraId, Principal principal) {
        bitacoraService.reabrirBitacora(bitacoraId.trim(), principal.getName());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}