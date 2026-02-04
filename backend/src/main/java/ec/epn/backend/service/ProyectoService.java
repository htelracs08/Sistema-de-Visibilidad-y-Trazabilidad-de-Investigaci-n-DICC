package ec.epn.backend.service;

import ec.epn.backend.domain.exception.DominioException;
import ec.epn.backend.repository.ProfesorRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.repository.UsuarioRepo;
import ec.epn.backend.service.dto.CrearProyectoCommand;
import ec.epn.backend.service.dto.CrearProyectoResult;
import ec.epn.backend.util.PasswordGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProyectoService {

    private final ProyectoRepo proyectoRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProfesorRepo profesorRepo;
    private final NotificacionPort notificacion;

    public ProyectoService(
        ProyectoRepo proyectoRepo,
        UsuarioRepo usuarioRepo,
        ProfesorRepo profesorRepo,
        NotificacionPort notificacion
    ) {
        this.proyectoRepo = proyectoRepo;
        this.usuarioRepo = usuarioRepo;
        this.profesorRepo = profesorRepo;
        this.notificacion = notificacion;
    }

    @Transactional
    public CrearProyectoResult crearProyecto(CrearProyectoCommand cmd) {
        // 1. Crear proyecto
        String proyectoId = proyectoRepo.crear(
            cmd.codigo(),
            cmd.nombre(),
            cmd.correoDirector(),
            cmd.tipo() != null ? cmd.tipo().name() : null,
            cmd.subtipo() != null ? cmd.subtipo().name() : null
        );

        // 2. Crear director si no existe
        boolean directorCreado = false;
        boolean notificacionEnviada = false;

        if (!usuarioRepo.existsByCorreo(cmd.correoDirector())) {
            directorCreado = true;
            notificacionEnviada = crearUsuarioDirector(cmd.correoDirector(), proyectoId);
        }

        return new CrearProyectoResult(proyectoId, directorCreado, notificacionEnviada);
    }

    private boolean crearUsuarioDirector(String correo, String proyectoId) {
        var profesor = profesorRepo.findByCorreo(correo)
            .orElseThrow(() -> new DominioException(
                "No se encontró profesor con correo: " + correo
            ));

        String tempPass = PasswordGenerator.generar();

        usuarioRepo.crearUsuario(
            profesor.nombres(),
            profesor.apellidos(),
            correo,
            tempPass,
            "DIRECTOR"
        );

        try {
            notificacion.enviarCredencialesTemporalesDirector(
                correo,
                profesor.nombres(),
                profesor.apellidos(),
                tempPass,
                proyectoId
            );
            return true;
        } catch (Exception e) {
            // Log del error pero no fallar la transacción
            System.err.println("Error al enviar notificación: " + e.getMessage());
            return false;
        }
    }
}