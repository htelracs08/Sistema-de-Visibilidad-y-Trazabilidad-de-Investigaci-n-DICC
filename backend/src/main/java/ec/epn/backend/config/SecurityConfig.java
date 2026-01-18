package ec.epn.backend.config;

import ec.epn.backend.repository.UsuarioRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UsuarioRepo repo) {
    return username -> repo.findByCorreo(username)
      .map(u -> User.withUsername(u.correo())
          .password(u.passwordHash())
          .roles(u.rol()) // JEFATURA, DIRECTOR, AYUDANTE
          .build()
      )
      .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/health").permitAll()
        .requestMatchers("/actuator/**").permitAll()
        .anyRequest().authenticated()
      )
      .httpBasic(basic -> {});

    return http.build();
  }
}
