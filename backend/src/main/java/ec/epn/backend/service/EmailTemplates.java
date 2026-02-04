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
    + "- Contraseña: " + safe(tempPass) + "\n\n"
    + "Por favor cambia tu contraseña al ingresar.\n\n"
    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
    + "⚠️  ADVERTENCIA IMPORTANTE\n"
    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
    + "Como Director del proyecto, es su responsabilidad:\n\n"
    + "1. Registrar a todos los ayudantes asignados al proyecto\n"
    + "2. Supervisar y aprobar las bitácoras mensuales de cada ayudante\n"
    + "3. Verificar que existan registros de actividades consistentes durante todo el semestre\n\n"
    + "IMPORTANTE: Al finalizar el semestre se realizará una auditoría del proyecto.\n"
    + "Si se detectan las siguientes inconsistencias, NO SE FIRMARÁ el informe semestral:\n\n"
    + "  • Ausencia de ayudantes registrados en el sistema\n"
    + "  • Falta de bitácoras mensuales aprobadas\n"
    + "  • Periodos extensos sin actividad documentada\n"
    + "  • Inconsistencias entre los avances reportados y los registros del sistema\n\n"
    + "La falta de firma en el informe semestral puede afectar la continuidad\n"
    + "y evaluación del proyecto.\n\n"
    + "Por favor, mantenga el sistema actualizado y revise periódicamente\n"
    + "el progreso de sus ayudantes.\n\n"
    + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
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
      + "- Contraseña: " + safe(tempPass) + "\n\n"
      + "Por favor cambia tu contraseña al ingresar.\n\n"
      + "Atentamente,\n"
      + "Sistema DICC";
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }
}