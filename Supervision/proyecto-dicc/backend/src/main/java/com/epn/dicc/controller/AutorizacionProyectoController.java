package com.epn.dicc.controller;

import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.model.ProyectoAutorizado;
import com.epn.dicc.service.IAutorizacionProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * Controlador REST para Autorización de Proyectos
 */
@RestController
@RequestMapping("/api/autorizacion-proyectos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutorizacionProyectoController {

    private final IAutorizacionProyectoService autorizacionService;

    /**
     * Cargar proyectos desde CSV
     * POST /api/autorizacion-proyectos/cargar-csv
     */
    @PostMapping("/cargar-csv")
    public ResponseEntity<ApiResponse<Integer>> cargarCSV(
            @RequestParam("archivo") MultipartFile archivo) {
        
        try {
            // Guardar temporalmente el archivo
            File tempFile = File.createTempFile("proyectos", ".csv");
            archivo.transferTo(tempFile);
            
            Integer cantidad = autorizacionService.cargarProyectosAutorizadosDesdeCSV(tempFile);
            
            // Eliminar archivo temporal
            tempFile.delete();
            
            return ResponseEntity.ok(
                    ApiResponse.success(cantidad + " proyectos autorizados", cantidad));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al procesar CSV: " + e.getMessage()));
        }
    }

    /**
     * Autorizar proyecto manualmente
     * POST /api/autorizacion-proyectos/autorizar
     */
    @PostMapping("/autorizar")
    public ResponseEntity<ApiResponse<ProyectoAutorizado>> autorizar(
            @RequestParam String codigoProyecto,
            @RequestParam Long jefaturaId) {
        
        ProyectoAutorizado autorizado = autorizacionService.autorizarProyecto(
                codigoProyecto, jefaturaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Proyecto autorizado", autorizado));
    }

    /**
     * Listar proyectos disponibles
     * GET /api/autorizacion-proyectos/disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<ProyectoAutorizado>>> listarDisponibles() {
        List<ProyectoAutorizado> disponibles = 
                autorizacionService.listarProyectosAutorizadosDisponibles();
        return ResponseEntity.ok(ApiResponse.success("Proyectos disponibles", disponibles));
    }

    /**
     * Eliminar autorización
     * DELETE /api/autorizacion-proyectos/{codigoProyecto}
     */
    @DeleteMapping("/{codigoProyecto}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String codigoProyecto) {
        autorizacionService.eliminarAutorizacion(codigoProyecto);
        return ResponseEntity.ok(ApiResponse.success("Autorización eliminada", null));
    }
}