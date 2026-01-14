package com.epn.dicc.service;

import com.epn.dicc.exception.BusinessException;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.JefaturaDICC;
import com.epn.dicc.model.ProyectoAutorizado;
import com.epn.dicc.repository.IJefaturaRepository;
import com.epn.dicc.repository.IProyectoAutorizadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de autorización de proyectos
 */
@Service
@RequiredArgsConstructor
public class AutorizacionProyectoServiceImpl implements IAutorizacionProyectoService {

    private final IProyectoAutorizadoRepository proyectoAutorizadoRepository;
    private final IJefaturaRepository jefaturaRepository;

    @Override
    @Transactional
    public Integer cargarProyectosAutorizadosDesdeCSV(File archivo) {
        int contador = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true;

            while ((linea = br.readLine()) != null) {
                // Saltar encabezado
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                String codigoProyecto = linea.trim();

                if (codigoProyecto.isEmpty()) {
                    continue;
                }

                // Verificar si ya existe
                if (proyectoAutorizadoRepository.existsByCodigoProyecto(codigoProyecto)) {
                    continue;
                }

                // Crear autorización
                ProyectoAutorizado autorizado = new ProyectoAutorizado();
                autorizado.setCodigoProyecto(codigoProyecto);
                autorizado.setFechaAutorizacion(LocalDateTime.now());
                autorizado.setUtilizado(false);

                proyectoAutorizadoRepository.save(autorizado);
                contador++;
            }

        } catch (Exception e) {
            throw new BusinessException("Error al leer el archivo CSV: " + e.getMessage());
        }

        return contador;
    }

    @Override
    @Transactional
    public ProyectoAutorizado autorizarProyecto(String codigoProyecto, Long jefaturaId) {
        JefaturaDICC jefatura = jefaturaRepository.findById(jefaturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Jefatura no encontrada"));

        if (proyectoAutorizadoRepository.existsByCodigoProyecto(codigoProyecto)) {
            throw new BusinessException("El código de proyecto ya está autorizado");
        }

        ProyectoAutorizado autorizado = new ProyectoAutorizado();
        autorizado.setCodigoProyecto(codigoProyecto);
        autorizado.setFechaAutorizacion(LocalDateTime.now());
        autorizado.setAutorizadoPor(jefatura.getCorreoInstitucional());
        autorizado.setUtilizado(false);

        return proyectoAutorizadoRepository.save(autorizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoAutorizado> listarProyectosAutorizadosDisponibles() {
        return proyectoAutorizadoRepository.findByUtilizado(false);
    }

    @Override
    @Transactional
    public void marcarProyectoComoUtilizado(String codigoProyecto) {
        ProyectoAutorizado autorizado = proyectoAutorizadoRepository
                .findByCodigoProyecto(codigoProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto autorizado no encontrado"));

        autorizado.marcarComoUtilizado();
        proyectoAutorizadoRepository.save(autorizado);
    }

    @Override
    @Transactional
    public void eliminarAutorizacion(String codigoProyecto) {
        ProyectoAutorizado autorizado = proyectoAutorizadoRepository
                .findByCodigoProyecto(codigoProyecto)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto autorizado no encontrado"));

        if (autorizado.getUtilizado()) {
            throw new BusinessException("No se puede eliminar un código ya utilizado");
        }

        proyectoAutorizadoRepository.delete(autorizado);
    }
}
