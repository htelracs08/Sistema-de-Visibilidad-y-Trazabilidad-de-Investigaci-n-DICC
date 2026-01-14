package com.epn.dicc.service;

import com.epn.dicc.dto.response.NotificacionResponse;
import com.epn.dicc.model.Notificacion;

import java.util.List;

/**
 * Interface del servicio de notificaciones
 */
public interface INotificacionService {
    void notificarNuevoProyecto(Long proyectoId);
    void notificarSolicitudIngreso(Long contratoId);
    void notificarAprobacionContrato(Long contratoId);
    void notificarRenuncia(Long contratoId);
    void notificarFinalizacionProyecto(Long proyectoId);
    void marcarComoLeida(Long notificacionId);
    List<NotificacionResponse> listarPorUsuario(Long usuarioId);
    List<NotificacionResponse> listarNoLeidas(Long usuarioId);
    Integer contarNoLeidas(Long usuarioId);
}