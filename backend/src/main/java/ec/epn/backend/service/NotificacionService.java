package ec.epn.backend.service;

import ec.epn.backend.repository.ProyectoRepo;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificacionService implements NotificacionPort {

  private final EmailService emailService;
  private final EmailTemplates templates;
  private final ProyectoRepo proyectoRepo;

  public NotificacionService(EmailService emailService, EmailTemplates templates, ProyectoRepo proyectoRepo) {
    this.emailService = emailService;
    this.templates = templates;
    this.proyectoRepo = proyectoRepo;
  }

  @Override
  public void enviarCredencialesTemporalesDirector(
      String correo,
      String nombres,
      String apellidos,
      String tempPassword,
      String proyectoId
  ) {
    Map<String, Object> p = proyectoRepo.obtenerBasicoPorId(proyectoId);

    String codigo = p == null ? "" : String.valueOf(p.get("codigo"));
    String nombre = p == null ? "" : String.valueOf(p.get("nombre"));

    String subject = "Credenciales temporales – Director del Proyecto " + codigo;

    String body = templates.credencialesDirector(
        nombres,
        apellidos,
        correo,
        tempPassword,
        codigo,
        nombre
    );

    emailService.sendPlainText(correo, subject, body);
  }

  @Override
  public void enviarCredencialesTemporalesAyudante(
      String correo,
      String nombres,
      String apellidos,
      String tempPassword,
      String proyectoId,
      String fechaInicio,
      String fechaFin
  ) {
    Map<String, Object> p = proyectoRepo.obtenerBasicoPorId(proyectoId);

    String codigo = p == null ? "" : String.valueOf(p.get("codigo"));
    String nombre = p == null ? "" : String.valueOf(p.get("nombre"));

    String subject = "Credenciales temporales – Ayudante (" + codigo + ")";

    String body = templates.credencialesAyudante(
        nombres,
        apellidos,
        correo,
        tempPassword,
        codigo,
        nombre,
        fechaInicio,
        fechaFin
    );

    emailService.sendPlainText(correo, subject, body);
  }
}
