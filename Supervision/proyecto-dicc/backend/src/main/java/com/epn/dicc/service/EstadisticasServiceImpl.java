package com.epn.dicc.service;

import com.epn.dicc.dto.response.KPIGlobalesResponse;
import com.epn.dicc.dto.response.ReporteAyudantesDTO;
import com.epn.dicc.dto.response.ReporteProyectosDTO;
import com.epn.dicc.model.enums.EstadoContrato;
import com.epn.dicc.model.enums.EstadoProyecto;
import com.epn.dicc.model.enums.Rol;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de estadísticas
 */
@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements IEstadisticasService {

    private final IProyectoRepository proyectoRepository;
    private final IContratoRepository contratoRepository;
    private final IArticuloRepository articuloRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ILaboratorioRepository laboratorioRepository;

    @Override
    @Transactional(readOnly = true)
    public KPIGlobalesResponse obtenerKPIGlobales() {
        KPIGlobalesResponse kpi = new KPIGlobalesResponse();

        // Proyectos
        kpi.setTotalProyectosActivos(
                proyectoRepository.contarPorEstado(EstadoProyecto.ACTIVO));
        kpi.setTotalProyectosFinalizados(
                proyectoRepository.contarPorEstado(EstadoProyecto.FINALIZADO));
        kpi.setTotalProyectosSuspendidos(
                proyectoRepository.contarPorEstado(EstadoProyecto.SUSPENDIDO));

        // Ayudantes activos
        int ayudantesActivos = contratoRepository.findByEstado(EstadoContrato.ACTIVO).size();
        kpi.setTotalAyudantesActivos(ayudantesActivos);

        // Total de ayudantes históricos
        kpi.setTotalAyudantesHistoricos(
                usuarioRepository.findByRol(Rol.AYUDANTE_PROYECTO).size());

        // Directores
        kpi.setTotalDirectores(
                usuarioRepository.findByRol(Rol.DIRECTOR_PROYECTO).size());

        // Artículos
        kpi.setTotalArticulosPublicados(
                (int) articuloRepository.count());

        // Laboratorios
        kpi.setTotalLaboratorios(
                (int) laboratorioRepository.count());

        return kpi;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteAyudantesDTO generarReporteAyudantes() {
        // Implementación simplificada
        return new ReporteAyudantesDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteProyectosDTO generarReporteProyectos() {
        // Implementación simplificada
        return new ReporteProyectosDTO();
    }
}
