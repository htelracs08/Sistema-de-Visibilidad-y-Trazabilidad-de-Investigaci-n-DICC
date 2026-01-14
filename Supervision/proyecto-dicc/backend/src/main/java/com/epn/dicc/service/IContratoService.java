package com.epn.dicc.service;

import com.epn.dicc.dto.request.AprobacionContratoRequest;
import com.epn.dicc.dto.request.SolicitudIngresoRequest;
import com.epn.dicc.dto.response.ContratoResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface del servicio de contratos
 */
public interface IContratoService {
    ContratoResponse solicitarIngreso(Long ayudanteId, SolicitudIngresoRequest request);
    ContratoResponse aprobarContrato(AprobacionContratoRequest request, Long directorId);
    void rechazarContrato(Long contratoId, String motivo, Long directorId);
    void registrarRenuncia(Long contratoId, LocalDate fecha, String motivo);
    List<ContratoResponse> listarSolicitudesPendientes(Long proyectoId);
    List<ContratoResponse> listarContratosPorAyudante(Long ayudanteId);
    List<ContratoResponse> listarContratosPorProyecto(Long proyectoId);
    ContratoResponse obtenerContratoPorId(Long id);
}