package com.epn.dicc.service;

import com.epn.dicc.dto.response.KPIGlobalesResponse;
import com.epn.dicc.dto.response.ReporteAyudantesDTO;
import com.epn.dicc.dto.response.ReporteProyectosDTO;

/**
 * Interface del servicio de estadísticas
 */
public interface IEstadisticasService {
    KPIGlobalesResponse obtenerKPIGlobales();
    ReporteAyudantesDTO generarReporteAyudantes();
    ReporteProyectosDTO generarReporteProyectos();
}