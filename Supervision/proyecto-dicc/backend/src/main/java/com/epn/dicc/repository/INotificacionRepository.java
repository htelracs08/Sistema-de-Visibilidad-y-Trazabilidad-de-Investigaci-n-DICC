package com.epn.dicc.repository;

import com.epn.dicc.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para Notificacion
 */
@Repository
public interface INotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByDestinatarioId(Long usuarioId);
    
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario.id = :usuarioId AND n.leida = false ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findNoLeidasPorUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.destinatario.id = :usuarioId AND n.leida = false")
    Integer contarNoLeidasPorUsuario(@Param("usuarioId") Long usuarioId);
    
    @Query("DELETE FROM Notificacion n WHERE n.destinatario.id = :usuarioId AND n.fechaEnvio < :fechaLimite")
    void eliminarAntiguasPorUsuario(@Param("usuarioId") Long usuarioId, @Param("fechaLimite") LocalDateTime fechaLimite);
}