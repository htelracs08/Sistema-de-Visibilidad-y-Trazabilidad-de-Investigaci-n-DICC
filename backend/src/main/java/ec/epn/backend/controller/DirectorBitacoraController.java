package ec.epn.backend.controller;

import ec.epn.backend.repository.ActividadRepo;
import ec.epn.backend.repository.InformeSemanalRepo;
import ec.epn.backend.repository.BitacoraBaseRepo;
import ec.epn.backend.repository.BitacoraEstadoRepo;
import ec.epn.backend.repository.BitacoraConsultasRepo;
import ec.epn.backend.service.AutorizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorBitacoraController {

    private final BitacoraBaseRepo bitacoraBaseRepo;
    private final BitacoraEstadoRepo bitacoraEstadoRepo;
    private final BitacoraConsultasRepo bitacoraConsultasRepo;
    private final AutorizacionService autorizacionService;
    private final InformeSemanalRepo informeRepo;
    private final ActividadRepo actividadRepo;

    public DirectorBitacoraController(
        BitacoraBaseRepo bitacoraBaseRepo,
        BitacoraEstadoRepo bitacoraEstadoRepo,
        BitacoraConsultasRepo bitacoraConsultasRepo,
        AutorizacionService autorizacionService,
        InformeSemanalRepo informeRepo,
        ActividadRepo actividadRepo
    ) {
        this.bitacoraBaseRepo = bitacoraBaseRepo;
        this.bitacoraEstadoRepo = bitacoraEstadoRepo;
        this.bitacoraConsultasRepo = bitacoraConsultasRepo;
        this.autorizacionService = autorizacionService;
        this.informeRepo = informeRepo;
        this.actividadRepo = actividadRepo;
    }

    @GetMapping("/bitacoras/{bitacoraId}")
    public ResponseEntity<?> verBitacora(
        @PathVariable String bitacoraId, 
        Principal principal
    ) {
        String correoDirector = principal.getName().trim().toLowerCase();

        var cabecera = bitacoraConsultasRepo.obtenerDetalleParaDirector(
            bitacoraId.trim(), 
            correoDirector
        );
        
        if (cabecera == null) {
            return ResponseEntity
                .status(403)
                .body(Map.of("ok", false, "msg", "Bitácora no encontrada o no autorizada"));
        }

        var semanas = informeRepo.listarPorBitacora(bitacoraId.trim());
        for (var s : semanas) {
            String semanaId = (String) s.get("semanaId");
            s.put("actividades", actividadRepo.listarPorSemana(semanaId));
        }

        return ResponseEntity.ok(Map.of("ok", true, "bitacora", cabecera, "semanas", semanas));
    }

    public record RevisarBitacoraReq(String decision, String observacion) {}

    @PostMapping("/bitacoras/{bitacoraId}/revisar")
    public ResponseEntity<?> revisar(
        @PathVariable String bitacoraId,
        @RequestBody RevisarBitacoraReq req,
        Principal principal
    ) {
        String correoDirector = principal.getName().trim().toLowerCase();

        try {
            // Validar autorización
            autorizacionService.verificarDirectorPuedeRevisarBitacora(
                correoDirector, 
                bitacoraId.trim()
            );

            String decision = req.decision().trim().toUpperCase();
            String nuevoEstado;

            if ("APROBAR".equals(decision)) {
                nuevoEstado = "APROBADA";
            } else if ("RECHAZAR".equals(decision)) {
                nuevoEstado = "RECHAZADA";
            } else {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("ok", false, "msg", "decision inválida (APROBAR | RECHAZAR)"));
            }

            String comentario = (req.observacion() == null || req.observacion().isBlank()) 
                ? null 
                : req.observacion().trim();

            int n = bitacoraEstadoRepo.revisar(bitacoraId.trim(), nuevoEstado, comentario);

            if (n == 0) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("ok", false, "msg", "Bitácora no encontrada o no se pudo revisar"));
            }

            return ResponseEntity.ok(Map.of("ok", true, "nuevoEstado", nuevoEstado));

        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("ok", false, "msg", e.getMessage()));
        }
    }

    @GetMapping("/proyectos/{proyectoId}/bitacoras/pendientes")
    public ResponseEntity<?> pendientes(
        @PathVariable String proyectoId, 
        Principal principal
    ) {
        String correoDirector = principal.getName().trim().toLowerCase();

        var lista = bitacoraConsultasRepo.listarPendientesParaDirector(
            proyectoId.trim(), 
            correoDirector
        );

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/proyectos/{proyectoId}/bitacoras/todas")
    public ResponseEntity<?> todas(
        @PathVariable String proyectoId, 
        Principal principal
    ) {
        String correoDirector = principal.getName().trim().toLowerCase();

        var lista = bitacoraConsultasRepo.listarTodasParaDirector(
            proyectoId.trim(), 
            correoDirector
        );

        return ResponseEntity.ok(lista);
    }
}