package com.epn.dicc.controller;

import com.epn.dicc.dto.response.ApiResponse;
import com.epn.dicc.dto.response.NotificacionResponse;
import com.epn.dicc.service.INotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para Notificaciones
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final INotificacionService notificacionService;

    /**
     * Listar notificaciones por usuario
     * GET /api/notificaciones/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarPorUsuario(
            @PathVariable Long usuarioId) {
        List<NotificacionResponse> notificaciones = notificacionService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Notificaciones", notificaciones));
    }

    /**
     * Listar notificaciones no leídas
     * GET /api/notificaciones/no-leidas/{usuarioId}
     */
    @GetMapping("/no-leidas/{usuarioId}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarNoLeidas(
            @PathVariable Long usuarioId) {
        List<NotificacionResponse> notificaciones = notificacionService.listarNoLeidas(usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Notificaciones no leídas", notificaciones));
    }

    /**
     * Contar notificaciones no leídas
     * GET /api/notificaciones/contar-no-leidas/{usuarioId}
     */
    @GetMapping("/contar-no-leidas/{usuarioId}")
    public ResponseEntity<ApiResponse<Integer>> contarNoLeidas(@PathVariable Long usuarioId) {
        Integer cantidad = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Cantidad", cantidad));
    }

    /**
     * Marcar como leída
     * POST /api/notificaciones/{id}/marcar-leida
     */
    @PostMapping("/{id}/marcar-leida")
    public ResponseEntity<ApiResponse<Void>> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(ApiResponse.success("Notificación marcada como leída", null));
    }
}
