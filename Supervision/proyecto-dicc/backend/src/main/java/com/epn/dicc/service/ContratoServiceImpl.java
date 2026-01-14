package com.epn.dicc.service;

import com.epn.dicc.dto.request.AprobacionContratoRequest;
import com.epn.dicc.dto.request.SolicitudIngresoRequest;
import com.epn.dicc.dto.response.ContratoResponse;
import com.epn.dicc.exception.BusinessException;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.EstadoContrato;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de contratos
 */
@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements IContratoService {

    private final IContratoRepository contratoRepository;
    private final IProyectoRepository proyectoRepository;
    private final IAyudanteRepository ayudanteRepository;
    private final IDocenteRepository docenteRepository;
    private final INotificacionService notificacionService;

    @Override
    @Transactional
    public ContratoResponse solicitarIngreso(Long ayudanteId, SolicitudIngresoRequest request) {
        // Buscar ayudante
        Ayudante ayudante = ayudanteRepository.findById(ayudanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Ayudante no encontrado"));

        // Buscar proyecto
        Proyecto proyecto = proyectoRepository.findByCodigoProyecto(request.getCodigoProyecto())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // Validar que no tenga contrato activo en este proyecto
        if (contratoRepository.existeContratoActivoAyudanteEnProyecto(ayudanteId, proyecto.getId())) {
            throw new BusinessException("Ya tienes un contrato activo en este proyecto");
        }

        // Crear contrato
        Contrato contrato = new Contrato();
        contrato.setNumeroContrato(generarNumeroContrato());
        contrato.setProyecto(proyecto);
        contrato.setAyudante(ayudante);
        contrato.setEstado(EstadoContrato.PENDIENTE_APROBACION_DIRECTOR);
        contrato.setFechaSolicitud(LocalDateTime.now());

        contrato = contratoRepository.save(contrato);

        // Notificar al director
        notificacionService.notificarSolicitudIngreso(contrato.getId());

        return mapearAContratoResponse(contrato);
    }

    @Override
    @Transactional
    public ContratoResponse aprobarContrato(AprobacionContratoRequest request, Long directorId) {
        // Buscar contrato
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // Verificar que esté pendiente
        if (contrato.getEstado() != EstadoContrato.PENDIENTE_APROBACION_DIRECTOR) {
            throw new BusinessException("El contrato no está pendiente de aprobación");
        }

        // Buscar director
        Docente director = docenteRepository.findById(directorId)
                .orElseThrow(() -> new ResourceNotFoundException("Director no encontrado"));

        // Verificar que el director sea del proyecto
        if (!contrato.getProyecto().getDirector().getId().equals(directorId)) {
            throw new BusinessException("No eres el director de este proyecto");
        }

        // Verificar capacidad disponible en el semestre
        Integer ayudantesActuales = contratoRepository
                .contarAyudantesActivosPorSemestre(
                        contrato.getProyecto().getId(), 
                        request.getSemestreAsignado()
                );

        // Aquí deberías verificar contra la capacidad definida
        // (necesitarías consultar ProyectoCapacidadSemestre)

        // Aprobar contrato
        contrato.aprobar(
                request.getMesesAsignados(),
                request.getSemestreAsignado(),
                director
        );
        contrato.setHorasSemanalesPactadas(request.getHorasSemanales());
        contrato.setRemuneracionMensual(request.getRemuneracionMensual());

        contrato = contratoRepository.save(contrato);

        // Notificar al ayudante
        notificacionService.notificarAprobacionContrato(contrato.getId());

        return mapearAContratoResponse(contrato);
    }

    @Override
    @Transactional
    public void rechazarContrato(Long contratoId, String motivo, Long directorId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (contrato.getEstado() != EstadoContrato.PENDIENTE_APROBACION_DIRECTOR) {
            throw new BusinessException("El contrato no está pendiente de aprobación");
        }

        Docente director = docenteRepository.findById(directorId)
                .orElseThrow(() -> new ResourceNotFoundException("Director no encontrado"));

        contrato.rechazar(motivo, director);
        contratoRepository.save(contrato);

        // Notificar al ayudante (podrías crear este método)
        // notificacionService.notificarRechazoContrato(contratoId);
    }

    @Override
    @Transactional
    public void registrarRenuncia(Long contratoId, LocalDate fecha, String motivo) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (!contrato.esActivo()) {
            throw new BusinessException("El contrato no está activo");
        }

        contrato.registrarRenuncia(fecha, motivo);
        contratoRepository.save(contrato);

        // Notificar al director y jefatura
        notificacionService.notificarRenuncia(contratoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoResponse> listarSolicitudesPendientes(Long proyectoId) {
        return contratoRepository.findPendientesPorProyecto(proyectoId)
                .stream()
                .map(this::mapearAContratoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoResponse> listarContratosPorAyudante(Long ayudanteId) {
        return contratoRepository.findByAyudanteId(ayudanteId)
                .stream()
                .map(this::mapearAContratoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoResponse> listarContratosPorProyecto(Long proyectoId) {
        return contratoRepository.findByProyectoId(proyectoId)
                .stream()
                .map(this::mapearAContratoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContratoResponse obtenerContratoPorId(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
        return mapearAContratoResponse(contrato);
    }

    private String generarNumeroContrato() {
        return "CT-" + LocalDateTime.now().getYear() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ContratoResponse mapearAContratoResponse(Contrato contrato) {
        ContratoResponse response = new ContratoResponse();
        response.setId(contrato.getId());
        response.setNumeroContrato(contrato.getNumeroContrato());
        response.setFechaSolicitud(contrato.getFechaSolicitud());
        response.setFechaAprobacion(contrato.getFechaAprobacion());
        response.setFechaInicioContrato(contrato.getFechaInicioContrato());
        response.setFechaFinContrato(contrato.getFechaFinContrato());
        response.setMesesPactados(contrato.getMesesPactados());
        response.setMesesTrabajados(contrato.getMesesTrabajados());
        response.setHorasSemanalesPactadas(contrato.getHorasSemanalesPactadas());
        response.setRemuneracionMensual(contrato.getRemuneracionMensual());
        response.setEstado(contrato.getEstado());
        response.setSemestreAsignado(contrato.getSemestreAsignado());

        if (contrato.getProyecto() != null) {
            response.setProyectoId(contrato.getProyecto().getId());
            response.setCodigoProyecto(contrato.getProyecto().getCodigoProyecto());
            response.setTituloProyecto(contrato.getProyecto().getTitulo());

            if (contrato.getProyecto().getDirector() != null) {
                response.setNombreDirector(contrato.getProyecto().getDirector().getNombreCompleto());
            }
        }

        if (contrato.getAyudante() != null) {
            response.setAyudanteId(contrato.getAyudante().getId());
            response.setNombreAyudante(contrato.getAyudante().getNombreCompleto());
            response.setCodigoEPNAyudante(contrato.getAyudante().getCodigoEPN());
        }

        response.setMesesRestantes(contrato.calcularMesesRestantes());
        response.setPuedeReemplazarse(contrato.puedeReemplazarse());

        return response;
    }
}