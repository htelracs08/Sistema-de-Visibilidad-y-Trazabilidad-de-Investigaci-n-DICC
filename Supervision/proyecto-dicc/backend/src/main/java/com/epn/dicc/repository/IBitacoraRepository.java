package com.epn.dicc.repository;

import com.epn.dicc.model.BitacoraMensual;
import com.epn.dicc.model.enums.EstadoBitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para BitacoraMensual
 */
@Repository
public interface IBitacoraRepository extends JpaRepository<BitacoraMensual, Long> {
    
    List<BitacoraMensual> findByContratoId(Long contratoId);
    
    Optional<BitacoraMensual> findByContratoIdAndMesAndAnio(Long contratoId, Integer mes, Integer anio);
    
    @Query("SELECT b FROM BitacoraMensual b WHERE b.contrato.proyecto.id = :proyectoId AND b.estado = 'ENVIADA_REVISION'")
    List<BitacoraMensual> findPendientesRevisionPorProyecto(@Param("proyectoId") Long proyectoId);
    
    @Query("SELECT b FROM BitacoraMensual b WHERE b.contrato.proyecto.id = :proyectoId AND b.estado = 'APROBADA'")
    List<BitacoraMensual> findAprobadasPorProyecto(@Param("proyectoId") Long proyectoId);
    
    @Query("SELECT COUNT(b) FROM BitacoraMensual b WHERE b.contrato.id = :contratoId AND b.estado = :estado")
    Integer contarPorEstado(@Param("contratoId") Long contratoId, @Param("estado") EstadoBitacora estado);
}