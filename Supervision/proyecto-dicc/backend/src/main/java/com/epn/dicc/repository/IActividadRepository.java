package com.epn.dicc.repository;

import com.epn.dicc.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para Actividad
 */
@Repository
public interface IActividadRepository extends JpaRepository<Actividad, Long> {
    
    List<Actividad> findByBitacoraId(Long bitacoraId);
    
    List<Actividad> findByBitacoraIdOrderByNumeroActividadAsc(Long bitacoraId);
}