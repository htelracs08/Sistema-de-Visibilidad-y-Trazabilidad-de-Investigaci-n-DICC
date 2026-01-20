package ec.epn.backend.controller;

import ec.epn.backend.dto.ActualizarProyectoReq;
import ec.epn.backend.repository.ProyectoRepo;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
public class DirectorProyectoController {

  private final ProyectoRepo proyectoRepo;

  public DirectorProyectoController(ProyectoRepo proyectoRepo) {
    this.proyectoRepo = proyectoRepo;
  }

  @PutMapping("/proyectos/{proyectoId}")
  public Object actualizar(@PathVariable String proyectoId, @RequestBody ActualizarProyectoReq req) {

    if (req.maxAyudantes() == null || req.maxAyudantes() < 0) {
      return Map.of("ok", false, "msg", "maxAyudantes es requerido (>=0)");
    }
    if (req.maxArticulos() == null || req.maxArticulos() < 0) {
      return Map.of("ok", false, "msg", "maxArticulos es requerido (>=0)");
    }
    if (req.tipo() == null || req.tipo().isBlank()) {
      return Map.of("ok", false, "msg", "tipo es requerido");
    }

    // Normalizar strings
    String tipo = req.tipo().trim().toUpperCase();
    String subtipo = (req.subtipo() == null || req.subtipo().isBlank())
      ? null
      : req.subtipo().trim().toUpperCase();

    // Regla: subtipo solo si INVESTIGACION
    if (!"INVESTIGACION".equals(tipo)) {
      subtipo = null;
    }

    // Validación de fechas (si vienen, deben ser válidas)
    String fechaInicio = (req.fechaInicio() == null || req.fechaInicio().isBlank()) ? null : req.fechaInicio().trim();
    String fechaFin = (req.fechaFin() == null || req.fechaFin().isBlank()) ? null : req.fechaFin().trim();

    LocalDate fi = null;
    LocalDate ff = null;

    try {
      if (fechaInicio != null) fi = LocalDate.parse(fechaInicio); // YYYY-MM-DD
      if (fechaFin != null) ff = LocalDate.parse(fechaFin);
    } catch (Exception e) {
      return Map.of("ok", false, "msg", "Fechas deben estar en formato YYYY-MM-DD");
    }

    if (fi != null && ff != null && ff.isBefore(fi)) {
      return Map.of("ok", false, "msg", "fechaFin no puede ser antes que fechaInicio");
    }

    int n = proyectoRepo.actualizarDetalles(
      proyectoId.trim(),
      fechaInicio,
      fechaFin,
      tipo,
      subtipo,
      req.maxAyudantes(),
      req.maxArticulos()
    );

    if (n == 0) return Map.of("ok", false, "msg", "Proyecto no encontrado");

    return Map.of("ok", true);
  }
}
