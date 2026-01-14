package com.epn.dicc.service;

import com.epn.dicc.dto.request.AgregarActividadRequest;
import com.epn.dicc.dto.request.CrearBitacoraRequest;
import com.epn.dicc.dto.request.RevisionBitacoraRequest;
import com.epn.dicc.dto.response.BitacoraResponse;

import java.util.List;

/**
 * Interface del servicio de bitácoras
 */
public interface IBitacoraService {
    BitacoraResponse crearBitacora(CrearBitacoraRequest request);
    void agregarActividad(AgregarActividadRequest request);
    void enviarRevision(Long bitacoraId);
    void aprobarBitacora(Long bitacoraId, String comentarios, Long docenteId);
    void rechazarBitacora(Long bitacoraId, String comentarios, Long docenteId);
    void solicitarModificacion(Long bitacoraId, String comentarios, Long docenteId);
    void eliminarActividad(Long actividadId);
    byte[] generarReportePDF(Long bitacoraId);
    List<BitacoraResponse> listarPorContrato(Long contratoId);
    List<BitacoraResponse> listarPendientesPorProyecto(Long proyectoId);
    BitacoraResponse obtenerPorId(Long bitacoraId);
}