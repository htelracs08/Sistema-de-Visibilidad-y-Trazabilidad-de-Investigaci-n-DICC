package ec.epn.backend.domain;

public record Usuario(
    String id,
    String nombres,
    String apellidos,
    String correo,
    String password,
    String rol,
    boolean debeCambiarPassword
) {}
