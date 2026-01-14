package com.epn.dicc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAyudantesDTO {
    private Integer totalAyudantes;
    private Integer ayudantesActivos;
    private Double horasPromedioMensual;
}