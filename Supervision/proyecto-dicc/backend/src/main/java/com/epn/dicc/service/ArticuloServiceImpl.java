package com.epn.dicc.service;

import com.epn.dicc.dto.response.RelacionArticuloAyudanteResponse;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.*;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de artículos
 */
@Service
@RequiredArgsConstructor
public class ArticuloServiceImpl implements IArticuloService {

    private final IArticuloRepository articuloRepository;
    private final IProyectoRepository proyectoRepository;
    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Articulo registrarArticulo(Articulo articulo, Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        articulo.setProyecto(proyecto);
        return articuloRepository.save(articulo);
    }

    @Override
    @Transactional
    public void asociarAutorInterno(Long articuloId, Long usuarioId, Integer orden) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AutorArticulo autor = new AutorArticulo();
        autor.setArticulo(articulo);
        autor.setUsuarioInterno(usuario);
        autor.setOrdenAutoria(orden);

        // Guardar (necesitarías el repository)
    }

    @Override
    @Transactional
    public void asociarAutorExterno(Long articuloId, Long autorExternoId, Integer orden) {
        // Similar al anterior
    }

    @Override
    @Transactional
    public void actualizarEstado(Long articuloId, String estado) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        articulo.setEstadoPublicacion(estado);
        articuloRepository.save(articulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Articulo> listarPorProyecto(Long proyectoId) {
        return articuloRepository.findByProyectoId(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelacionArticuloAyudanteResponse> obtenerRelacionArticulosAyudantes(Long proyectoId) {
        // Implementación simplificada
        return new ArrayList<>();
    }
}