package com.epn.dicc.repository;

import com.epn.dicc.model.Ayudante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para Ayudante
 */
@Repository
public interface IAyudanteRepository extends JpaRepository<Ayudante, Long> {
    
    List<Ayudante> findByCarrera(String carrera);
    
    List<Ayudante> findByFacultad(String facultad);
    
    List<Ayudante> findByQuintil(Integer quintil);
}