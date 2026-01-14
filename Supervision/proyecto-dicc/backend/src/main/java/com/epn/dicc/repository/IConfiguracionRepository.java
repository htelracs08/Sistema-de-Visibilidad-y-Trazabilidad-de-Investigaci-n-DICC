package com.epn.dicc.repository;

import com.epn.dicc.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ConfiguracionSistema
 */
@Repository
public interface IConfiguracionRepository extends JpaRepository<ConfiguracionSistema, Long> {
    
    Optional<ConfiguracionSistema> findByClaveConfiguracion(String clave);
    
    @Query("SELECT c FROM ConfiguracionSistema c WHERE c.editablePorJefatura = true")
    List<ConfiguracionSistema> findEditablesPorJefatura();
}