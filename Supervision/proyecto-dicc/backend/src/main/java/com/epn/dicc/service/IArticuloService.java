package com.epn.dicc.service;

import com.epn.dicc.dto.response.RelacionArticuloAyudanteResponse;
import com.epn.dicc.model.Articulo;

import java.util.List;

/**
 * Interface del servicio de artículos
 */
public interface IArticuloService {
    Articulo registrarArticulo(Articulo articulo, Long proyectoId);
    void asociarAutorInterno(Long articuloId, Long usuarioId, Integer orden);
    void asociarAutorExterno(Long articuloId, Long autorExternoId, Integer orden);
    void actualizarEstado(Long articuloId, String estado);
    List<Articulo> listarPorProyecto(Long proyectoId);
    List<RelacionArticuloAyudanteResponse> obtenerRelacionArticulosAyudantes(Long proyectoId);
}