package ec.epn.backend.controller;

import ec.epn.backend.dto.CrearProyectoReq;
import ec.epn.backend.repository.BitacoraRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.ProfesorRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.repository.UsuarioRepo;
import ec.epn.backend.service.NotificacionPort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jefatura")
public class JefaturaController {

  private final ProyectoRepo proyectoRepo;
  private final UsuarioRepo usuarioRepo;
  private final ProfesorRepo profesorRepo;
  private final ContratoRepo contratoRepo;
  private final BitacoraRepo bitacoraRepo;
  private final NotificacionPort notificacion;

  public JefaturaController(
      ProfesorRepo profesorRepo,
      ProyectoRepo proyectoRepo,
      UsuarioRepo usuarioRepo,
      ContratoRepo contratoRepo,
      BitacoraRepo bitacoraRepo,
      NotificacionPort notificacion
  ) {
    this.profesorRepo = profesorRepo;
    this.proyectoRepo = proyectoRepo;
    this.usuarioRepo = usuarioRepo;
    this.contratoRepo = contratoRepo;
    this.bitacoraRepo = bitacoraRepo;
    this.notificacion = notificacion;
  }

  @GetMapping("/ayudantes/activos")
  public Object totalActivos() {
    return Map.of("activos", contratoRepo.contarActivosGlobal());
  }

  @GetMapping("/profesores")
  public List<?> listarProfesores() {
    return profesorRepo.findAll();
  }

  @GetMapping("/proyectos/resumen")
  public Object resumenProyectos() {
    return proyectoRepo.listarResumen();
  }

  @GetMapping("/ayudantes/estadisticas")
  public Object estadisticasAyudantes() {
    int activosTotal = contratoRepo.contarActivosGlobal();
    var porTipo = contratoRepo.contarActivosPorTipoAyudante();
    return Map.of("activosTotal", activosTotal, "porTipo", porTipo);
  }

  @GetMapping("/proyectos/{proyectoId}/ayudantes")
  public Object listarAyudantesProyecto(@PathVariable String proyectoId) {
    return contratoRepo.listarPorProyecto(proyectoId.trim());
  }

  @GetMapping("/proyectos/estadisticas")
  public Object estadisticasProyectos() {
    return proyectoRepo.estadisticasPorTipoYEstado();
  }

  @GetMapping("/semaforo")
  public Object semaforo() {
    var contratos = contratoRepo.listarActivosDetallado();
    var hoy = java.time.LocalDate.now();

    var out = new java.util.ArrayList<Map<String, Object>>();

    for (var c : contratos) {
      String contratoId = (String) c.get("contratoId");
      var fechaInicio = java.time.LocalDate.parse((String) c.get("fechaInicio"));

      int anioDesde = fechaInicio.getYear();
      int mesDesde = fechaInicio.getMonthValue();
      int anioHasta = hoy.getYear();
      int mesHasta = hoy.getMonthValue();

      int mesesEsperados = ((anioHasta - anioDesde) * 12) + (mesHasta - mesDesde) + 1;
      if (mesesEsperados < 0) mesesEsperados = 0;

      int mesesAprobados = bitacoraRepo.contarAprobadasEnRango(contratoId, anioDesde, mesDesde, anioHasta, mesHasta);
      int faltantes = Math.max(0, mesesEsperados - mesesAprobados);

      String color = (faltantes == 0) ? "VERDE" : (faltantes == 1 ? "AMARILLO" : "ROJO");

      var m = new java.util.LinkedHashMap<String, Object>();
      m.putAll(c);
      m.put("anioDesde", anioDesde);
      m.put("mesDesde", mesDesde);
      m.put("anioHasta", anioHasta);
      m.put("mesHasta", mesHasta);
      m.put("mesesEsperados", mesesEsperados);
      m.put("mesesAprobados", mesesAprobados);
      m.put("faltantes", faltantes);
      m.put("color", color);

      out.add(m);
    }

    return out;
  }

  // ✅ CREA PROYECTO + CREA USUARIO DIRECTOR (si no existe) + ENVÍA CORREO
  @PostMapping("/proyectos")
  public Object crearProyecto(@RequestBody CrearProyectoReq req) {

    System.out.println("REQ = " + req);

    if (req == null) return Map.of("ok", false, "msg", "body requerido");

    if (req.codigo() == null || req.codigo().isBlank()) {
      return Map.of("ok", false, "msg", "codigo es requerido");
    }
    if (req.nombre() == null || req.nombre().isBlank()) {
      return Map.of("ok", false, "msg", "nombre es requerido");
    }
    if (req.correoDirector() == null || req.correoDirector().isBlank()) {
      return Map.of("ok", false, "msg", "correoDirector es requerido");
    }

    String codigo = req.codigo().trim();
    String nombre = req.nombre().trim();
    String correoDirector = req.correoDirector().trim().toLowerCase();
    String tipoProyecto = req.getTipoProyecto() != null ? req.getTipoProyecto().trim() : null;
    String subtipoProyecto = req.getSubtipoProyecto() != null ? req.getSubtipoProyecto().trim() : null;

    System.out.println("REQ tipoProyecto=" + tipoProyecto + " subtipoProyecto=" + subtipoProyecto);

    if (tipoProyecto == null || !"INVESTIGACION".equalsIgnoreCase(tipoProyecto)) {
      subtipoProyecto = null;
    }

    // 1) crear proyecto
    String proyectoId = proyectoRepo.crear(codigo, nombre, correoDirector, tipoProyecto, subtipoProyecto);

    // 2) crear usuario director si no existe
    boolean seCreoUsuarioDirector = false;
    String tempPass = "Temp123*";

    if (!usuarioRepo.existsByCorreo(correoDirector)) {
      var prof = profesorRepo.findByCorreo(correoDirector).orElse(null);

      String nombres = (prof != null) ? prof.nombres() : "DIRECTOR";
      String apellidos = (prof != null) ? prof.apellidos() : "PROYECTO";

      usuarioRepo.crearUsuario(nombres, apellidos, correoDirector, tempPass, "DIRECTOR");
      seCreoUsuarioDirector = true;

      try {
        notificacion.enviarCredencialesTemporalesDirector(
            correoDirector,
            nombres,
            apellidos,
            tempPass,
            proyectoId
        );
      } catch (Exception e) {
        System.out.println("[MAIL ERROR] No se pudo enviar correo a DIRECTOR: " + e.getMessage());
      }
    }

    return Map.of(
        "ok", true,
        "proyectoId", proyectoId,
        "directorCreado", seCreoUsuarioDirector
    );
  }

  @GetMapping("/proyectos")
  public Object listarProyectos() {
    return proyectoRepo.findAll();
  }
}
