package ec.epn.jefatura.ui.panels;

import com.google.gson.*;
import ec.epn.jefatura.api.ApiClient;
import ec.epn.jefatura.api.Endpoints;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProyectoDetalleDialog extends JDialog {

  private final ApiClient api;
  private final String proyectoId;

  private final DefaultTableModel model;
  private final JLabel lblEstado = new JLabel(" ");

  public ProyectoDetalleDialog(Window owner, ApiClient api, String proyectoId) {
    super(owner, "Detalle Proyecto", ModalityType.APPLICATION_MODAL);
    this.api = api;
    this.proyectoId = proyectoId;

    setSize(980, 520);
    setLocationRelativeTo(owner);

    JPanel root = new JPanel(new BorderLayout());
    root.setBorder(new EmptyBorder(12, 12, 12, 12));

    JPanel top = new JPanel(new BorderLayout(8, 8));
    JLabel title = new JLabel("Proyecto ID: " + proyectoId);
    title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
    JButton btnRefresh = new JButton("Refrescar");
    btnRefresh.addActionListener(e -> load());

    top.add(title, BorderLayout.WEST);
    top.add(btnRefresh, BorderLayout.EAST);

    root.add(top, BorderLayout.NORTH);

    model = new DefaultTableModel(new Object[]{
        "ContratoId", "Estado", "MotivoInactivo",
        "Ayudante", "Correo", "TipoAyudante",
        "Inicio", "Fin"
    }, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    JTable table = new JTable(model);
    table.setRowHeight(26);
    root.add(new JScrollPane(table), BorderLayout.CENTER);

    lblEstado.setForeground(new Color(90, 90, 90));
    root.add(lblEstado, BorderLayout.SOUTH);

    setContentPane(root);
    load();
  }

  private void load() {
    lblEstado.setText("Cargando contratos/ayudantes...");
    model.setRowCount(0);

    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err;
      JsonArray arr;

      @Override protected Void doInBackground() {
        try {
          String path = String.format(Endpoints.PROYECTOS_AYUDANTES, proyectoId);
          String json = api.get(path);
          arr = JsonParser.parseString(json).getAsJsonArray();
        } catch (Exception e) {
          err = e.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          lblEstado.setText("Error: " + err);
          JOptionPane.showMessageDialog(ProyectoDetalleDialog.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        for (JsonElement el : arr) {
          JsonObject o = el.getAsJsonObject();
          model.addRow(new Object[]{
              s(o, "contratoId"),
              s(o, "estado"),
              s(o, "motivoInactivo"),
              (s(o, "nombres") + " " + s(o, "apellidos")).trim(),
              s(o, "correoInstitucional"),
              s(o, "tipoAyudante"),
              s(o, "fechaInicio"),
              s(o, "fechaFin")
          });
        }

        lblEstado.setText("OK (" + arr.size() + ")");
      }
    };
    w.execute();
  }

  private static String s(JsonObject o, String k) {
    return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
  }
}
