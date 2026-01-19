package ec.epn.backend.controller;

import ec.epn.backend.repository.UsuarioRepo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UsuarioRepo usuarioRepo;

  public AuthController(UsuarioRepo usuarioRepo) {
    this.usuarioRepo = usuarioRepo;
  }

  public record CambiarPasswordReq(String nuevaPassword) {}

  @PostMapping("/cambiar-password")
  public Map<String, Object> cambiarPassword(Principal principal, @RequestBody CambiarPasswordReq req) {
    if (req.nuevaPassword() == null || req.nuevaPassword().isBlank()) {
      return Map.of("ok", false, "msg", "nuevaPassword es requerida");
    }

    String correo = principal.getName();
    usuarioRepo.cambiarPassword(correo, req.nuevaPassword().trim());
    return Map.of("ok", true);
  }
}
