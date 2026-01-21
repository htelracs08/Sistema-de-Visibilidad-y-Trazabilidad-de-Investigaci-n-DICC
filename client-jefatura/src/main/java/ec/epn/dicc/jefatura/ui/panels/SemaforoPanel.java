package ec.epn.dicc.jefatura.ui.panels;

import com.google.gson.*;

import ec.epn.dicc.jefatura.api.ApiClient;
import ec.epn.dicc.jefatura.api.Endpoints;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SemaforoPanel extends JPanel {

  private final ApiClient api;

  private final DefaultTableModel model;
  private final JTable table;
  private final JLabel lblEstado = new JLabel(" ");

  private final JComboBox<String> cmbColor = new JComboBox<>(new String[]{"TODOS", "VERDE", "AMARILLO", "ROJO"});

  private List<JsonObject> cache = new ArrayList<>();

  public SemaforoPanel(ApiClient api) {
    this.api = api;
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel top = new JPanel(new BorderLayout(8, 8));
    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JButton btnRefresh = new JButton("Refrescar");
    btnRefresh.addActionListener(e -> refresh());
    left.add(btnRefresh);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    right.add(new JLabel("Filtrar:"));
    cmbColor.addActionListener(e -> filtrar());
    right.add(cmbColor);

    top.add(left, BorderLayout.WEST);
    top.add(right, BorderLayout.EAST);

    add(top, BorderLayout.NORTH);

    model = new DefaultTableModel(new Object[]{
        "ContratoId", "Proyecto", "Ayudante", "InicioContrato",
        "MesesEsperados", "Aprobados", "Faltantes", "Color"
    }, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    table = new JTable(model);
    table.setRowHeight(26);

    // color renderer
    table.getColumnModel().getColumn(7).setCellRenderer(new ColorRenderer());

    add(new JScrollPane(table), BorderLayout.CENTER);

    lblEstado.setForeground(new Color(90, 90, 90));
    add(lblEstado, BorderLayout.SOUTH);

    refresh();
  }

  private void refresh() {
    lblEstado.setText("Cargando semáforo...");
    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err;
      List<JsonObject> rows = new ArrayList<>();

      @Override protected Void doInBackground() {
        try {
          String json = api.get(Endpoints.SEMAFORO);
          JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
          for (JsonElement el : arr) rows.add(el.getAsJsonObject());
        } catch (Exception e) {
          err = e.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          lblEstado.setText("Error: " + err);
          JOptionPane.showMessageDialog(SemaforoPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        cache = rows;
        filtrar();
        lblEstado.setText("OK (" + rows.size() + ")");
      }
    };
    w.execute();
  }

  private void filtrar() {
    String filtro = String.valueOf(cmbColor.getSelectedItem());
    model.setRowCount(0);

    int shown = 0;
    for (JsonObject o : cache) {
      String color = s(o, "color");
      if (!"TODOS".equalsIgnoreCase(filtro) && !filtro.equalsIgnoreCase(color)) continue;

      String proyecto = (s(o, "proyectoCodigo") + " - " + s(o, "proyectoNombre")).trim();
      String ayudante = (s(o, "nombres") + " " + s(o, "apellidos")).trim();

      model.addRow(new Object[]{
          s(o, "contratoId"),
          proyecto,
          ayudante,
          s(o, "fechaInicio"),
          i(o, "mesesEsperados"),
          i(o, "mesesAprobados"),
          i(o, "faltantes"),
          color
      });
      shown++;
    }

    lblEstado.setText("Mostrando: " + shown + " / " + cache.size());
  }

  private static String s(JsonObject o, String k) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
  }
  private static int i(JsonObject o, String k) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
  }

  static class ColorRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      String v = value == null ? "" : value.toString();
      c.setText(v);
      c.setHorizontalAlignment(SwingConstants.CENTER);

      if (!isSelected) {
        c.setForeground(Color.WHITE);
        c.setOpaque(true);
        switch (v.toUpperCase()) {
          case "VERDE" -> c.setBackground(new Color(40, 140, 70));
          case "AMARILLO" -> {
            c.setBackground(new Color(230, 170, 20));
            c.setForeground(Color.BLACK);
          }
          case "ROJO" -> c.setBackground(new Color(180, 60, 60));
          default -> c.setBackground(new Color(150, 150, 150));
        }
      }
      return c;
    }
  }
}
