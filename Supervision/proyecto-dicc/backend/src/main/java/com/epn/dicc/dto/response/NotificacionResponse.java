package com.epn.dicc.dto.response;

import com.epn.dicc.model.enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Notificación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private Long id;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private LocalDateTime fechaEnvio;
    private Boolean leida;
    private LocalDateTime fechaLectura;
    private String urlReferencia;
    private Boolean esUrgente;
}