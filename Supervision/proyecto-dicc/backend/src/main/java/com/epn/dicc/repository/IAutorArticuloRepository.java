package com.epn.dicc.repository;

import com.epn.dicc.model.AutorArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para AutorArticulo
 */
@Repository
public interface IAutorArticuloRepository extends JpaRepository<AutorArticulo, Long> {
    
    List<AutorArticulo> findByArticuloId(Long articuloId);
    
    List<AutorArticulo> findByUsuarioInternoId(Long usuarioId);
}