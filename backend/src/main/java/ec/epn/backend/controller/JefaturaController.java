package ec.epn.backend.controller;

import ec.epn.backend.dto.CrearProyectoReq;
import ec.epn.backend.repository.BitacoraConsultasRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.ProfesorRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.service.ProyectoService;
import ec.epn.backend.service.dto.CrearProyectoCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/jefatura")
public class JefaturaController {

    private final ProyectoService proyectoService;
    private final ProyectoRepo proyectoRepo;
    private final ProfesorRepo profesorRepo;
    private final ContratoRepo contratoRepo;
    private final BitacoraConsultasRepo bitacoraConsultasRepo;

    public JefaturaController(
        ProyectoService proyectoService,
        ProyectoRepo proyectoRepo,
        ProfesorRepo profesorRepo,
        ContratoRepo contratoRepo,
        BitacoraConsultasRepo bitacoraConsultasRepo
    ) {
        this.proyectoService = proyectoService;
        this.proyectoRepo = proyectoRepo;
        this.profesorRepo = profesorRepo;
        this.contratoRepo = contratoRepo;
        this.bitacoraConsultasRepo = bitacoraConsultasRepo;
    }

    @PostMapping("/proyectos")
    public ResponseEntity<?> crearProyecto(@RequestBody CrearProyectoReq req) {
        try {
            var comando = CrearProyectoCommand.fromRequest(
                req.codigo(),
                req.nombre(),
                req.correoDirector(),
                req.getTipoProyecto(),
                req.getSubtipoProyecto()
            );

            var resultado = proyectoService.crearProyecto(comando);

            return ResponseEntity.ok(Map.of(
                "ok", true,
                "proyectoId", resultado.proyectoId(),
                "directorCreado", resultado.directorCreado()
            ));
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("ok", false, "msg", e.getMessage()));
        }
    }

    @GetMapping("/profesores")
    public ResponseEntity<?> listarProfesores() {
        return ResponseEntity.ok(profesorRepo.findAll());
    }

    @GetMapping("/proyectos")
    public ResponseEntity<?> listarProyectos() {
        return ResponseEntity.ok(proyectoRepo.findAll());
    }

    @GetMapping("/proyectos/resumen")
    public ResponseEntity<?> resumenProyectos() {
        return ResponseEntity.ok(proyectoRepo.listarResumen());
    }

    @GetMapping("/ayudantes/activos")
    public ResponseEntity<?> totalActivos() {
        int total = contratoRepo.contarActivosGlobal();
        return ResponseEntity.ok(Map.of("ok", true, "activos", total));
    }

    @GetMapping("/proyectos/{proyectoId}/ayudantes")
    public ResponseEntity<?> listarAyudantesProyecto(@PathVariable String proyectoId) {
        return ResponseEntity.ok(contratoRepo.listarPorProyecto(proyectoId.trim()));
    }

    @GetMapping("/proyectos/estadisticas")
    public ResponseEntity<?> estadisticasProyectos() {
        return ResponseEntity.ok(proyectoRepo.estadisticasPorTipoYEstado());
    }

    @GetMapping("/ayudantes/estadisticas")
    public ResponseEntity<?> estadisticasAyudantes() {
        int activosTotal = contratoRepo.contarActivosGlobal();
        var porTipo = contratoRepo.contarActivosPorTipoAyudante();
        return ResponseEntity.ok(Map.of("ok", true, "activosTotal", activosTotal, "porTipo", porTipo));
    }

    @GetMapping("/semaforo")
    public ResponseEntity<?> semaforo() {
        var contratos = contratoRepo.listarActivosDetallado();
        var hoy = java.time.LocalDate.now();

        var out = new java.util.ArrayList<Map<String, Object>>();

        for (var c : contratos) {
            String contratoId = (String) c.get("contratoId");
            var fechaInicio = java.time.LocalDate.parse((String) c.get("fechaInicio"));

            int anioDesde = fechaInicio.getYear();
            int mesDesde = fechaInicio.getMonthValue();
            int anioHasta = hoy.getYear();
            int mesHasta = hoy.getMonthValue();

            int mesesEsperados = ((anioHasta - anioDesde) * 12) + (mesHasta - mesDesde) + 1;
            if (mesesEsperados < 0) mesesEsperados = 0;

            int mesesAprobados = bitacoraConsultasRepo.contarAprobadasEnRango(
                contratoId, anioDesde, mesDesde, anioHasta, mesHasta
            );
            
            int faltantes = Math.max(0, mesesEsperados - mesesAprobados);

            String color = (faltantes == 0) ? "VERDE" : (faltantes == 1 ? "AMARILLO" : "ROJO");

            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("contratoId", c.get("contratoId"));
            m.put("proyectoId", c.get("proyectoId"));
            m.put("proyectoCodigo", c.get("proyectoCodigo"));
            m.put("proyectoNombre", c.get("proyectoNombre"));
            m.put("fechaInicio", c.get("fechaInicio"));
            m.put("fechaFin", c.get("fechaFin"));
            m.put("nombres", c.get("ayudanteNombres"));
            m.put("apellidos", c.get("ayudanteApellidos"));
            m.put("correoInstitucional", c.get("correoInstitucional"));
            m.put("anioDesde", anioDesde);
            m.put("mesDesde", mesDesde);
            m.put("anioHasta", anioHasta);
            m.put("mesHasta", mesHasta);
            m.put("mesesEsperados", mesesEsperados);
            m.put("mesesAprobados", mesesAprobados);
            m.put("faltantes", faltantes);
            m.put("color", color);

            out.add(m);
        }

        return ResponseEntity.ok(out);
    }
}