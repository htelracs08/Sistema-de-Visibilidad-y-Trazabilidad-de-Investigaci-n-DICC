package com.epn.dicc.repository;

import com.epn.dicc.model.ProyectoCapacidadSemestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para ProyectoCapacidadSemestre
 */
@Repository
public interface IProyectoCapacidadRepository extends JpaRepository<ProyectoCapacidadSemestre, Long> {
    
    List<ProyectoCapacidadSemestre> findByProyectoId(Long proyectoId);
    
    ProyectoCapacidadSemestre findByProyectoIdAndNumeroSemestre(Long proyectoId, Integer numeroSemestre);
}
