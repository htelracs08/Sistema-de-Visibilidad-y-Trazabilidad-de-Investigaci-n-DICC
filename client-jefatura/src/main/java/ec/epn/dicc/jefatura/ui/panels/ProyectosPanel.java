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

public class ProyectosPanel extends JPanel {

  // ✅ CORREGIDO: Tipos de proyecto actualizados
  private enum TipoProyecto {
    INVESTIGACION("Investigación"),
    VINCULACION("Vinculación"),
    TRANSFERENCIA_TECNOLOGICA("Transferencia Tecnológica");

    private final String label;

    TipoProyecto(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  // ✅ CORREGIDO: Subtipos de proyecto actualizados
  private enum SubtipoProyecto {
    INTERNO("Interno"),
    SEMILLA("Semilla"),
    GRUPAL("Grupal"),
    MULTIDISCIPLINARIO("Multidisciplinario");

    private final String label;

    SubtipoProyecto(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private final ApiClient api;

  private final DefaultTableModel model;
  private final JTable table;

  private final JLabel lblEstado = new JLabel(" ");
  private final JTextField txtBuscar = new JTextField();

  private List<JsonObject> cache = new ArrayList<>();

  public ProyectosPanel(ApiClient api) {
    this.api = api;
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(12, 12, 12, 12));

    // top bar
    JPanel top = new JPanel(new BorderLayout(12, 12));
    top.add(buildLeftActions(), BorderLayout.WEST);
    top.add(buildSearch(), BorderLayout.CENTER);
    top.add(buildRightActions(), BorderLayout.EAST);
    add(top, BorderLayout.NORTH);

    // table
    model = new DefaultTableModel(new Object[]{
        "ID", "Código", "Nombre", "Director", "Tipo", "Subtipo", "Activo", "Creado"
    }, 0) {
      @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    table = new JTable(model);
    table.setRowHeight(26);
    table.setAutoCreateRowSorter(true);

    // renderer: activo badge (ahora en columna 6)
    table.getColumnModel().getColumn(6).setCellRenderer(new ActivoRenderer());

    // hide ID column visually
    TableColumn idCol = table.getColumnModel().getColumn(0);
    idCol.setMinWidth(0);
    idCol.setMaxWidth(0);
    idCol.setPreferredWidth(0);

    add(new JScrollPane(table), BorderLayout.CENTER);

    // footer
    JPanel footer = new JPanel(new BorderLayout());
    lblEstado.setForeground(new Color(90, 90, 90));
    footer.add(lblEstado, BorderLayout.WEST);
    add(footer, BorderLayout.SOUTH);

    refresh();
  }

  private JPanel buildLeftActions() {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    JButton btnRefresh = new JButton("Refrescar");
    btnRefresh.addActionListener(e -> refresh());

    JButton btnDetalle = new JButton("Ver detalle");
    btnDetalle.addActionListener(e -> abrirDetalle());

    p.add(btnRefresh);
    p.add(btnDetalle);
    return p;
  }

  private JPanel buildSearch() {
    JPanel p = new JPanel(new BorderLayout(8, 0));
    JLabel lbl = new JLabel("Buscar (código/nombre/director):");
    p.add(lbl, BorderLayout.WEST);

    txtBuscar.getDocument().addDocumentListener(new SimpleDocumentListener(this::filtrar));
    p.add(txtBuscar, BorderLayout.CENTER);
    return p;
  }

  private JPanel buildRightActions() {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    JButton btnCrear = new JButton("Crear proyecto");
    btnCrear.setBackground(new Color(30, 90, 160));
    btnCrear.setForeground(Color.WHITE);
    btnCrear.setOpaque(true);
    btnCrear.addActionListener(e -> crearProyectoDialog());

    p.add(btnCrear);
    return p;
  }

  private void refresh() {
    lblEstado.setText("Cargando proyectos...");
    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err;
      List<JsonObject> rows = new ArrayList<>();

      @Override protected Void doInBackground() {
        try {
          String json = api.get(Endpoints.PROYECTOS_LIST);
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
          JOptionPane.showMessageDialog(ProyectosPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        cache = rows;
        render(rows);
        lblEstado.setText("OK (" + rows.size() + " proyectos)");
      }
    };
    w.execute();
  }

  private void render(List<JsonObject> rows) {
    model.setRowCount(0);
    for (JsonObject o : rows) {
      String id = getS(o, "id");
      String codigo = getS(o, "codigo");
      String nombre = getS(o, "nombre");
      String director = getS(o, "correoDirector");
      String tipo = getS(o, "tipo");
      String subtipo = getS(o, "subtipo");
      boolean activo = getB(o, "activo");
      String creado = getS(o, "creadoEn");
      
      // Si no hay tipo, mostrar guión
      if (tipo.isEmpty()) tipo = "-";
      if (subtipo.isEmpty()) subtipo = "-";
      
      model.addRow(new Object[]{id, codigo, nombre, director, tipo, subtipo, activo, creado});
    }
  }

  private void filtrar() {
    String q = txtBuscar.getText().trim().toLowerCase();
    if (q.isEmpty()) {
      render(cache);
      lblEstado.setText("OK (" + cache.size() + " proyectos)");
      return;
    }
    List<JsonObject> out = new ArrayList<>();
    for (JsonObject o : cache) {
      String codigo = getS(o, "codigo").toLowerCase();
      String nombre = getS(o, "nombre").toLowerCase();
      String director = getS(o, "correoDirector").toLowerCase();
      String tipo = getS(o, "tipo").toLowerCase();
      String subtipo = getS(o, "subtipo").toLowerCase();
      
      if (codigo.contains(q) || nombre.contains(q) || director.contains(q) || 
          tipo.contains(q) || subtipo.contains(q)) {
        out.add(o);
      }
    }
    render(out);
    lblEstado.setText("Filtrado: " + out.size());
  }

  private void crearProyectoDialog() {
    lblEstado.setText("Cargando profesores...");
    
    SwingWorker<List<DirectorOption>, Void> worker = new SwingWorker<>() {
      List<DirectorOption> profesores = new ArrayList<>();
      String error = null;
      
      @Override
      protected List<DirectorOption> doInBackground() {
        try {
          String json = api.get("/api/v1/jefatura/profesores");
          JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
          
          for (JsonElement el : arr) {
            JsonObject prof = el.getAsJsonObject();
            String correo = getS(prof, "correo");
            String nombres = getS(prof, "nombres");
            String apellidos = getS(prof, "apellidos");
            
            if (!correo.isEmpty()) {
              String display = (nombres + " " + apellidos).trim();
              profesores.add(new DirectorOption(display, correo));
            }
          }
        } catch (Exception e) {
          error = e.getMessage();
        }
        return profesores;
      }
      
      @Override
      protected void done() {
        if (error != null) {
          lblEstado.setText("Error al cargar profesores");
          JOptionPane.showMessageDialog(ProyectosPanel.this, 
              "Error al cargar profesores: " + error, 
              "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        
        if (profesores.isEmpty()) {
          lblEstado.setText("No hay profesores disponibles");
          JOptionPane.showMessageDialog(ProyectosPanel.this, 
              "No hay profesores disponibles en el sistema", 
              "Advertencia", JOptionPane.WARNING_MESSAGE);
          return;
        }
        
        lblEstado.setText("Profesores cargados");
        mostrarDialogoCreacion(profesores);
      }
    };
    
    worker.execute();
  }
  
  private void mostrarDialogoCreacion(List<DirectorOption> directores) {
    JTextField codigo = new JTextField();
    JTextField nombre = new JTextField();
    JComboBox<DirectorOption> comboDirector = new JComboBox<>(directores.toArray(new DirectorOption[0]));
    comboDirector.setEditable(false);

    JComboBox<TipoProyecto> comboTipo = new JComboBox<>(TipoProyecto.values());
    JComboBox<SubtipoProyecto> comboSubtipo = new JComboBox<>(SubtipoProyecto.values());
    comboTipo.setSelectedIndex(-1);
    comboSubtipo.setSelectedIndex(-1);
    comboSubtipo.setEnabled(false);

    // ✅ CORREGIDO: Habilitar subtipo solo si se elige INVESTIGACION
    comboTipo.addActionListener(e -> {
      Object sel = comboTipo.getSelectedItem();
      boolean habilitar = sel != null && sel == TipoProyecto.INVESTIGACION;
      if (!habilitar) {
        comboSubtipo.setSelectedIndex(-1);
        comboSubtipo.setEnabled(false);
      } else {
        comboSubtipo.setEnabled(true);
      }
    });

    JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
    p.add(new JLabel("Código (ej: PRJ-001):"));
    p.add(codigo);
    p.add(new JLabel("Nombre:"));
    p.add(nombre);
    p.add(new JLabel("Director (seleccione):"));
    p.add(comboDirector);
    p.add(new JLabel("Tipo de proyecto:"));
    p.add(comboTipo);
    p.add(new JLabel("Subtipo de proyecto (solo para Investigación):"));
    p.add(comboSubtipo);

    int ok = JOptionPane.showConfirmDialog(this, p, "Crear proyecto", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;
    
    String codigoText = codigo.getText().trim();
    String nombreText = nombre.getText().trim();
    DirectorOption seleccionado = (DirectorOption) comboDirector.getSelectedItem();
    String correoDirector = seleccionado != null ? seleccionado.correo() : null;
    TipoProyecto tipoSel = (TipoProyecto) comboTipo.getSelectedItem();
    SubtipoProyecto subtipoSel = (SubtipoProyecto) comboSubtipo.getSelectedItem();
    String tipoProyecto = tipoSel != null ? tipoSel.name() : null;
    String subtipoProyecto = subtipoSel != null ? subtipoSel.name() : null;
    if (tipoProyecto == null || !"INVESTIGACION".equalsIgnoreCase(tipoProyecto)) {
      subtipoProyecto = null;
    }
    
    if (codigoText.isEmpty() || nombreText.isEmpty() || correoDirector == null) {
      JOptionPane.showMessageDialog(this, 
          "Todos los campos son requeridos", 
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JsonObject payload = new JsonObject();
    payload.addProperty("codigo", codigoText);
    payload.addProperty("nombre", nombreText);
    payload.addProperty("correoDirector", correoDirector);
    if (tipoProyecto == null) payload.add("tipoProyecto", JsonNull.INSTANCE);
    else payload.addProperty("tipoProyecto", tipoProyecto);
    if (subtipoProyecto == null) payload.add("subtipoProyecto", JsonNull.INSTANCE);
    else payload.addProperty("subtipoProyecto", subtipoProyecto);

    String body = new Gson().toJson(payload);
    System.out.println("REQ BODY = " + body);

    lblEstado.setText("Creando proyecto...");
    SwingWorker<Void, Void> w = new SwingWorker<>() {
      String err;
      String resp;

      @Override protected Void doInBackground() {
        try {
          resp = api.postJson(Endpoints.PROYECTOS_CREAR, body);
        } catch (Exception e) {
          err = e.getMessage();
        }
        return null;
      }

      @Override protected void done() {
        if (err != null) {
          lblEstado.setText("Error");
          JOptionPane.showMessageDialog(ProyectosPanel.this, err, "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        JOptionPane.showMessageDialog(ProyectosPanel.this, resp, "Creado", JOptionPane.INFORMATION_MESSAGE);
        refresh();
      }
    };
    w.execute();
  }

  private void abrirDetalle() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Selecciona un proyecto", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    // convert row (if sorter)
    int modelRow = table.convertRowIndexToModel(row);
    String proyectoId = String.valueOf(model.getValueAt(modelRow, 0));
    new ProyectoDetalleDialog(SwingUtilities.getWindowAncestor(this), api, proyectoId).setVisible(true);
  }

  private static String getS(JsonObject o, String k) {
    return o != null && o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
  }
  private static boolean getB(JsonObject o, String k) {
    return o != null && o.has(k) && !o.get(k).isJsonNull() && o.get(k).getAsBoolean();
  }

  private record DirectorOption(String displayName, String correo) {
    @Override
    public String toString() {
      return displayName == null || displayName.isBlank() ? correo : displayName;
    }
  }

  // Renderer Activo badge
  static class ActivoRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      boolean activo = (value instanceof Boolean) && (Boolean) value;
      c.setText(activo ? "ACTIVO" : "INACTIVO");
      c.setHorizontalAlignment(SwingConstants.CENTER);
      if (!isSelected) {
        c.setForeground(Color.WHITE);
        c.setBackground(activo ? new Color(40, 140, 70) : new Color(140, 60, 60));
        c.setOpaque(true);
      }
      return c;
    }
  }
}