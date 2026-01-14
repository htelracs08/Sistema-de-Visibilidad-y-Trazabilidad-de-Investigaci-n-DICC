package com.epn.dicc.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad EventoAuditoria
 */
@Data
@Entity
@Table(name = "evento_auditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada")
    private Long idEntidadAfectada;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion; // CREATE, UPDATE, DELETE, LOGIN, etc.

    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}