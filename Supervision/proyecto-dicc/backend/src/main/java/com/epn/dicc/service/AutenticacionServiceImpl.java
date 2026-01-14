package com.epn.dicc.service;

import com.epn.dicc.dto.request.LoginRequest;
import com.epn.dicc.dto.request.RegistroUsuarioRequest;
import com.epn.dicc.dto.response.TokenResponse;
import com.epn.dicc.dto.response.UsuarioResponse;
import com.epn.dicc.exception.BusinessException;
import com.epn.dicc.exception.UnauthorizedException;
import com.epn.dicc.model.*;
import com.epn.dicc.model.enums.Rol;
import com.epn.dicc.repository.*;
import com.epn.dicc.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de autenticación
 */
@Service
@RequiredArgsConstructor
public class AutenticacionServiceImpl implements IAutenticacionService {

    private final IUsuarioRepository usuarioRepository;
    private final IConfiguracionRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // Buscar usuario por correo
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(request.getCorreoInstitucional())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        // CAMBIO: Verificar contraseña SIN hash (comparación directa)
        if (!request.getPassword().equals(usuario.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        // Verificar que el usuario esté activo
        if (!usuario.estaActivo()) {
            throw new BusinessException("El usuario está inactivo");
        }

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuario);
        LocalDateTime expiracion = jwtTokenProvider.getExpirationDateFromToken(token);

        // Construir respuesta
        return new TokenResponse(
                token,
                "Bearer",
                expiracion,
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getRol()
        );
    }

    @Override
    @Transactional
    public UsuarioResponse registrarUsuario(RegistroUsuarioRequest request) {
        // Validar correo institucional
        validarCorreoInstitucional(request.getCorreoInstitucional());

        // Verificar que no exista el usuario
        if (usuarioRepository.existsByCorreoInstitucional(request.getCorreoInstitucional())) {
            throw new BusinessException("El correo institucional ya está registrado");
        }

        if (usuarioRepository.existsByCodigoEPN(request.getCodigoEPN())) {
            throw new BusinessException("El código EPN ya está registrado");
        }

        if (usuarioRepository.existsByCedula(request.getCedula())) {
            throw new BusinessException("La cédula ya está registrada");
        }

        // Crear usuario según el rol
        Usuario usuario;
        switch (request.getRol()) {
            case JEFATURA_DICC:
                usuario = crearJefatura(request);
                break;
            case DIRECTOR_PROYECTO:
                usuario = crearDocente(request);
                break;
            case AYUDANTE_PROYECTO:
                usuario = crearAyudante(request);
                break;
            default:
                throw new BusinessException("Rol no válido");
        }

        // CAMBIO: Guardar contraseña SIN hash (solo para prototipo)
        usuario.setPasswordHash(request.getPassword()); // ← SIN passwordEncoder
        usuario.setFechaRegistro(LocalDateTime.now());

        // Guardar
        usuario = usuarioRepository.save(usuario);

        // Retornar respuesta
        return mapearAUsuarioResponse(usuario);
    }

    @Override
    public void validarCorreoInstitucional(String correo) {
        if (!correo.toLowerCase().endsWith("@epn.edu.ec")) {
            throw new BusinessException("Debe usar un correo institucional @epn.edu.ec");
        }
    }

    @Override
    public boolean verificarCodigoJefatura(String codigo) {
        return configuracionRepository.findByClaveConfiguracion("CODIGO_REGISTRO_JEFATURA")
                .map(config -> config.getValorConfiguracion().equals(codigo))
                .orElse(false);
    }

    // Métodos privados auxiliares

    private JefaturaDICC crearJefatura(RegistroUsuarioRequest request) {
        // Verificar código especial
        if (request.getCodigoEspecialJefatura() == null || 
            !verificarCodigoJefatura(request.getCodigoEspecialJefatura())) {
            throw new BusinessException("Código especial de Jefatura inválido");
        }

        // Verificar que no exista ya una jefatura
        if (usuarioRepository.findByRol(Rol.JEFATURA_DICC).size() > 0) {
            throw new BusinessException("Ya existe un usuario Jefatura registrado");
        }

        JefaturaDICC jefatura = new JefaturaDICC();
        mapearDatosBasicos(jefatura, request);
        jefatura.setCargo("Director del DICC");
        jefatura.setCodigoRegistroEspecial(request.getCodigoEspecialJefatura());
        jefatura.setRol(Rol.JEFATURA_DICC);

        return jefatura;
    }

    private Docente crearDocente(RegistroUsuarioRequest request) {
        Docente docente = new Docente();
        mapearDatosBasicos(docente, request);
        docente.setDepartamento(request.getDepartamento());
        docente.setCubiculo(request.getCubiculo());
        docente.setExtension(request.getExtension());
        docente.setAreaInvestigacion(request.getAreaInvestigacion());
        docente.setRol(Rol.DIRECTOR_PROYECTO);

        return docente;
    }

    private Ayudante crearAyudante(RegistroUsuarioRequest request) {
        Ayudante ayudante = new Ayudante();
        mapearDatosBasicos(ayudante, request);
        ayudante.setCarrera(request.getCarrera());
        ayudante.setFacultad(request.getFacultad());
        ayudante.setQuintil(request.getQuintil());
        ayudante.setSemestreActual(request.getSemestreActual());
        ayudante.setPromedioGeneral(request.getPromedioGeneral());
        ayudante.setRol(Rol.AYUDANTE_PROYECTO);

        return ayudante;
    }

    private void mapearDatosBasicos(Usuario usuario, RegistroUsuarioRequest request) {
        usuario.setCodigoEPN(request.getCodigoEPN());
        usuario.setCedula(request.getCedula());
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setCorreoInstitucional(request.getCorreoInstitucional());
        usuario.setEmailVerificado(true); // En producción enviarías un correo
    }

    private UsuarioResponse mapearAUsuarioResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setCodigoEPN(usuario.getCodigoEPN());
        response.setCedula(usuario.getCedula());
        response.setNombres(usuario.getNombres());
        response.setApellidos(usuario.getApellidos());
        response.setNombreCompleto(usuario.getNombreCompleto());
        response.setCorreoInstitucional(usuario.getCorreoInstitucional());
        response.setRol(usuario.getRol());
        response.setEmailVerificado(usuario.getEmailVerificado());
        response.setFechaRegistro(usuario.getFechaRegistro());

        // Campos específicos
        if (usuario instanceof Docente) {
            response.setDepartamento(((Docente) usuario).getDepartamento());
        } else if (usuario instanceof Ayudante) {
            response.setCarrera(((Ayudante) usuario).getCarrera());
            response.setQuintil(((Ayudante) usuario).getQuintil());
        }

        return response;
    }
}