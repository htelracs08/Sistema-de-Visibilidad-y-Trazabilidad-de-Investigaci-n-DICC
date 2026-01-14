package com.epn.dicc.repository;

import com.epn.dicc.model.Contrato;
import com.epn.dicc.model.enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Contrato
 */
@Repository
public interface IContratoRepository extends JpaRepository<Contrato, Long> {
    
    Optional<Contrato> findByNumeroContrato(String numeroContrato);
    
    List<Contrato> findByProyectoId(Long proyectoId);
    
    List<Contrato> findByAyudanteId(Long ayudanteId);
    
    List<Contrato> findByEstado(EstadoContrato estado);
    
    @Query("SELECT c FROM Contrato c WHERE c.proyecto.id = :proyectoId AND c.estado = 'ACTIVO'")
    List<Contrato> findActivosPorProyecto(@Param("proyectoId") Long proyectoId);
    
    @Query("SELECT c FROM Contrato c WHERE c.proyecto.id = :proyectoId AND c.estado = 'PENDIENTE_APROBACION_DIRECTOR'")
    List<Contrato> findPendientesPorProyecto(@Param("proyectoId") Long proyectoId);
    
    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.proyecto.id = :proyectoId AND c.estado = 'ACTIVO'")
    Integer contarAyudantesActivosPorProyecto(@Param("proyectoId") Long proyectoId);
    
    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.proyecto.id = :proyectoId AND c.semestreAsignado = :semestre AND c.estado = 'ACTIVO'")
    Integer contarAyudantesActivosPorSemestre(@Param("proyectoId") Long proyectoId, @Param("semestre") Integer semestre);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contrato c " +
           "WHERE c.ayudante.id = :ayudanteId AND c.proyecto.id = :proyectoId AND c.estado = 'ACTIVO'")
    boolean existeContratoActivoAyudanteEnProyecto(@Param("ayudanteId") Long ayudanteId, @Param("proyectoId") Long proyectoId);
}