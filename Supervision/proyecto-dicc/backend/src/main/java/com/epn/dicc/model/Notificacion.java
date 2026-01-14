package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import com.epn.dicc.model.enums.TipoNotificacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Entidad Notificacion
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notificacion")
public class Notificacion extends EntidadBase {

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoNotificacion tipo;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "leida")
    private Boolean leida = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "url_referencia", length = 300)
    private String urlReferencia;

    @Column(name = "datos_adicionales", columnDefinition = "TEXT")
    private String datosAdicionales;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    // Métodos
    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }

    public boolean esUrgente() {
        return tipo == TipoNotificacion.SOLICITUD_INGRESO_AYUDANTE ||
               tipo == TipoNotificacion.RENUNCIA_AYUDANTE;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }
}
