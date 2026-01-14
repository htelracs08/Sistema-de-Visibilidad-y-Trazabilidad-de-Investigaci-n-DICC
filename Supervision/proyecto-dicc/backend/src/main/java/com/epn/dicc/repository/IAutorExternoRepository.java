package com.epn.dicc.repository;

import com.epn.dicc.model.AutorExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para AutorExterno
 */
@Repository
public interface IAutorExternoRepository extends JpaRepository<AutorExterno, Long> {
    
    Optional<AutorExterno> findByEmail(String email);
}