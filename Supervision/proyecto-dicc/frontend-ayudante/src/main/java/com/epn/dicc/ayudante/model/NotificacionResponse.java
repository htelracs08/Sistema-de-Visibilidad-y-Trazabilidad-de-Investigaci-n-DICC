package com.epn.dicc.ayudante.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response de notificación
 */
@Data
public class NotificacionResponse {
    private Long id;
    private String titulo;
    private String mensaje;
    private String tipo;
    private LocalDateTime fechaEnvio;
    private Boolean leida;
    private LocalDateTime fechaLectura;
    private String urlReferencia;
    private Boolean esUrgente;
}