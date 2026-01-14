package com.epn.dicc.controller;

import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.model.Articulo;
import com.epn.dicc.service.IArticuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para Artículos
 */
@RestController
@RequestMapping("/api/articulos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArticuloController {

    private final IArticuloService articuloService;

    /**
     * Registrar nuevo artículo
     * POST /api/articulos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Articulo>> registrar(
            @RequestBody Articulo articulo,
            @RequestParam Long proyectoId) {
        
        Articulo nuevoArticulo = articuloService.registrarArticulo(articulo, proyectoId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Artículo registrado", nuevoArticulo));
    }

    /**
     * Listar artículos por proyecto
     * GET /api/articulos/proyecto/{proyectoId}
     */
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<ApiResponse<List<Articulo>>> listarPorProyecto(
            @PathVariable Long proyectoId) {
        
        List<Articulo> articulos = articuloService.listarPorProyecto(proyectoId);
        return ResponseEntity.ok(ApiResponse.success("Artículos del proyecto", articulos));
    }
}