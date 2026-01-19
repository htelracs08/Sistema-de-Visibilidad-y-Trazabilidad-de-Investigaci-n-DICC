package ec.epn.backend.controller;

import ec.epn.backend.repository.UsuarioRepo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {
  private final UsuarioRepo repo;

  public DebugController(UsuarioRepo repo) {
    this.repo = repo;
  }

  @GetMapping("/usuario")
  public Map<String, Object> usuario(@RequestParam String correo) {
    var u = repo.findByCorreo(correo).orElse(null);
    if (u == null) return Map.of("found", false);
    return Map.of(
        "found", true,
        "correo", u.correo(),
        "password", u.password(),
        "rol", u.rol(),
        "debeCambiarPassword", u.debeCambiarPassword()
    );
  }
}
