package com.epn.dicc.service;

import com.epn.dicc.dto.request.CrearProyectoRequest;
import com.epn.dicc.dto.response.EstadisticasProyectoResponse;
import com.epn.dicc.dto.response.ProyectoResponse;
import com.epn.dicc.model.Proyecto;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface del servicio de proyectos
 */
public interface IProyectoService {
    ProyectoResponse crearProyecto(CrearProyectoRequest request, Long directorId);
    ProyectoResponse actualizarProyecto(Long id, Proyecto proyecto);
    void finalizarProyecto(Long id, LocalDate fechaFin);
    void suspenderProyecto(Long id, String motivo);
    void avanzarSemestreProyecto(Long id);
    List<ProyectoResponse> listarProyectosActivos();
    List<ProyectoResponse> listarProyectosPorDirector(Long docenteId);
    ProyectoResponse obtenerProyectoPorId(Long id);
    ProyectoResponse obtenerProyectoPorCodigo(String codigo);
    EstadisticasProyectoResponse obtenerEstadisticas(Long id);
}