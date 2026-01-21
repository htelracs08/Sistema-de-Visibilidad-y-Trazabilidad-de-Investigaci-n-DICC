package ec.epn.jefatura.ui.panels;

import com.google.gson.*;
import ec.epn.jefatura.api.ApiClient;
import ec.epn.jefatura.api.Endpoints;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EstadisticasPanel extends JPanel {

  private final ApiClient api;

  private final DefaultTableModel modelProy;
  private final DefaultTableModel modelAyud;
  private final JLabel lblEstado = new JLabel(" ");

  public EstadisticasPanel(ApiClient api) {
    this.api = api;
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel top = new JPanel(new BorderLayout());
    JButton btnRefresh = new JButton("Refrescar");
    btnRefresh.addActionListener(e -> load());
    top.add(btnRefresh, BorderLayout.WEST);
    add(top, BorderLayout.NORTH);

    JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));

    // proyectos por tipo y activo
    modelProy = new DefaultTableModel(new Object[]{"Tipo", "Activo", "Total"}, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable tblProy = new JTable(modelProy);
    tblProy.setRowHeight(26);
    center.add(wrap("Proyectos por tipo/estado", tblProy));

    // ayudantes activos total + por tipo
    modelAyud = new DefaultTableModel(new Object[]{"TipoAyudante", "Activos"}, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable tblAyud = new JTable(modelAyud);
    tblAyud.setRowHeight(26);
    center.add(wrap("Ayudantes activos por tipo", tblAyud));

    add(center, BorderLayout.CENTER);

    lblEstado.setForeground(new Color(90, 90, 90));
    add(lblEstado, BorderLayout.SOUTH);

    load();
  }

  private JPanel wrap(String title, JTable table) {
    JPanel p = new JPanel(new BorderLayout());
    JLabel t = new JLabel(title);
    t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
    p.add(t, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);
    p.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    return p;
  }

  private void load() {
    lblEstado.setText("Cargando estadísticas...");
    modelProy.setRowCount(0);
    modelAyud.setRowCount(0);

    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err;
      JsonArray proy;
      JsonObject ayudStats;

      @Override protected Void doInBackground() {
        try {
          proy = JsonParser.parseString(api.get(Endpoints.PROYECTOS_ESTADISTICAS)).getAsJsonArray();
          ayudStats = JsonParser.parseString(api.get(Endpoints.AYUDANTES_ESTADISTICAS)).getAsJsonObject();
        } catch (Exception e) {
          err = e.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          lblEstado.setText("Error: " + err);
          JOptionPane.showMessageDialog(EstadisticasPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // Proyectos
        for (JsonElement el : proy) {
          JsonObject o = el.getAsJsonObject();
          String tipo = s(o, "tipo");
          boolean activo = o.has("activo") && o.get("activo").getAsBoolean();
          int total = o.has("total") ? o.get("total").getAsInt() : 0;
          modelProy.addRow(new Object[]{tipo, activo ? "ACTIVO" : "INACTIVO", total});
        }

        // Ayudantes
        int activosTotal = ayudStats.has("activosTotal") ? ayudStats.get("activosTotal").getAsInt() : 0;
        JsonArray porTipo = ayudStats.has("porTipo") ? ayudStats.get("porTipo").getAsJsonArray() : new JsonArray();

        // fila total
        modelAyud.addRow(new Object[]{"TOTAL", activosTotal});
        for (JsonElement el : porTipo) {
          JsonObject o = el.getAsJsonObject();
          modelAyud.addRow(new Object[]{s(o, "tipoAyudante"), i(o, "activos")});
        }

        lblEstado.setText("OK");
      }
    };
    w.execute();
  }

  private static String s(JsonObject o, String k) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
  }

  private static int i(JsonObject o, String k) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
  }
}
