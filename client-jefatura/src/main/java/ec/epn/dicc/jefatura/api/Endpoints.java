package ec.epn.dicc.jefatura.api;

public class Endpoints {
  public static final String ME = "/api/v1/me";

  public static final String PROYECTOS_LIST = "/api/v1/jefatura/proyectos";
  public static final String PROYECTOS_CREAR = "/api/v1/jefatura/proyectos";
  public static final String PROYECTOS_RESUMEN = "/api/v1/jefatura/proyectos/resumen";
  public static final String PROYECTOS_AYUDANTES = "/api/v1/jefatura/proyectos/%s/ayudantes";
  public static final String PROYECTOS_ESTADISTICAS = "/api/v1/jefatura/proyectos/estadisticas";

  public static final String AYUDANTES_ACTIVOS = "/api/v1/jefatura/ayudantes/activos";
  public static final String AYUDANTES_ESTADISTICAS = "/api/v1/jefatura/ayudantes/estadisticas";

  public static final String SEMAFORO = "/api/v1/jefatura/semaforo";
}
