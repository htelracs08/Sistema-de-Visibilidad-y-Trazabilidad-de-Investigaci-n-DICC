package ec.epn.backend.controller;

import ec.epn.backend.repository.UsuarioRepo;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CommonController {

  private final UsuarioRepo usuarioRepo;

  public CommonController(UsuarioRepo usuarioRepo) {
    this.usuarioRepo = usuarioRepo;
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("ok", true);
  }

  @GetMapping("/me")
  public Map<String, Object> me(Principal principal) {
    if (principal == null) {
      return Map.of("ok", false, "msg", "No autenticado");
    }

    String correo = principal.getName();
    
    // Buscar el usuario en la BD para obtener el rol
    var usuario = usuarioRepo.findByCorreo(correo).orElse(null);
    
    if (usuario == null) {
      return Map.of("ok", false, "msg", "Usuario no encontrado");
    }

    return Map.of(
      "ok", true,
      "correo", usuario.correo(),
      "rol", usuario.rol(),
      "nombres", usuario.nombres(),
      "apellidos", usuario.apellidos(),
      "debeCambiarPassword", usuario.debeCambiarPassword()
    );
  }
}