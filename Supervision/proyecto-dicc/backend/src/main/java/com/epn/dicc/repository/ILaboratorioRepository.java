package com.epn.dicc.repository;

import com.epn.dicc.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para Laboratorio
 */
@Repository
public interface ILaboratorioRepository extends JpaRepository<Laboratorio, Long> {
    
    Optional<Laboratorio> findByCodigoLaboratorio(String codigoLaboratorio);
    
    boolean existsByCodigoLaboratorio(String codigoLaboratorio);
}