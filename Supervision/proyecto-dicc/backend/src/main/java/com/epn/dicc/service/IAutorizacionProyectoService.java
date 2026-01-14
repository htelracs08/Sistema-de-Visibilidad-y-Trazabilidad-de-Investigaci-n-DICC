package com.epn.dicc.service;

import com.epn.dicc.model.ProyectoAutorizado;

import java.io.File;
import java.util.List;

/**
 * Interface del servicio de autorización de proyectos
 */
public interface IAutorizacionProyectoService {
    Integer cargarProyectosAutorizadosDesdeCSV(File archivo);
    ProyectoAutorizado autorizarProyecto(String codigoProyecto, Long jefaturaId);
    List<ProyectoAutorizado> listarProyectosAutorizadosDisponibles();
    void marcarProyectoComoUtilizado(String codigoProyecto);
    void eliminarAutorizacion(String codigoProyecto);
}