package ec.epn.backend.config;

import ec.epn.backend.repository.UsuarioRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

  @Bean
  UserDetailsService userDetailsService(UsuarioRepo repo) {
    return username -> repo.findByCorreo(username)
        .map(u -> User.withUsername(u.correo())
            .password("{noop}" + u.password()) // CLAVE
            .roles(u.rol())
            .build())
        .orElseThrow(
            () -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
  }

  // @Bean
  // SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
  // http
  // .csrf(csrf -> csrf.disable())
  // .authorizeHttpRequests(auth -> auth
  // .requestMatchers("/api/v1/health").permitAll()
  // .requestMatchers("/debug/**").permitAll()
  // .anyRequest().authenticated()
  // )
  // .httpBasic(basic -> {});

  // return http.build();
  // }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {
        }) // ✅ habilita CORS usando CorsConfigurationSource
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/health").permitAll()
            .requestMatchers("/debug/**").permitAll()
            .anyRequest().authenticated())
        .httpBasic(basic -> {
        });

    return http.build();
  }

}
