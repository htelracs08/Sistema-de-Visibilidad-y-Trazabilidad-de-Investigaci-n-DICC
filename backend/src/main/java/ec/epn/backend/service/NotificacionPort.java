package ec.epn.backend.service;

public interface NotificacionPort {

  void enviarCredencialesTemporalesDirector(
      String correo,
      String nombres,
      String apellidos,
      String tempPassword,
      String proyectoId
  );

  void enviarCredencialesTemporalesAyudante(
      String correo,
      String nombres,
      String apellidos,
      String tempPassword,
      String proyectoId,
      String fechaInicio,
      String fechaFin
  );
}
