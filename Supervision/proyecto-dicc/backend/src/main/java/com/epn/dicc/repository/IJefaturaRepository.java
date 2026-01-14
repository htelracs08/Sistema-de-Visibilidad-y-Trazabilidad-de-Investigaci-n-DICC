package com.epn.dicc.repository;

import com.epn.dicc.model.JefaturaDICC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para JefaturaDICC
 */
@Repository
public interface IJefaturaRepository extends JpaRepository<JefaturaDICC, Long> {
    
    Optional<JefaturaDICC> findByCodigoRegistroEspecial(String codigoRegistroEspecial);
    
    boolean existsByCodigoRegistroEspecial(String codigoRegistroEspecial);
}
