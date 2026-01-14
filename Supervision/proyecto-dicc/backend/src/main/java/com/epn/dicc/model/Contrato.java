package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import com.epn.dicc.model.enums.EstadoContrato;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad Contrato
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "contrato")
public class Contrato extends EntidadBase {

    @NotBlank(message = "El número de contrato es obligatorio")
    @Column(name = "numero_contrato", unique = true, nullable = false, length = 50)
    private String numeroContrato;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @Column(name = "fecha_inicio_contrato")
    private LocalDate fechaInicioContrato;

    @Column(name = "fecha_fin_contrato")
    private LocalDate fechaFinContrato;

    @Column(name = "fecha_renuncia")
    private LocalDate fechaRenuncia;

    @Column(name = "meses_pactados")
    private Integer mesesPactados;

    @Column(name = "meses_trabajados")
    private Integer mesesTrabajados = 0;

    @Column(name = "horas_semanales_pactadas")
    private Integer horasSemanalesPactadas;

    @Column(name = "remuneracion_mensual", precision = 10, scale = 2)
    private BigDecimal remuneracionMensual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private EstadoContrato estado = EstadoContrato.PENDIENTE_APROBACION_DIRECTOR;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "motivo_renuncia", columnDefinition = "TEXT")
    private String motivoRenuncia;

    @Column(name = "semestre_asignado")
    private Integer semestreAsignado;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ayudante_id", nullable = false)
    private Ayudante ayudante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por_id")
    private Docente aprobadoPor;

    // Métodos de negocio
    public void aprobar(Integer meses, Integer semestre, Docente director) {
        this.estado = EstadoContrato.ACTIVO;
        this.mesesPactados = meses;
        this.semestreAsignado = semestre;
        this.aprobadoPor = director;
        this.fechaAprobacion = LocalDateTime.now();
        this.fechaInicioContrato = LocalDate.now();
        this.fechaFinContrato = this.fechaInicioContrato.plusMonths(meses);
    }

    public void rechazar(String motivo, Docente director) {
        this.estado = EstadoContrato.RECHAZADO;
        this.motivoRechazo = motivo;
        this.aprobadoPor = director;
        this.fechaRechazo = LocalDateTime.now();
    }

    public void registrarRenuncia(LocalDate fecha, String motivo) {
        this.estado = EstadoContrato.FINALIZADO_RENUNCIA;
        this.fechaRenuncia = fecha != null ? fecha : LocalDate.now();
        this.motivoRenuncia = motivo;
        this.mesesTrabajados = calcularMesesEfectivos();
    }

    public void finalizarNormalmente() {
        this.estado = EstadoContrato.FINALIZADO_NORMAL;
        this.mesesTrabajados = this.mesesPactados;
    }

    public Integer calcularMesesRestantes() {
        if (!esActivo() || fechaFinContrato == null) {
            return 0;
        }
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(fechaFinContrato)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(hoy, fechaFinContrato);
    }

    public Integer calcularMesesEfectivos() {
        if (fechaInicioContrato == null) {
            return 0;
        }
        LocalDate fechaFin = fechaRenuncia != null ? fechaRenuncia : 
                            (fechaFinContrato != null ? fechaFinContrato : LocalDate.now());
        return (int) ChronoUnit.MONTHS.between(fechaInicioContrato, fechaFin);
    }

    public boolean esActivo() {
        return this.estado == EstadoContrato.ACTIVO;
    }

    public boolean puedeReemplazarse() {
        return this.estado == EstadoContrato.FINALIZADO_RENUNCIA;
    }

    public Integer getMesesDisponiblesParaReemplazo() {
        if (!puedeReemplazarse()) {
            return 0;
        }
        return this.mesesPactados - this.mesesTrabajados;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }
}