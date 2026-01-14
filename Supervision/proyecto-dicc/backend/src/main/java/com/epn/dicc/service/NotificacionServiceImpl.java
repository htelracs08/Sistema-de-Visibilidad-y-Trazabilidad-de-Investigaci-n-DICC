package com.epn.dicc.service;

import com.epn.dicc.dto.response.NotificacionResponse;
import com.epn.dicc.exception.ResourceNotFoundException;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.Rol;
import com.epn.dicc.model.enums.TipoNotificacion;
import com.epn.dicc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de notificaciones
 */
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements INotificacionService {

    private final INotificacionRepository notificacionRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IProyectoRepository proyectoRepository;
    private final IContratoRepository contratoRepository;

    @Override
    @Transactional
    public void notificarNuevoProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // Notificar a todos los usuarios con rol JEFATURA_DICC
        List<Usuario> jefaturas = usuarioRepository.findByRol(Rol.JEFATURA_DICC);

        for (Usuario jefatura : jefaturas) {
            Notificacion notificacion = new Notificacion();
            notificacion.setTitulo("Nuevo Proyecto Creado");
            notificacion.setMensaje(String.format(
                    "El proyecto '%s' (código: %s) ha sido creado por %s",
                    proyecto.getTitulo(),
                    proyecto.getCodigoProyecto(),
                    proyecto.getDirector().getNombreCompleto()
            ));
            notificacion.setTipo(TipoNotificacion.PROYECTO_CREADO);
            notificacion.setDestinatario(jefatura);
            notificacion.setEmisor(proyecto.getDirector());
            notificacion.setUrlReferencia("/proyectos/" + proyectoId);

            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void notificarSolicitudIngreso(Long contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // Notificar al director del proyecto
        Notificacion notificacion = new Notificacion();
        notificacion.setTitulo("Nueva Solicitud de Ingreso");
        notificacion.setMensaje(String.format(
                "El ayudante %s ha solicitado ingresar al proyecto '%s'",
                contrato.getAyudante().getNombreCompleto(),
                contrato.getProyecto().getTitulo()
        ));
        notificacion.setTipo(TipoNotificacion.SOLICITUD_INGRESO_AYUDANTE);
        notificacion.setDestinatario(contrato.getProyecto().getDirector());
        notificacion.setEmisor(contrato.getAyudante());
        notificacion.setUrlReferencia("/contratos/" + contratoId);

        notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional
    public void notificarAprobacionContrato(Long contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // Notificar al ayudante
        Notificacion notifAyudante = new Notificacion();
        notifAyudante.setTitulo("Contrato Aprobado");
        notifAyudante.setMensaje(String.format(
                "Tu solicitud para el proyecto '%s' ha sido aprobada. " +
                "Duración: %d meses. Inicio: %s",
                contrato.getProyecto().getTitulo(),
                contrato.getMesesPactados(),
                contrato.getFechaInicioContrato()
        ));
        notifAyudante.setTipo(TipoNotificacion.CONTRATO_APROBADO);
        notifAyudante.setDestinatario(contrato.getAyudante());
        notifAyudante.setEmisor(contrato.getProyecto().getDirector());
        notifAyudante.setUrlReferencia("/contratos/" + contratoId);

        notificacionRepository.save(notifAyudante);

        // Notificar a Jefatura
        List<Usuario> jefaturas = usuarioRepository.findByRol(Rol.JEFATURA_DICC);
        for (Usuario jefatura : jefaturas) {
            Notificacion notifJefatura = new Notificacion();
            notifJefatura.setTitulo("Contrato Aprobado");
            notifJefatura.setMensaje(String.format(
                    "Se ha contratado a %s para el proyecto '%s' (%s)",
                    contrato.getAyudante().getNombreCompleto(),
                    contrato.getProyecto().getTitulo(),
                    contrato.getProyecto().getCodigoProyecto()
            ));
            notifJefatura.setTipo(TipoNotificacion.CONTRATO_APROBADO);
            notifJefatura.setDestinatario(jefatura);
            notifJefatura.setEmisor(contrato.getProyecto().getDirector());
            notifJefatura.setUrlReferencia("/contratos/" + contratoId);

            notificacionRepository.save(notifJefatura);
        }
    }

    @Override
    @Transactional
    public void notificarRenuncia(Long contratoId) {
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // Notificar al director
        Notificacion notifDirector = new Notificacion();
        notifDirector.setTitulo("Renuncia de Ayudante");
        notifDirector.setMensaje(String.format(
                "El ayudante %s ha renunciado al proyecto '%s'. " +
                "Meses trabajados: %d de %d",
                contrato.getAyudante().getNombreCompleto(),
                contrato.getProyecto().getTitulo(),
                contrato.getMesesTrabajados(),
                contrato.getMesesPactados()
        ));
        notifDirector.setTipo(TipoNotificacion.RENUNCIA_AYUDANTE);
        notifDirector.setDestinatario(contrato.getProyecto().getDirector());
        notifDirector.setEmisor(contrato.getAyudante());
        notifDirector.setUrlReferencia("/contratos/" + contratoId);

        notificacionRepository.save(notifDirector);

        // Notificar a Jefatura
        List<Usuario> jefaturas = usuarioRepository.findByRol(Rol.JEFATURA_DICC);
        for (Usuario jefatura : jefaturas) {
            Notificacion notifJefatura = new Notificacion();
            notifJefatura.setTitulo("Renuncia de Ayudante");
            notifJefatura.setMensaje(String.format(
                    "El ayudante %s ha renunciado del proyecto '%s' (%s)",
                    contrato.getAyudante().getNombreCompleto(),
                    contrato.getProyecto().getTitulo(),
                    contrato.getProyecto().getCodigoProyecto()
            ));
            notifJefatura.setTipo(TipoNotificacion.RENUNCIA_AYUDANTE);
            notifJefatura.setDestinatario(jefatura);
            notifJefatura.setEmisor(contrato.getAyudante());
            notifJefatura.setUrlReferencia("/contratos/" + contratoId);

            notificacionRepository.save(notifJefatura);
        }
    }

    @Override
    @Transactional
    public void notificarFinalizacionProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // Notificar a Jefatura
        List<Usuario> jefaturas = usuarioRepository.findByRol(Rol.JEFATURA_DICC);
        for (Usuario jefatura : jefaturas) {
            Notificacion notificacion = new Notificacion();
            notificacion.setTitulo("Proyecto Finalizado");
            notificacion.setMensaje(String.format(
                    "El proyecto '%s' (código: %s) ha finalizado",
                    proyecto.getTitulo(),
                    proyecto.getCodigoProyecto()
            ));
            notificacion.setTipo(TipoNotificacion.FINALIZACION_PROYECTO);
            notificacion.setDestinatario(jefatura);
            notificacion.setEmisor(proyecto.getDirector());
            notificacion.setUrlReferencia("/proyectos/" + proyectoId);

            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        notificacion.marcarComoLeida();
        notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByDestinatarioId(usuarioId)
                .stream()
                .map(this::mapearANotificacionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarNoLeidas(Long usuarioId) {
        return notificacionRepository.findNoLeidasPorUsuario(usuarioId)
                .stream()
                .map(this::mapearANotificacionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarNoLeidas(Long usuarioId) {
        return notificacionRepository.contarNoLeidasPorUsuario(usuarioId);
    }

    private NotificacionResponse mapearANotificacionResponse(Notificacion notificacion) {
        NotificacionResponse response = new NotificacionResponse();
        response.setId(notificacion.getId());
        response.setTitulo(notificacion.getTitulo());
        response.setMensaje(notificacion.getMensaje());
        response.setTipo(notificacion.getTipo());
        response.setFechaEnvio(notificacion.getFechaEnvio());
        response.setLeida(notificacion.getLeida());
        response.setFechaLectura(notificacion.getFechaLectura());
        response.setUrlReferencia(notificacion.getUrlReferencia());
        response.setEsUrgente(notificacion.esUrgente());
        return response;
    }
}