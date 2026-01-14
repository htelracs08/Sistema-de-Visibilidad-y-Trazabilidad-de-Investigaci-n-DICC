package com.epn.dicc.controller;

import com.epn.dicc.dto.request.AprobacionContratoRequest;
import com.epn.dicc.dto.request.RegistrarRenunciaRequest;
import com.epn.dicc.dto.request.SolicitudIngresoRequest;
import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.ContratoResponse;
import com.epn.dicc.service.IContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para Contratos
 */
@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContratoController {

    private final IContratoService contratoService;

    /**
     * Solicitar ingreso a proyecto
     * POST /api/contratos/solicitar
     */
    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<ContratoResponse>> solicitarIngreso(
            @Valid @RequestBody SolicitudIngresoRequest request,
            @RequestParam Long ayudanteId) {
        
        ContratoResponse contrato = contratoService.solicitarIngreso(ayudanteId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solicitud enviada exitosamente", contrato));
    }

    /**
     * Aprobar contrato
     * POST /api/contratos/aprobar
     */
    @PostMapping("/aprobar")
    public ResponseEntity<ApiResponse<ContratoResponse>> aprobar(
            @Valid @RequestBody AprobacionContratoRequest request,
            @RequestParam Long directorId) {
        
        ContratoResponse contrato = contratoService.aprobarContrato(request, directorId);
        return ResponseEntity.ok(ApiResponse.success("Contrato aprobado exitosamente", contrato));
    }

    /**
     * Rechazar contrato
     * POST /api/contratos/{id}/rechazar
     */
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<Void>> rechazar(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestParam Long directorId) {
        
        contratoService.rechazarContrato(id, motivo, directorId);
        return ResponseEntity.ok(ApiResponse.success("Contrato rechazado", null));
    }

    /**
     * Registrar renuncia
     * POST /api/contratos/{id}/renuncia
     */
    @PostMapping("/{id}/renuncia")
    public ResponseEntity<ApiResponse<Void>> registrarRenuncia(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarRenunciaRequest request) {
        
        contratoService.registrarRenuncia(id, request.getFechaRenuncia(), request.getMotivo());
        return ResponseEntity.ok(ApiResponse.success("Renuncia registrada", null));
    }

    /**
     * Listar solicitudes pendientes de un proyecto
     * GET /api/contratos/pendientes/proyecto/{proyectoId}
     */
    @GetMapping("/pendientes/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<ContratoResponse>>> listarPendientes(
            @PathVariable Long proyectoId) {
        
        List<ContratoResponse> contratos = contratoService.listarSolicitudesPendientes(proyectoId);
        return ResponseEntity.ok(ApiResponse.success("Solicitudes pendientes", contratos));
    }

    /**
     * Listar contratos de un ayudante
     * GET /api/contratos/ayudante/{ayudanteId}
     */
    @GetMapping("/ayudante/{ayudanteId}")
    public ResponseEntity<ApiResponse<List<ContratoResponse>>> listarPorAyudante(
            @PathVariable Long ayudanteId) {
        
        List<ContratoResponse> contratos = contratoService.listarContratosPorAyudante(ayudanteId);
        return ResponseEntity.ok(ApiResponse.success("Contratos del ayudante", contratos));
    }

    /**
     * Listar contratos de un proyecto
     * GET /api/contratos/proyecto/{proyectoId}
     */
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<ContratoResponse>>> listarPorProyecto(
            @PathVariable Long proyectoId) {
        
        List<ContratoResponse> contratos = contratoService.listarContratosPorProyecto(proyectoId);
        return ResponseEntity.ok(ApiResponse.success("Contratos del proyecto", contratos));
    }

    /**
     * Obtener contrato por ID
     * GET /api/contratos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContratoResponse>> obtenerPorId(@PathVariable Long id) {
        ContratoResponse contrato = contratoService.obtenerContratoPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Contrato encontrado", contrato));
    }
}