package com.epn.dicc.service;

import com.epn.dicc.dto.request.AgregarActividadRequest;
import com.epn.dicc.dto.request.CrearBitacoraRequest;
import com.epn.dicc.dto.response.ActividadResponse;
import com.epn.dicc.dto.response.BitacoraResponse;
import com.epn.dicc.exception.BusinessException;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.EstadoBitacora;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de bitácoras
 */
@Service
@RequiredArgsConstructor
public class BitacoraServiceImpl implements IBitacoraService {

    private final IBitacoraRepository bitacoraRepository;
    private final IContratoRepository contratoRepository;
    private final IActividadRepository actividadRepository;
    private final IDocenteRepository docenteRepository;
    private final INotificacionService notificacionService;

    @Override
    @Transactional
    public BitacoraResponse crearBitacora(CrearBitacoraRequest request) {
        // Verificar que el contrato exista y esté activo
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (!contrato.esActivo()) {
            throw new BusinessException("El contrato no está activo");
        }

        // Verificar que no exista ya una bitácora para ese mes/año
        if (bitacoraRepository.findByContratoIdAndMesAndAnio(
                request.getContratoId(), request.getMes(), request.getAnio()).isPresent()) {
            throw new BusinessException("Ya existe una bitácora para este mes");
        }

        // Crear bitácora
        BitacoraMensual bitacora = new BitacoraMensual();
        bitacora.setCodigoBitacora(generarCodigoBitacora());
        bitacora.setContrato(contrato);
        bitacora.setMes(request.getMes());
        bitacora.setAnio(request.getAnio());
        bitacora.setEstado(EstadoBitacora.BORRADOR);
        bitacora.setComentariosAyudante(request.getComentariosAyudante());

        bitacora = bitacoraRepository.save(bitacora);

        return mapearABitacoraResponse(bitacora);
    }

    @Override
    @Transactional
    public void agregarActividad(AgregarActividadRequest request) {
        BitacoraMensual bitacora = bitacoraRepository.findById(request.getBitacoraId())
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        if (!bitacora.puedeEditar()) {
            throw new BusinessException("La bitácora no puede ser editada en su estado actual");
        }

        // Crear actividad
        Actividad actividad = new Actividad();
        actividad.setBitacora(bitacora);
        actividad.setNumeroActividad(obtenerSiguienteNumeroActividad(bitacora.getId()));
        actividad.setDescripcion(request.getDescripcion());
        actividad.setObjetivoActividad(request.getObjetivoActividad());
        actividad.setResultadoObtenido(request.getResultadoObtenido());
        actividad.setTiempoDedicadoHoras(request.getTiempoDedicadoHoras());
        actividad.setFechaEjecucion(request.getFechaEjecucion());
        actividad.setEvidenciaUrl(request.getEvidenciaUrl());
        actividad.setCategoria(request.getCategoria());

        actividadRepository.save(actividad);

        // Actualizar horas totales
        actualizarHorasTotales(bitacora.getId());
    }

    @Override
    @Transactional
    public void enviarRevision(Long bitacoraId) {
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        if (bitacora.getEstado() != EstadoBitacora.BORRADOR && 
            bitacora.getEstado() != EstadoBitacora.REQUIERE_MODIFICACION) {
            throw new BusinessException("La bitácora no puede ser enviada en su estado actual");
        }

        // Verificar que tenga actividades
        List<Actividad> actividades = actividadRepository.findByBitacoraId(bitacoraId);
        if (actividades.isEmpty()) {
            throw new BusinessException("Debe agregar al menos una actividad antes de enviar");
        }

        bitacora.enviarRevision();
        bitacoraRepository.save(bitacora);

        // Notificar al director (implementar según sea necesario)
        // notificacionService.notificarBitacoraEnviada(bitacoraId);
    }

    @Override
    @Transactional
    public void aprobarBitacora(Long bitacoraId, String comentarios, Long docenteId) {
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        if (bitacora.getEstado() != EstadoBitacora.ENVIADA_REVISION) {
            throw new BusinessException("La bitácora no está pendiente de revisión");
        }

        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado"));

        // Verificar que el docente sea el director del proyecto
        if (!bitacora.getContrato().getProyecto().getDirector().getId().equals(docenteId)) {
            throw new BusinessException("No eres el director de este proyecto");
        }

        bitacora.aprobar(comentarios, docente);
        bitacoraRepository.save(bitacora);

        // Notificar al ayudante
        // notificacionService.notificarBitacoraAprobada(bitacoraId);
    }

    @Override
    @Transactional
    public void rechazarBitacora(Long bitacoraId, String comentarios, Long docenteId) {
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        if (bitacora.getEstado() != EstadoBitacora.ENVIADA_REVISION) {
            throw new BusinessException("La bitácora no está pendiente de revisión");
        }

        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado"));

        if (!bitacora.getContrato().getProyecto().getDirector().getId().equals(docenteId)) {
            throw new BusinessException("No eres el director de este proyecto");
        }

        bitacora.rechazar(comentarios, docente);
        bitacoraRepository.save(bitacora);

        // Notificar al ayudante
        // notificacionService.notificarBitacoraRechazada(bitacoraId);
    }

    @Override
    @Transactional
    public void solicitarModificacion(Long bitacoraId, String comentarios, Long docenteId) {
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        if (bitacora.getEstado() != EstadoBitacora.ENVIADA_REVISION) {
            throw new BusinessException("La bitácora no está pendiente de revisión");
        }

        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado"));

        bitacora.solicitarModificacion(comentarios);
        bitacora.setRevisadaPor(docente);
        bitacoraRepository.save(bitacora);
    }

    @Override
    @Transactional
    public void eliminarActividad(Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));

        BitacoraMensual bitacora = actividad.getBitacora();
        if (!bitacora.puedeEditar()) {
            throw new BusinessException("No se puede eliminar la actividad en el estado actual de la bitácora");
        }

        actividadRepository.delete(actividad);

        // Actualizar horas totales
        actualizarHorasTotales(bitacora.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePDF(Long bitacoraId) {
        // Implementación simplificada - en producción usar iText o similar
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));

        // TODO: Implementar generación real de PDF con iText
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraResponse> listarPorContrato(Long contratoId) {
        return bitacoraRepository.findByContratoId(contratoId)
                .stream()
                .map(this::mapearABitacoraResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraResponse> listarPendientesPorProyecto(Long proyectoId) {
        return bitacoraRepository.findPendientesRevisionPorProyecto(proyectoId)
                .stream()
                .map(this::mapearABitacoraResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BitacoraResponse obtenerPorId(Long bitacoraId) {
        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora no encontrada"));
        return mapearABitacoraResponse(bitacora);
    }

    // Métodos auxiliares

    private String generarCodigoBitacora() {
        return "BIT-" + LocalDateTime.now().getYear() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Integer obtenerSiguienteNumeroActividad(Long bitacoraId) {
        List<Actividad> actividades = actividadRepository.findByBitacoraId(bitacoraId);
        return actividades.stream()
                .mapToInt(Actividad::getNumeroActividad)
                .max()
                .orElse(0) + 1;
    }

    private void actualizarHorasTotales(Long bitacoraId) {
        List<Actividad> actividades = actividadRepository.findByBitacoraId(bitacoraId);
        BigDecimal total = actividades.stream()
                .map(Actividad::getTiempoDedicadoHoras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BitacoraMensual bitacora = bitacoraRepository.findById(bitacoraId).orElseThrow();
        bitacora.setHorasTotales(total);
        bitacoraRepository.save(bitacora);
    }

    private BitacoraResponse mapearABitacoraResponse(BitacoraMensual bitacora) {
        BitacoraResponse response = new BitacoraResponse();
        response.setId(bitacora.getId());
        response.setCodigoBitacora(bitacora.getCodigoBitacora());
        response.setMes(bitacora.getMes());
        response.setAnio(bitacora.getAnio());
        response.setFechaEnvio(bitacora.getFechaEnvio());
        response.setFechaRevision(bitacora.getFechaRevision());
        response.setEstado(bitacora.getEstado());
        response.setHorasTotales(bitacora.getHorasTotales());
        response.setComentariosAyudante(bitacora.getComentariosAyudante());
        response.setComentariosDirector(bitacora.getComentariosDirector());

        if (bitacora.getContrato() != null) {
            response.setContratoId(bitacora.getContrato().getId());
            response.setNumeroContrato(bitacora.getContrato().getNumeroContrato());
            
            if (bitacora.getContrato().getAyudante() != null) {
                response.setNombreAyudante(bitacora.getContrato().getAyudante().getNombreCompleto());
            }
        }

        // Mapear actividades
        List<Actividad> actividades = actividadRepository.findByBitacoraIdOrderByNumeroActividadAsc(bitacora.getId());
        response.setActividades(actividades.stream()
                .map(this::mapearAActividadResponse)
                .collect(Collectors.toList()));

        response.setPuedeEditar(bitacora.puedeEditar());
        response.setEstaEnPlazo(bitacora.estaEnPlazo());

        return response;
    }

    private ActividadResponse mapearAActividadResponse(Actividad actividad) {
        ActividadResponse response = new ActividadResponse();
        response.setId(actividad.getId());
        response.setNumeroActividad(actividad.getNumeroActividad());
        response.setDescripcion(actividad.getDescripcion());
        response.setObjetivoActividad(actividad.getObjetivoActividad());
        response.setResultadoObtenido(actividad.getResultadoObtenido());
        response.setTiempoDedicadoHoras(actividad.getTiempoDedicadoHoras());
        response.setFechaEjecucion(actividad.getFechaEjecucion());
        response.setEvidenciaUrl(actividad.getEvidenciaUrl());
        response.setCategoria(actividad.getCategoria());
        return response;
    }
}