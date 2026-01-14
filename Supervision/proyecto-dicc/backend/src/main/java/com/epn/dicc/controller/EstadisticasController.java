package com.epn.dicc.controller;

import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.KPIGlobalesResponse;
import com.epn.dicc.service.IEstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para Estadísticas
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstadisticasController {

    private final IEstadisticasService estadisticasService;

    /**
     * Obtener KPIs globales
     * GET /api/estadisticas/kpi
     */
    @GetMapping("/kpi")
    public ResponseEntity<ApiResponse<KPIGlobalesResponse>> obtenerKPIGlobales() {
        KPIGlobalesResponse kpi = estadisticasService.obtenerKPIGlobales();
        return ResponseEntity.ok(ApiResponse.success("KPIs globales", kpi));
    }
}
