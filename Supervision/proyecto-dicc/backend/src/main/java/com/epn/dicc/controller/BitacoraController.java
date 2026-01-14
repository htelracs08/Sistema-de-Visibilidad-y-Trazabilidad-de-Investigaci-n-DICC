package com.epn.dicc.controller;

import com.epn.dicc.dto.request.AgregarActividadRequest;
import com.epn.dicc.dto.request.CrearBitacoraRequest;
import com.epn.dicc.dto.request.RevisionBitacoraRequest;
import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.BitacoraResponse;
import com.epn.dicc.service.IBitacoraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para Bitácoras
 */
@RestController
@RequestMapping("/api/bitacoras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BitacoraController {

    private final IBitacoraService bitacoraService;

    /**
     * Crear nueva bitácora
     * POST /api/bitacoras
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BitacoraResponse>> crear(
            @Valid @RequestBody CrearBitacoraRequest request) {
        
        BitacoraResponse bitacora = bitacoraService.crearBitacora(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bitácora creada exitosamente", bitacora));
    }

    /**
     * Agregar actividad a bitácora
     * POST /api/bitacoras/actividades
     */
    @PostMapping("/actividades")
    public ResponseEntity<ApiResponse<Void>> agregarActividad(
            @Valid @RequestBody AgregarActividadRequest request) {
        
        bitacoraService.agregarActividad(request);
        return ResponseEntity.ok(ApiResponse.success("Actividad agregada", null));
    }

    /**
     * Enviar bitácora a revisión
     * POST /api/bitacoras/{id}/enviar
     */
    @PostMapping("/{id}/enviar")
    public ResponseEntity<ApiResponse<Void>> enviarRevision(@PathVariable Long id) {
        bitacoraService.enviarRevision(id);
        return ResponseEntity.ok(ApiResponse.success("Bitácora enviada a revisión", null));
    }

    /**
     * Revisar bitácora (aprobar/rechazar/modificar)
     * POST /api/bitacoras/{id}/revisar
     */
    @PostMapping("/{id}/revisar")
    public ResponseEntity<ApiResponse<Void>> revisar(
            @PathVariable Long id,
            @Valid @RequestBody RevisionBitacoraRequest request,
            @RequestParam Long docenteId) {
        
        switch (request.getAccion().toUpperCase()) {
            case "APROBAR":
                bitacoraService.aprobarBitacora(id, request.getComentariosDirector(), docenteId);
                break;
            case "RECHAZAR":
                bitacoraService.rechazarBitacora(id, request.getComentariosDirector(), docenteId);
                break;
            case "MODIFICAR":
                bitacoraService.solicitarModificacion(id, request.getComentariosDirector(), docenteId);
                break;
            default:
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Acción inválida"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Bitácora revisada", null));
    }

    /**
     * Eliminar actividad
     * DELETE /api/bitacoras/actividades/{id}
     */
    @DeleteMapping("/actividades/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarActividad(@PathVariable Long id) {
        bitacoraService.eliminarActividad(id);
        return ResponseEntity.ok(ApiResponse.success("Actividad eliminada", null));
    }

    /**
     * Generar reporte PDF
     * GET /api/bitacoras/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPDF(@PathVariable Long id) {
        byte[] pdf = bitacoraService.generarReportePDF(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bitacora-" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    /**
     * Listar bitácoras por contrato
     * GET /api/bitacoras/contrato/{contratoId}
     */
    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<ApiResponse<List<BitacoraResponse>>> listarPorContrato(
            @PathVariable Long contratoId) {
        
        List<BitacoraResponse> bitacoras = bitacoraService.listarPorContrato(contratoId);
        return ResponseEntity.ok(ApiResponse.success("Bitácoras del contrato", bitacoras));
    }

    /**
     * Listar bitácoras pendientes de un proyecto
     * GET /api/bitacoras/pendientes/proyecto/{proyectoId}
     */
    @GetMapping("/pendientes/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<BitacoraResponse>>> listarPendientes(
            @PathVariable Long proyectoId) {
        
        List<BitacoraResponse> bitacoras = bitacoraService.listarPendientesPorProyecto(proyectoId);
        return ResponseEntity.ok(ApiResponse.success("Bitácoras pendientes", bitacoras));
    }

    /**
     * Obtener bitácora por ID
     * GET /api/bitacoras/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BitacoraResponse>> obtenerPorId(@PathVariable Long id) {
        BitacoraResponse bitacora = bitacoraService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Bitácora encontrada", bitacora));
    }
}