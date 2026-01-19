package ec.epn.backend.controller;

import ec.epn.backend.repository.ProfesorRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jefatura")
public class JefaturaController {

  private final ProfesorRepo profesorRepo;

  public JefaturaController(ProfesorRepo profesorRepo) {
    this.profesorRepo = profesorRepo;
  }

  @GetMapping("/profesores")
  public List<?> listarProfesores() {
    return profesorRepo.findAll();
  }
}
