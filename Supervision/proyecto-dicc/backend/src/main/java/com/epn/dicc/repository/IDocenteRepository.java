package com.epn.dicc.repository;

import com.epn.dicc.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para Docente
 */
@Repository
public interface IDocenteRepository extends JpaRepository<Docente, Long> {
    
    List<Docente> findByDepartamento(String departamento);
    
    List<Docente> findByAreaInvestigacion(String areaInvestigacion);
}