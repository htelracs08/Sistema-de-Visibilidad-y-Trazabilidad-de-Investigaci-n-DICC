package ec.epn.backend.controller;

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

  public JefaturaController(ProfesorRepo profesorRepo, ProyectoRepo proyectoRepo, UsuarioRepo usuarioRepo, ContratoRepo contratoRepo) {
    this.profesorRepo = profesorRepo;
    this.proyectoRepo = proyectoRepo;
    this.usuarioRepo = usuarioRepo;
    this.contratoRepo = contratoRepo;
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

  @GetMapping("/proyectos/{proyectoId}/ayudantes")
  public Object listarAyudantesProyecto(@PathVariable String proyectoId) {
    return contratoRepo.listarPorProyecto(proyectoId.trim());
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


