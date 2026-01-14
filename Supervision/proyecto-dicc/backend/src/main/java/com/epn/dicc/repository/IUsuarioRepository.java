package com.epn.dicc.repository;

import com.epn.dicc.model.Usuario;
import com.epn.dicc.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Usuario
 */
@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByCodigoEPN(String codigoEPN);
    
    Optional<Usuario> findByCorreoInstitucional(String correoInstitucional);
    
    Optional<Usuario> findByCedula(String cedula);
    
    List<Usuario> findByRol(Rol rol);
    
    boolean existsByCorreoInstitucional(String correoInstitucional);
    
    boolean existsByCodigoEPN(String codigoEPN);
    
    boolean existsByCedula(String cedula);
}