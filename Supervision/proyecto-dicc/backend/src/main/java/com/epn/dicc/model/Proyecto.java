package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import com.epn.dicc.model.enums.EstadoProyecto;
import com.epn.dicc.model.enums.TipoProyecto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad Proyecto
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "proyecto")
public class Proyecto extends EntidadBase {

    @NotBlank(message = "El código del proyecto es obligatorio")
    @Column(name = "codigo_proyecto", unique = true, nullable = false, length = 50)
    private String codigoProyecto;

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 300)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "objetivo_general", columnDefinition = "TEXT")
    private String objetivoGeneral;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @NotNull(message = "La duración en semestres es obligatoria")
    @Column(name = "duracion_semestres", nullable = false)
    private Integer duracionSemestres;

    @Column(name = "semestre_actual")
    private Integer semestreActual = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoProyecto estado = EstadoProyecto.AUTORIZADO_PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_proyecto", nullable = false, length = 50)
    private TipoProyecto tipoProyecto;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private Docente director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    // Métodos de negocio
    public void activar() {
        this.estado = EstadoProyecto.ACTIVO;
        if (this.fechaInicioReal == null) {
            this.fechaInicioReal = LocalDate.now();
        }
    }

    public void finalizar(LocalDate fechaFin) {
        this.estado = EstadoProyecto.FINALIZADO;
        this.fechaFinReal = fechaFin != null ? fechaFin : LocalDate.now();
    }

    public void suspender(String motivo) {
        this.estado = EstadoProyecto.SUSPENDIDO;
        // Aquí podrías registrar el motivo en auditoría
    }

    public void avanzarSemestre() {
        if (this.semestreActual < this.duracionSemestres) {
            this.semestreActual++;
        }
    }

    public Integer calcularMesesRestantes() {
        if (fechaFinEstimada == null) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(fechaFinEstimada)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(hoy, fechaFinEstimada);
    }

    public boolean estaActivo() {
        return this.estado == EstadoProyecto.ACTIVO;
    }

    public boolean estaFinalizado() {
        return this.estado == EstadoProyecto.FINALIZADO;
    }
}