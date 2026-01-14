package com.epn.dicc.service;

import com.epn.dicc.dto.request.CrearProyectoRequest;
import com.epn.dicc.dto.response.EstadisticasProyectoResponse;
import com.epn.dicc.dto.response.ProyectoResponse;
import com.epn.dicc.exception.BusinessException;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.EstadoProyecto;
import com.epn.dicc.model.enums.TipoNotificacion;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de proyectos
 */
@Service
@RequiredArgsConstructor
public class ProyectoServiceImpl implements IProyectoService {

    private final IProyectoRepository proyectoRepository;
    private final IProyectoAutorizadoRepository proyectoAutorizadoRepository;
    private final IDocenteRepository docenteRepository;
    private final ILaboratorioRepository laboratorioRepository;
    private final IContratoRepository contratoRepository;
    private final IBitacoraRepository bitacoraRepository;
    private final IArticuloRepository articuloRepository;
    private final INotificacionService notificacionService;

    @Override
    @Transactional
    public ProyectoResponse crearProyecto(CrearProyectoRequest request, Long directorId) {
        // Verificar que el código esté autorizado
        ProyectoAutorizado autorizado = proyectoAutorizadoRepository
                .findByCodigoProyecto(request.getCodigoProyectoAutorizado())
                .orElseThrow(() -> new BusinessException("Código de proyecto no autorizado"));

        if (autorizado.getUtilizado()) {
            throw new BusinessException("El código de proyecto ya fue utilizado");
        }

        // Verificar que el código no exista ya
        if (proyectoRepository.existsByCodigoProyecto(request.getCodigoProyectoAutorizado())) {
            throw new BusinessException("Ya existe un proyecto con ese código");
        }

        // Obtener director
        Docente director = docenteRepository.findById(directorId)
                .orElseThrow(() -> new ResourceNotFoundException("Director no encontrado"));

        // Obtener laboratorio
        Laboratorio laboratorio = laboratorioRepository.findById(request.getLaboratorioId())
                .orElseThrow(() -> new ResourceNotFoundException("Laboratorio no encontrado"));

        // Crear proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setCodigoProyecto(request.getCodigoProyectoAutorizado());
        proyecto.setTitulo(request.getTitulo());
        proyecto.setDescripcion(request.getDescripcion());
        proyecto.setObjetivoGeneral(request.getObjetivoGeneral());
        proyecto.setFechaInicioReal(request.getFechaInicioEstimada());
        proyecto.setFechaFinEstimada(request.getFechaFinEstimada());
        proyecto.setDuracionSemestres(request.getDuracionSemestres());
        proyecto.setTipoProyecto(request.getTipoProyecto());
        proyecto.setDirector(director);
        proyecto.setLaboratorio(laboratorio);
        proyecto.setEstado(EstadoProyecto.ACTIVO);

        proyecto = proyectoRepository.save(proyecto);

        // Guardar capacidad por semestre
        for (var entry : request.getCapacidadPorSemestre().entrySet()) {
            ProyectoCapacidadSemestre capacidad = new ProyectoCapacidadSemestre();
            capacidad.setProyecto(proyecto);
            capacidad.setNumeroSemestre(entry.getKey());
            capacidad.setNumeroAyudantes(entry.getValue().getNumeroAyudantes());
            capacidad.setNumeroMesesPorAyudante(entry.getValue().getNumeroMeses());
            // Guardar capacidad (necesitarías un repository)
        }

        // Marcar código como utilizado
        autorizado.marcarComoUtilizado();
        proyectoAutorizadoRepository.save(autorizado);

        // Notificar a Jefatura
        notificacionService.notificarNuevoProyecto(proyecto.getId());

        return mapearAProyectoResponse(proyecto);
    }

    @Override
    @Transactional
    public void finalizarProyecto(Long id, LocalDate fechaFin) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        proyecto.finalizar(fechaFin);
        proyectoRepository.save(proyecto);

        // Notificar a Jefatura
        notificacionService.notificarFinalizacionProyecto(id);
    }

    @Override
    @Transactional
    public void suspenderProyecto(Long id, String motivo) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        proyecto.suspender(motivo);
        proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional
    public void avanzarSemestreProyecto(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        proyecto.avanzarSemestre();
        proyectoRepository.save(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectosActivos() {
        return proyectoRepository.findByEstado(EstadoProyecto.ACTIVO)
                .stream()
                .map(this::mapearAProyectoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectosPorDirector(Long docenteId) {
        return proyectoRepository.findByDirectorId(docenteId)
                .stream()
                .map(this::mapearAProyectoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoResponse obtenerProyectoPorId(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        return mapearAProyectoResponse(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoResponse obtenerProyectoPorCodigo(String codigo) {
        Proyecto proyecto = proyectoRepository.findByCodigoProyecto(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        return mapearAProyectoResponse(proyecto);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasProyectoResponse obtenerEstadisticas(Long id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        EstadisticasProyectoResponse stats = new EstadisticasProyectoResponse();
        stats.setProyectoId(proyecto.getId());
        stats.setCodigoProyecto(proyecto.getCodigoProyecto());
        stats.setTitulo(proyecto.getTitulo());

        // Calcular estadísticas
        Integer ayudantesActivos = contratoRepository.contarAyudantesActivosPorProyecto(id);
        stats.setTotalAyudantesActuales(ayudantesActivos);

        Integer articulosPublicados = articuloRepository.contarPorProyecto(id);
        stats.setTotalArticulosPublicados(articulosPublicados);

        stats.setMesesRestantes(proyecto.calcularMesesRestantes());

        return stats;
    }

    @Override
    @Transactional
    public ProyectoResponse actualizarProyecto(Long id, Proyecto proyectoActualizado) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // Actualizar campos permitidos
        proyecto.setTitulo(proyectoActualizado.getTitulo());
        proyecto.setDescripcion(proyectoActualizado.getDescripcion());
        proyecto.setObjetivoGeneral(proyectoActualizado.getObjetivoGeneral());
        proyecto.setFechaFinEstimada(proyectoActualizado.getFechaFinEstimada());

        proyecto = proyectoRepository.save(proyecto);
        return mapearAProyectoResponse(proyecto);
    }

    private ProyectoResponse mapearAProyectoResponse(Proyecto proyecto) {
        ProyectoResponse response = new ProyectoResponse();
        response.setId(proyecto.getId());
        response.setCodigoProyecto(proyecto.getCodigoProyecto());
        response.setTitulo(proyecto.getTitulo());
        response.setDescripcion(proyecto.getDescripcion());
        response.setFechaInicioReal(proyecto.getFechaInicioReal());
        response.setFechaFinEstimada(proyecto.getFechaFinEstimada());
        response.setDuracionSemestres(proyecto.getDuracionSemestres());
        response.setSemestreActual(proyecto.getSemestreActual());
        response.setEstado(proyecto.getEstado());
        response.setTipoProyecto(proyecto.getTipoProyecto());

        if (proyecto.getDirector() != null) {
            response.setDirectorId(proyecto.getDirector().getId());
            response.setNombreDirector(proyecto.getDirector().getNombreCompleto());
        }

        if (proyecto.getLaboratorio() != null) {
            response.setLaboratorioId(proyecto.getLaboratorio().getId());
            response.setNombreLaboratorio(proyecto.getLaboratorio().getNombre());
        }

        response.setTotalAyudantesActivos(
                contratoRepository.contarAyudantesActivosPorProyecto(proyecto.getId()));
        response.setMesesRestantes(proyecto.calcularMesesRestantes());

        return response;
    }
}