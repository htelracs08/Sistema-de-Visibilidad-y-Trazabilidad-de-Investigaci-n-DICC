package ec.epn.backend.service;

import ec.epn.backend.domain.exception.DominioException;
import ec.epn.backend.domain.exception.OperacionNoPermitidaException;
import ec.epn.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class BitacoraService {

    private final BitacoraBaseRepo bitacoraBaseRepo;
    private final BitacoraEstadoRepo bitacoraEstadoRepo;
    private final BitacoraSeguridadRepo bitacoraSeguridadRepo;
    private final InformeSemanalRepo informeSemanalRepo;
    private final ActividadRepo actividadRepo;
    private final ContratoRepo contratoRepo;

    public BitacoraService(
        BitacoraBaseRepo bitacoraBaseRepo,
        BitacoraEstadoRepo bitacoraEstadoRepo,
        BitacoraSeguridadRepo bitacoraSeguridadRepo,
        InformeSemanalRepo informeSemanalRepo,
        ActividadRepo actividadRepo,
        ContratoRepo contratoRepo
    ) {
        this.bitacoraBaseRepo = bitacoraBaseRepo;
        this.bitacoraEstadoRepo = bitacoraEstadoRepo;
        this.bitacoraSeguridadRepo = bitacoraSeguridadRepo;
        this.informeSemanalRepo = informeSemanalRepo;
        this.actividadRepo = actividadRepo;
        this.contratoRepo = contratoRepo;
    }

    public String obtenerBitacoraActual(String correoAyudante) {
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(
            correoAyudante.trim().toLowerCase()
        );

        if (contratoId == null) {
            throw new DominioException("No existe contrato activo para este usuario");
        }

        return obtenerOCrearBitacora(contratoId);
    }

    private String obtenerOCrearBitacora(String contratoId) {
        // 1. Verificar si hay borrador activo
        String borradorId = bitacoraEstadoRepo.obtenerBorradorActiva(contratoId);
        if (borradorId != null) {
            return borradorId;
        }

        // 2. Obtener última bitácora
        var ultima = bitacoraEstadoRepo.obtenerUltimaPorContrato(contratoId);
        if (ultima == null) {
            // Primera bitácora del contrato
            LocalDate hoy = LocalDate.now();
            return bitacoraBaseRepo.crear(contratoId, hoy.getYear(), hoy.getMonthValue());
        }

        String estado = String.valueOf(ultima.get("estado"));

        // 3. Si la última está RECHAZADA, reabrir esa
        if ("RECHAZADA".equalsIgnoreCase(estado)) {
            return String.valueOf(ultima.get("id"));
        }

        // 4. Si está APROBADA o ENVIADA, crear la siguiente
        if ("APROBADA".equalsIgnoreCase(estado) || "ENVIADA".equalsIgnoreCase(estado)) {
            Number anioN = (Number) ultima.get("anio");
            Number mesN = (Number) ultima.get("mes");
            int anio = anioN == null ? LocalDate.now().getYear() : anioN.intValue();
            int mes = mesN == null ? LocalDate.now().getMonthValue() : mesN.intValue();

            int[] siguiente = calcularSiguientePeriodo(anio, mes);
            return bitacoraBaseRepo.crear(contratoId, siguiente[0], siguiente[1]);
        }

        // Por defecto, devolver la última
        return String.valueOf(ultima.get("id"));
    }

    private int[] calcularSiguientePeriodo(int anio, int mes) {
        int nextAnio = anio;
        int nextMes = mes + 1;
        if (nextMes > 12) {
            nextMes = 1;
            nextAnio++;
        }
        return new int[]{nextAnio, nextMes};
    }

    @Transactional
    public void enviarBitacora(String bitacoraId, String correoAyudante) {
        // 1. Validar pertenencia
        validarPerteneceAyudante(bitacoraId, correoAyudante);

        // 2. Validar estado
        String estado = bitacoraEstadoRepo.obtenerEstado(bitacoraId);
        if (!"BORRADOR".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
            throw new OperacionNoPermitidaException(
                "Solo se puede enviar bitácoras en BORRADOR o RECHAZADA. Estado actual: " + estado
            );
        }

        // 3. Validar contenido mínimo
        validarContenidoMinimo(bitacoraId);

        // 4. Enviar
        int rows = bitacoraEstadoRepo.enviar(bitacoraId);
        if (rows == 0) {
            throw new DominioException("No se pudo enviar la bitácora");
        }
    }

    private void validarPerteneceAyudante(String bitacoraId, String correoAyudante) {
        String contratoId = contratoRepo.obtenerContratoActivoPorCorreo(
            correoAyudante.trim().toLowerCase()
        );

        if (contratoId == null) {
            throw new OperacionNoPermitidaException("No existe contrato activo");
        }

        boolean pertenece = bitacoraSeguridadRepo.perteneceAContrato(bitacoraId, contratoId);
        if (!pertenece) {
            throw new OperacionNoPermitidaException(
                "La bitácora no pertenece a tu contrato activo"
            );
        }
    }

    private void validarContenidoMinimo(String bitacoraId) {
        int semanas = informeSemanalRepo.contarPorBitacora(bitacoraId);
        if (semanas <= 0) {
            throw new DominioException(
                "No se puede enviar: la bitácora debe tener al menos una semana"
            );
        }

        int actividades = actividadRepo.contarPorBitacora(bitacoraId);
        if (actividades <= 0) {
            throw new DominioException(
                "No se puede enviar: la bitácora debe tener al menos una actividad"
            );
        }
    }

    @Transactional
    public void reabrirBitacora(String bitacoraId, String correoAyudante) {
        // 1. Validar pertenencia
        validarPerteneceAyudante(bitacoraId, correoAyudante);

        // 2. Validar estado RECHAZADA
        String estado = bitacoraEstadoRepo.obtenerEstado(bitacoraId);
        if (!"RECHAZADA".equalsIgnoreCase(estado)) {
            throw new OperacionNoPermitidaException(
                "Solo se puede reabrir bitácoras RECHAZADAS. Estado actual: " + estado
            );
        }

        // 3. Reabrir
        int rows = bitacoraEstadoRepo.reabrirRechazada(bitacoraId);
        if (rows == 0) {
            throw new DominioException("No se pudo reabrir la bitácora");
        }
    }

    public void validarBitacoraEditable(String bitacoraId, String correoAyudante) {
        // 1. Validar pertenencia
        validarPerteneceAyudante(bitacoraId, correoAyudante);

        // 2. Validar estado editable
        String estado = bitacoraEstadoRepo.obtenerEstado(bitacoraId);
        if (!"BORRADOR".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
            throw new OperacionNoPermitidaException(
                "La bitácora no es editable. Estado actual: " + estado
            );
        }
    }
}