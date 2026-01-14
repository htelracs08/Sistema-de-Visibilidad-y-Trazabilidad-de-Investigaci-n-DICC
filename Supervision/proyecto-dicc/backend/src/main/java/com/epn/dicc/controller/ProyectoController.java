package com.epn.dicc.controller;

import com.epn.dicc.dto.request.CrearProyectoRequest;
import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.EstadisticasProyectoResponse;
import com.epn.dicc.dto.response.ProyectoResponse;
import com.epn.dicc.service.IProyectoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para Proyectos
 */
@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProyectoController {

    private final IProyectoService proyectoService;

    /**
     * Crear nuevo proyecto
     * POST /api/proyectos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProyectoResponse>> crearProyecto(
            @Valid @RequestBody CrearProyectoRequest request,
            @RequestParam Long directorId) {
        
        ProyectoResponse proyecto = proyectoService.crearProyecto(request, directorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proyecto creado exitosamente", proyecto));
    }

    /**
     * Listar proyectos activos
     * GET /api/proyectos/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ProyectoResponse>>> listarActivos() {
        List<ProyectoResponse> proyectos = proyectoService.listarProyectosActivos();
        return ResponseEntity.ok(ApiResponse.success("Proyectos activos", proyectos));
    }

    /**
     * Listar proyectos por director
     * GET /api/proyectos/director/{directorId}
     */
    @GetMapping("/director/{directorId}")
    public ResponseEntity<ApiResponse<List<ProyectoResponse>>> listarPorDirector(
            @PathVariable Long directorId) {
        List<ProyectoResponse> proyectos = proyectoService.listarProyectosPorDirector(directorId);
        return ResponseEntity.ok(ApiResponse.success("Proyectos del director", proyectos));
    }

    /**
     * Obtener proyecto por ID
     * GET /api/proyectos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProyectoResponse>> obtenerPorId(@PathVariable Long id) {
        ProyectoResponse proyecto = proyectoService.obtenerProyectoPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Proyecto encontrado", proyecto));
    }

    /**
     * Obtener proyecto por código
     * GET /api/proyectos/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<ProyectoResponse>> obtenerPorCodigo(@PathVariable String codigo) {
        ProyectoResponse proyecto = proyectoService.obtenerProyectoPorCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.success("Proyecto encontrado", proyecto));
    }

    /**
     * Obtener estadísticas del proyecto
     * GET /api/proyectos/{id}/estadisticas
     */
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasProyectoResponse>> obtenerEstadisticas(
            @PathVariable Long id) {
        EstadisticasProyectoResponse stats = proyectoService.obtenerEstadisticas(id);
        return ResponseEntity.ok(ApiResponse.success("Estadísticas del proyecto", stats));
    }

    /**
     * Finalizar proyecto
     * PUT /api/proyectos/{id}/finalizar
     */
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<ApiResponse<Void>> finalizar(@PathVariable Long id) {
        proyectoService.finalizarProyecto(id, null);
        return ResponseEntity.ok(ApiResponse.success("Proyecto finalizado", null));
    }

    /**
     * Avanzar semestre del proyecto
     * PUT /api/proyectos/{id}/avanzar-semestre
     */
    @PutMapping("/{id}/avanzar-semestre")
    public ResponseEntity<ApiResponse<Void>> avanzarSemestre(@PathVariable Long id) {
        proyectoService.avanzarSemestreProyecto(id);
        return ResponseEntity.ok(ApiResponse.success("Semestre avanzado", null));
    }
}