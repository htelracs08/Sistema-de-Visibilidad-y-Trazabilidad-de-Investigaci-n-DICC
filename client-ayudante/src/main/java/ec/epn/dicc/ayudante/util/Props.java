package ec.epn.dicc.ayudante.util;

import java.io.InputStream;
import java.util.Properties;

public class Props {
  private static final Properties P = new Properties();

  static {
    try (InputStream in = Props.class.getClassLoader().getResourceAsStream("client.properties")) {
      if (in != null) P.load(in);
    } catch (Exception ignored) {}
  }

  public static String get(String key, String def) {
    return P.getProperty(key, def);
  }
}
