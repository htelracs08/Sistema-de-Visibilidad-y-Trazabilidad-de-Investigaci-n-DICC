package com.epn.dicc.repository;

import com.epn.dicc.model.Proyecto;
import com.epn.dicc.model.enums.EstadoProyecto;
import com.epn.dicc.model.enums.TipoProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Proyecto
 */
@Repository
public interface IProyectoRepository extends JpaRepository<Proyecto, Long> {
    
    Optional<Proyecto> findByCodigoProyecto(String codigoProyecto);
    
    List<Proyecto> findByEstado(EstadoProyecto estado);
    
    List<Proyecto> findByDirectorId(Long docenteId);
    
    List<Proyecto> findByLaboratorioId(Long laboratorioId);
    
    List<Proyecto> findByTipoProyecto(TipoProyecto tipoProyecto);
    
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.estado = :estado")
    Integer contarPorEstado(@Param("estado") EstadoProyecto estado);
    
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.estado = 'ACTIVO'")
    Integer contarProyectosActivos();
    
    boolean existsByCodigoProyecto(String codigoProyecto);
}