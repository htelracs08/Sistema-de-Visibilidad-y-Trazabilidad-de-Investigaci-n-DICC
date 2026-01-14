package com.epn.dicc.repository;

import com.epn.dicc.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Articulo
 */
@Repository
public interface IArticuloRepository extends JpaRepository<Articulo, Long> {
    
    Optional<Articulo> findByDoi(String doi);
    
    List<Articulo> findByProyectoId(Long proyectoId);
    
    @Query("SELECT a FROM Articulo a JOIN a.proyecto p WHERE YEAR(a.fechaPublicacion) = :anio")
    List<Articulo> findByAnio(@Param("anio") Integer anio);
    
    @Query("SELECT COUNT(a) FROM Articulo a WHERE a.proyecto.id = :proyectoId")
    Integer contarPorProyecto(@Param("proyectoId") Long proyectoId);
}