package com.epn.dicc.repository;

import com.epn.dicc.model.ProyectoAutorizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ProyectoAutorizado
 */
@Repository
public interface IProyectoAutorizadoRepository extends JpaRepository<ProyectoAutorizado, Long> {
    
    Optional<ProyectoAutorizado> findByCodigoProyecto(String codigoProyecto);
    
    List<ProyectoAutorizado> findByUtilizado(Boolean utilizado);
    
    boolean existsByCodigoProyecto(String codigoProyecto);
    
    @Query("SELECT pa FROM ProyectoAutorizado pa WHERE pa.utilizado = false")
    List<ProyectoAutorizado> findDisponibles();
}