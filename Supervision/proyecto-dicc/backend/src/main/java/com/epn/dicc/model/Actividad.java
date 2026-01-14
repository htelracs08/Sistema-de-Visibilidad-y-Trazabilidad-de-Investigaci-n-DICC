package com.epn.dicc.model;

import com.epn.dicc.model.base.EntidadBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad Actividad
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "actividad")
public class Actividad extends EntidadBase {

    @NotNull(message = "El número de actividad es obligatorio")
    @Column(name = "numero_actividad", nullable = false)
    private Integer numeroActividad;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "objetivo_actividad", columnDefinition = "TEXT")
    private String objetivoActividad;

    @Column(name = "resultado_obtenido", columnDefinition = "TEXT")
    private String resultadoObtenido;

    @NotNull(message = "El tiempo dedicado es obligatorio")
    @Positive(message = "El tiempo debe ser positivo")
    @Column(name = "tiempo_dedicado_horas", precision = 5, scale = 2, nullable = false)
    private BigDecimal tiempoDedicadoHoras;

    @NotNull(message = "La fecha de ejecución es obligatoria")
    @Column(name = "fecha_ejecucion", nullable = false)
    private LocalDate fechaEjecucion;

    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Column(name = "categoria", length = 100)
    private String categoria;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitacora_id", nullable = false)
    private BitacoraMensual bitacora;

    // Método de validación
    public boolean validarTiempoDedicado() {
        return tiempoDedicadoHoras != null && 
               tiempoDedicadoHoras.compareTo(BigDecimal.ZERO) > 0 &&
               tiempoDedicadoHoras.compareTo(new BigDecimal("200")) <= 0; // Máximo 200 horas/mes
    }
}