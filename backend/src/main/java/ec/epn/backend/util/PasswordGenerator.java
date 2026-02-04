package ec.epn.backend.util;

import java.security.SecureRandom;

public class PasswordGenerator {

  private static final String MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMEROS = "0123456789";
  private static final String CARACTERES_ESPECIALES = "!@#$%&*";
  
  private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS + CARACTERES_ESPECIALES;
  private static final SecureRandom random = new SecureRandom();

  /**
   * Genera una contraseña aleatoria segura de longitud especificada.
   * Garantiza al menos: 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial
   */
  public static String generar(int longitud) {
    if (longitud < 8) {
      longitud = 8; // mínimo seguro
    }

    StringBuilder password = new StringBuilder(longitud);

    // Garantizar al menos uno de cada tipo
    password.append(MAYUSCULAS.charAt(random.nextInt(MAYUSCULAS.length())));
    password.append(MINUSCULAS.charAt(random.nextInt(MINUSCULAS.length())));
    password.append(NUMEROS.charAt(random.nextInt(NUMEROS.length())));
    password.append(CARACTERES_ESPECIALES.charAt(random.nextInt(CARACTERES_ESPECIALES.length())));

    // Rellenar el resto con caracteres aleatorios
    for (int i = 4; i < longitud; i++) {
      password.append(TODOS.charAt(random.nextInt(TODOS.length())));
    }

    // Mezclar los caracteres para que no estén en orden predecible
    return mezclar(password.toString());
  }

  /**
   * Genera una contraseña de longitud por defecto (12 caracteres)
   */
  public static String generar() {
    return generar(12);
  }

  private static String mezclar(String input) {
    char[] caracteres = input.toCharArray();
    for (int i = caracteres.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char temp = caracteres[i];
      caracteres[i] = caracteres[j];
      caracteres[j] = temp;
    }
    return new String(caracteres);
  }
}