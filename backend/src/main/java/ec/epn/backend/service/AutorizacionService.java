package ec.epn.backend.service;

import ec.epn.backend.domain.exception.OperacionNoPermitidaException;
import ec.epn.backend.repository.BitacoraEstadoRepo;
import ec.epn.backend.repository.BitacoraSeguridadRepo;
import ec.epn.backend.repository.ContratoRepo;
import org.springframework.stereotype.Service;

@Service
public class AutorizacionService {

    private final ContratoRepo contratoRepo;
    private final BitacoraSeguridadRepo bitacoraSeguridadRepo;
    private final BitacoraEstadoRepo bitacoraEstadoRepo;

    public AutorizacionService(
        ContratoRepo contratoRepo,
        BitacoraSeguridadRepo bitacoraSeguridadRepo,
        BitacoraEstadoRepo bitacoraEstadoRepo
    ) {
        this.contratoRepo = contratoRepo;
        this.bitacoraSeguridadRepo = bitacoraSeguridadRepo;
        this.bitacoraEstadoRepo = bitacoraEstadoRepo;
    }

    public void verificarAyudantePuedeEditarBitacora(
        String correoAyudante,
        String bitacoraId
    ) {
        // 1. Verificar contrato activo
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(
            correoAyudante.trim().toLowerCase()
        );

        if (contratoId == null) {
            throw new OperacionNoPermitidaException(
                "No existe contrato activo para este ayudante"
            );
        }

        // 2. Verificar pertenencia
        boolean pertenece = bitacoraSeguridadRepo.perteneceAContrato(
            bitacoraId,
            contratoId
        );

        if (!pertenece) {
            throw new OperacionNoPermitidaException(
                "La bitácora no pertenece al contrato activo del ayudante"
            );
        }

        // 3. Verificar estado editable
        String estado = bitacoraEstadoRepo.obtenerEstado(bitacoraId);
        
        if (!"BORRADOR".equalsIgnoreCase(estado) && 
            !"RECHAZADA".equalsIgnoreCase(estado)) {
            throw new OperacionNoPermitidaException(
                "La bitácora no está en estado editable. Estado actual: " + estado
            );
        }
    }

    public void verificarDirectorPuedeRevisarBitacora(
        String correoDirector,
        String bitacoraId
    ) {
        boolean puedeRevisar = bitacoraSeguridadRepo.directorPuedeRevisarBitacora(
            bitacoraId,
            correoDirector.trim().toLowerCase()
        );

        if (!puedeRevisar) {
            throw new OperacionNoPermitidaException(
                "No tiene permisos para revisar esta bitácora"
            );
        }

        // Verificar estado ENVIADA
        String estado = bitacoraEstadoRepo.obtenerEstado(bitacoraId);
        
        if (!"ENVIADA".equalsIgnoreCase(estado)) {
            throw new OperacionNoPermitidaException(
                "Solo se pueden revisar bitácoras en estado ENVIADA. Estado actual: " + estado
            );
        }
    }
}