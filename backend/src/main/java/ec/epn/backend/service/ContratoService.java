package ec.epn.backend.service;

import ec.epn.backend.domain.Ayudante;
import ec.epn.backend.domain.exception.CupoAgotadoException;
import ec.epn.backend.domain.exception.DominioException;
import ec.epn.backend.repository.AyudanteRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.repository.UsuarioRepo;
import ec.epn.backend.service.dto.RegistrarAyudanteCommand;
import ec.epn.backend.service.dto.RegistrarAyudanteResult;
import ec.epn.backend.util.PasswordGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoService {

    private final ContratoRepo contratoRepo;
    private final AyudanteRepo ayudanteRepo;
    private final ProyectoRepo proyectoRepo;
    private final UsuarioRepo usuarioRepo;
    private final NotificacionPort notificacion;

    public ContratoService(
        ContratoRepo contratoRepo,
        AyudanteRepo ayudanteRepo,
        ProyectoRepo proyectoRepo,
        UsuarioRepo usuarioRepo,
        NotificacionPort notificacion
    ) {
        this.contratoRepo = contratoRepo;
        this.ayudanteRepo = ayudanteRepo;
        this.proyectoRepo = proyectoRepo;
        this.usuarioRepo = usuarioRepo;
        this.notificacion = notificacion;
    }

    @Transactional
    public RegistrarAyudanteResult registrarAyudante(
        String proyectoId,
        RegistrarAyudanteCommand cmd
    ) {
        // 1. Validar cupo disponible
        validarCupoDisponible(proyectoId);

        // 2. Obtener o crear ayudante
        String correo = cmd.correoInstitucional().trim().toLowerCase();
        var existente = ayudanteRepo.findByCorreoInstitucional(correo).orElse(null);
        
        String ayudanteId;
        boolean ayudanteCreado;
        
        if (existente == null) {
            ayudanteId = ayudanteRepo.crear(new Ayudante(
                null,
                cmd.nombres().trim(),
                cmd.apellidos().trim(),
                correo,
                cmd.facultad().trim(),
                cmd.quintil(),
                cmd.tipoAyudante().trim()
            ));
            ayudanteCreado = true;
        } else {
            ayudanteId = existente.id();
            ayudanteCreado = false;
        }

        // 3. Crear contrato
        String contratoId = contratoRepo.crear(
            proyectoId,
            ayudanteId,
            cmd.fechaInicio().toString(),
            cmd.fechaFin().toString()
        );

        // 4. Crear usuario si no existe y notificar
        boolean usuarioCreado = crearUsuarioSiNoExiste(
            cmd.nombres().trim(),
            cmd.apellidos().trim(),
            correo,
            proyectoId,
            cmd.fechaInicio().toString(),
            cmd.fechaFin().toString()
        );

        return new RegistrarAyudanteResult(
            ayudanteId,
            contratoId,
            ayudanteCreado,
            usuarioCreado
        );
    }

    private void validarCupoDisponible(String proyectoId) {
        Integer maxAyudantes = proyectoRepo.obtenerMaxAyudantes(proyectoId);
        
        if (maxAyudantes == null) {
            throw new DominioException("Proyecto no encontrado");
        }
        
        if (maxAyudantes <= 0) {
            throw new DominioException(
                "El proyecto no tiene configurado maxAyudantes. " +
                "Complete los detalles del proyecto primero."
            );
        }

        int activos = contratoRepo.contarActivosPorProyecto(proyectoId);
        
        if (activos >= maxAyudantes) {
            throw new CupoAgotadoException(activos, maxAyudantes);
        }
    }

    private boolean crearUsuarioSiNoExiste(
        String nombres,
        String apellidos,
        String correo,
        String proyectoId,
        String fechaInicio,
        String fechaFin
    ) {
        if (usuarioRepo.existsByCorreo(correo)) {
            return false;
        }

        String tempPass = PasswordGenerator.generar();

        usuarioRepo.crearUsuario(
            nombres,
            apellidos,
            correo,
            tempPass,
            "AYUDANTE"
        );

        try {
            notificacion.enviarCredencialesTemporalesAyudante(
                correo,
                nombres,
                apellidos,
                tempPass,
                proyectoId,
                fechaInicio,
                fechaFin
            );
        } catch (Exception e) {
            // Log pero no fallar
            System.err.println("Error al enviar notificación: " + e.getMessage());
        }

        return true;
    }

    @Transactional
    public void finalizarContrato(String contratoId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo es requerido");
        }

        String motivoUpper = motivo.trim().toUpperCase();
        
        if (!motivoUpper.equals("RENUNCIA") && 
            !motivoUpper.equals("FIN_CONTRATO") && 
            !motivoUpper.equals("DESPIDO")) {
            throw new IllegalArgumentException(
                "Motivo inválido. Debe ser: RENUNCIA, FIN_CONTRATO o DESPIDO"
            );
        }

        contratoRepo.finalizar(contratoId, motivoUpper);
    }
}