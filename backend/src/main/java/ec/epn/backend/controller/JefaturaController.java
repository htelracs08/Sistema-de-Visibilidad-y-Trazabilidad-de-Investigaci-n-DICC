package ec.epn.backend.controller;

import ec.epn.backend.repository.BitacoraRepo;
import ec.epn.backend.repository.ContratoRepo;
import ec.epn.backend.repository.ProfesorRepo;
import ec.epn.backend.repository.ProyectoRepo;
import ec.epn.backend.repository.UsuarioRepo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jefatura")
public class JefaturaController {

  private final ProyectoRepo proyectoRepo;
  private final UsuarioRepo usuarioRepo;
  private final ProfesorRepo profesorRepo;
  private final ContratoRepo contratoRepo;
  private final BitacoraRepo bitacoraRepo;

  public JefaturaController(ProfesorRepo profesorRepo, ProyectoRepo proyectoRepo, UsuarioRepo usuarioRepo, ContratoRepo contratoRepo, BitacoraRepo bitacoraRepo) {
    this.profesorRepo = profesorRepo;
    this.proyectoRepo = proyectoRepo;
    this.usuarioRepo = usuarioRepo;
    this.contratoRepo = contratoRepo;
    this.bitacoraRepo = bitacoraRepo;
  }

  @GetMapping("/ayudantes/activos")
  public Object totalActivos() {
    return java.util.Map.of("activos", contratoRepo.contarActivosGlobal());
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
    return java.util.Map.of(
      "activosTotal", activosTotal,
      "porTipo", porTipo
    );
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

    var out = new java.util.ArrayList<java.util.Map<String, Object>>();

    for (var c : contratos) {
      String contratoId = (String) c.get("contratoId");
      var fechaInicio = java.time.LocalDate.parse((String) c.get("fechaInicio"));

      // meses esperados: desde (anio/mes de inicio) hasta (anio/mes de hoy), inclusive
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


  @PostMapping("/proyectos")
  public Object crearProyecto(@RequestBody ec.epn.backend.dto.CrearProyectoReq req) {
    if (req.codigo() == null || req.codigo().isBlank()) {
      return java.util.Map.of("ok", false, "msg", "codigo es requerido");
    }
    if (req.nombre() == null || req.nombre().isBlank()) {
      return java.util.Map.of("ok", false, "msg", "nombre es requerido");
    }
    if (req.correoDirector() == null || req.correoDirector().isBlank()) {
      return java.util.Map.of("ok", false, "msg", "correoDirector es requerido");
    }

    // 1) crear proyecto
    var proyectoId = proyectoRepo.crear(req.codigo().trim(), req.nombre().trim(), req.correoDirector().trim().toLowerCase());

    // 2) crear usuario director si no existe
    var correo = req.correoDirector().trim().toLowerCase();
    if (!usuarioRepo.existsByCorreo(correo)) {
      // buscamos datos del profesor para nombres/apellidos (si existe)
      var prof = profesorRepo.findByCorreo(correo).orElse(null);

      String nombres = (prof != null) ? prof.nombres() : "DIRECTOR";
      String apellidos = (prof != null) ? prof.apellidos() : "PROYECTO";

      String tempPass = "Temp123*"; // luego lo hacemos random
      usuarioRepo.crearUsuario(nombres, apellidos, correo, tempPass, "DIRECTOR");

      // por ahora solo lo logueamos (luego Outlook SMTP)
      System.out.println("[CREDENCIALES DIRECTOR] correo=" + correo + " pass=" + tempPass);
    }

    return java.util.Map.of("ok", true, "proyectoId", proyectoId);
  }

  @GetMapping("/proyectos")
  public Object listarProyectos() {
    return proyectoRepo.findAll();
  }


}


