package com.epn.dicc.dto.request;

import com.epn.dicc.model.enums.TipoProyecto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO para crear un proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearProyectoRequest {
    
    @NotBlank(message = "El código del proyecto es obligatorio")
    private String codigoProyectoAutorizado;
    
    @NotBlank(message = "El título es obligatorio")
    private String titulo;
    
    private String descripcion;
    private String objetivoGeneral;
    
    private LocalDate fechaInicioEstimada;
    private LocalDate fechaFinEstimada;
    
    @NotNull(message = "La duración en semestres es obligatoria")
    private Integer duracionSemestres;
    
    @NotNull(message = "El tipo de proyecto es obligatorio")
    private TipoProyecto tipoProyecto;
    
    @NotNull(message = "El laboratorio es obligatorio")
    private Long laboratorioId;
    
    // Map<Semestre, Capacidad>
    // Ejemplo: {1: {ayudantes: 2, meses: 5}, 2: {ayudantes: 3, meses: 6}}
    @NotNull(message = "La capacidad por semestre es obligatoria")
    private Map<Integer, CapacidadSemestreDTO> capacidadPorSemestre;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacidadSemestreDTO {
        @NotNull
        private Integer numeroAyudantes;
        
        @NotNull
        @Min(value = 1, message = "Los meses deben ser al menos 1")
        @Max(value = 6, message = "Los meses no pueden ser más de 6")
        private Integer numeroMeses;
    }
}