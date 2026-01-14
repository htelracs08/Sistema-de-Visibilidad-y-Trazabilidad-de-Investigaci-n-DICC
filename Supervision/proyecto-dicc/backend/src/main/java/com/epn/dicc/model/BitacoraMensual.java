package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import com.epn.dicc.model.enums.EstadoBitacora;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad BitacoraMensual
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bitacora_mensual",
       uniqueConstraints = @UniqueConstraint(columnNames = {"contrato_id", "mes", "anio"}))
public class BitacoraMensual extends EntidadBase {

    @NotBlank(message = "El código de bitácora es obligatorio")
    @Column(name = "codigo_bitacora", unique = true, nullable = false, length = 50)
    private String codigoBitacora;

    @NotNull(message = "El mes es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    @Column(name = "mes", nullable = false)
    private Integer mes;

    @NotNull(message = "El año es obligatorio")
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoBitacora estado = EstadoBitacora.BORRADOR;

    @Column(name = "horas_totales", precision = 6, scale = 2)
    private BigDecimal horasTotales = BigDecimal.ZERO;

    @Column(name = "comentarios_ayudante", columnDefinition = "TEXT")
    private String comentariosAyudante;

    @Column(name = "comentarios_director", columnDefinition = "TEXT")
    private String comentariosDirector;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisada_por_id")
    private Docente revisadaPor;

    // Métodos de negocio
    public void enviarRevision() {
        this.estado = EstadoBitacora.ENVIADA_REVISION;
        this.fechaEnvio = LocalDateTime.now();
    }

    public void aprobar(String comentarios, Docente docente) {
        this.estado = EstadoBitacora.APROBADA;
        this.comentariosDirector = comentarios;
        this.revisadaPor = docente;
        this.fechaRevision = LocalDateTime.now();
    }

    public void rechazar(String comentarios, Docente docente) {
        this.estado = EstadoBitacora.RECHAZADA;
        this.comentariosDirector = comentarios;
        this.revisadaPor = docente;
        this.fechaRevision = LocalDateTime.now();
    }

    public void solicitarModificacion(String comentarios) {
        this.estado = EstadoBitacora.REQUIERE_MODIFICACION;
        this.comentariosDirector = comentarios;
        this.fechaRevision = LocalDateTime.now();
    }

    public boolean puedeEditar() {
        return estado.puedeEditar();
    }

    public boolean estaEnPlazo() {
        // Verificar si está dentro de los primeros 5 días del mes siguiente
        LocalDateTime limiteEnvio = LocalDateTime.of(anio, mes, 1, 0, 0)
                                                  .plusMonths(1)
                                                  .plusDays(5);
        return LocalDateTime.now().isBefore(limiteEnvio);
    }
}