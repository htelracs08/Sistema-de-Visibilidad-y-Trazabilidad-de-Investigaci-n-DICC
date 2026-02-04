package ec.epn.backend.controller;

import ec.epn.backend.dto.RegistrarAyudanteReq;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.service.ContratoService;  // ✅ NUEVO
import ec.epn.backend.service.dto.RegistrarAyudanteCommand;  // ✅ NUEVO
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorAyudantesController {

    private final ContratoService contratoService;  // ✅ NUEVO
    private final ContratoRepo contratoRepo;

    public DirectorAyudantesController(
        ContratoService contratoService,  // ✅ NUEVO
        ContratoRepo contratoRepo
    ) {
        this.contratoService = contratoService;
        this.contratoRepo = contratoRepo;
    }

    @GetMapping("/proyectos/{proyectoId}/ayudantes")
    public ResponseEntity<?> listar(@PathVariable String proyectoId) {
        return ResponseEntity.ok(contratoRepo.listarPorProyecto(proyectoId.trim()));
    }

    public record FinalizarContratoReq(String motivo) {}

    // ✅ MÉTODO REFACTORIZADO
    @PostMapping("/contratos/{contratoId}/finalizar")
    public ResponseEntity<?> finalizar(
        @PathVariable String contratoId, 
        @RequestBody FinalizarContratoReq req
    ) {
        contratoService.finalizarContrato(contratoId.trim(), req.motivo());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ✅ MÉTODO REFACTORIZADO
    @PostMapping("/proyectos/{proyectoId}/ayudantes")
    public ResponseEntity<?> registrar(
        @PathVariable String proyectoId, 
        @RequestBody RegistrarAyudanteReq req
    ) {
        LocalDate fechaInicio = LocalDate.parse(req.fechaInicioContrato().trim());
        LocalDate fechaFin = LocalDate.parse(req.fechaFinContrato().trim());

        var comando = new RegistrarAyudanteCommand(
            req.nombres(),
            req.apellidos(),
            req.correoInstitucional(),
            req.facultad(),
            req.quintil(),
            req.tipoAyudante(),
            fechaInicio,
            fechaFin
        );

        var resultado = contratoService.registrarAyudante(proyectoId.trim(), comando);

        return ResponseEntity.ok(Map.of(
            "ok", true,
            "ayudanteId", resultado.ayudanteId(),
            "contratoId", resultado.contratoId(),
            "ayudanteCreado", resultado.ayudanteCreado(),
            "usuarioAyudanteCreado", resultado.usuarioCreado()
        ));
    }
}