package ec.epn.backend.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

  public String credencialesDirector(String nombres, String apellidos, String correo, String tempPass,
                                     String codigoProyecto, String nombreProyecto) {

    return ""
      + "Hola " + safe(nombres) + " " + safe(apellidos) + ",\n\n"
      + "Has sido asignado como DIRECTOR del proyecto:\n"
      + "- Código: " + safe(codigoProyecto) + "\n"
      + "- Nombre: " + safe(nombreProyecto) + "\n\n"
      + "Credenciales temporales:\n"
      + "- Usuario: " + safe(correo) + "\n"
      + "- Contraseña temporal: " + safe(tempPass) + "\n\n"
      + "Por favor cambia tu contraseña al ingresar.\n\n"
      + "Atentamente,\n"
      + "Sistema DICC";
  }

  public String credencialesAyudante(String nombres, String apellidos, String correo, String tempPass,
                                     String codigoProyecto, String nombreProyecto,
                                     String fechaInicio, String fechaFin) {

    return ""
      + "Hola " + safe(nombres) + " " + safe(apellidos) + ",\n\n"
      + "Has sido registrado como AYUDANTE en el proyecto:\n"
      + "- Código: " + safe(codigoProyecto) + "\n"
      + "- Nombre: " + safe(nombreProyecto) + "\n"
      + "- Inicio contrato: " + safe(fechaInicio) + "\n"
      + "- Fin contrato: " + safe(fechaFin) + "\n\n"
      + "Credenciales temporales:\n"
      + "- Usuario: " + safe(correo) + "\n"
      + "- Contraseña temporal: " + safe(tempPass) + "\n\n"
      + "Por favor cambia tu contraseña al ingresar.\n\n"
      + "Atentamente,\n"
      + "Sistema DICC";
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }
}
