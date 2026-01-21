package ec.epn.dicc.director.ui;

import com.google.gson.*;
import ec.epn.dicc.director.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DirectorFrame extends JFrame {
  private final ApiClient api;

  // Tab Proyectos
  private final DefaultTableModel proyectosModel = new DefaultTableModel(
      new Object[]{"proyectoId", "codigo", "nombre", "directorCorreo", "tipo", "subtipo", "maxAyudantes", "maxArticulos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Ayudantes
  private final DefaultTableModel ayudantesModel = new DefaultTableModel(
      new Object[]{"contratoId", "ayudanteId", "correoInstitucional", "nombres", "apellidos", "estado", "fechaInicio", "fechaFin"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Bitácoras
  private final DefaultTableModel pendientesModel = new DefaultTableModel(
      new Object[]{"bitacoraId", "contratoId", "anio", "mes", "estado", "correoInstitucional", "nombres", "apellidos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Selección actual
  private String proyectoIdSeleccionado = null;

  public DirectorFrame(ApiClient api) {
    super("Director - Panel");
    this.api = api;

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1100, 650);
    setLocationRelativeTo(null);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Proyectos", buildTabProyectos());
    tabs.addTab("Ayudantes", buildTabAyudantes());
    tabs.addTab("Bitácoras", buildTabBitacoras());

    setContentPane(tabs);

    // carga inicial
    cargarProyectos();
  }

  // =========================
  // TAB: PROYECTOS
  // =========================
  private JPanel buildTabProyectos() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(proyectosModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int row = table.getSelectedRow();
        if (row >= 0) {
          proyectoIdSeleccionado = String.valueOf(proyectosModel.getValueAt(row, 0));
        }
      }
    });

    JButton btnRef = new JButton("Refrescar");
    btnRef.addActionListener(e -> cargarProyectos());

    JButton btnUpdate = new JButton("Actualizar Detalles");
    btnUpdate.addActionListener(e -> actualizarDetallesProyecto());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnRef);
    top.add(btnUpdate);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JLabel hint = new JLabel("Selecciona un proyecto y luego 'Actualizar Detalles' (tipo/subtipo/max...).");
    hint.setBorder(new EmptyBorder(8, 0, 0, 0));
    p.add(hint, BorderLayout.SOUTH);

    return p;
  }

  private void cargarProyectos() {
    proyectosModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/mis-proyectos");
    int httpStatus = resp.get("_httpStatus").getAsInt();
    
    if (httpStatus != 200) {
      showError("No pude cargar proyectos. HTTP " + httpStatus);
      return;
    }

    JsonElement data = resp.get("data");
    if (data == null || !data.isJsonArray()) {
      showError("Respuesta inesperada al listar proyectos.");
      return;
    }

    JsonArray arr = data.getAsJsonArray();
    for (JsonElement el : arr) {
      JsonObject o = el.getAsJsonObject();
      proyectosModel.addRow(new Object[]{
          s(o, "id"),
          s(o, "codigo"),
          s(o, "nombre"),
          s(o, "directorCorreo"),
          s(o, "tipo"),
          s(o, "subtipo"),
          s(o, "maxAyudantes"),
          s(o, "maxArticulos")
      });
    }
  }

  private void actualizarDetallesProyecto() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero.");
      return;
    }

    JTextField fIni = new JTextField("2026-01-01");
    JTextField fFin = new JTextField("2026-12-31");
    JTextField tipo = new JTextField("INVESTIGACION");
    JTextField subtipo = new JTextField("INTERNO");
    JTextField maxAyu = new JTextField("2");
    JTextField maxArt = new JTextField("3");

    JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
    form.add(new JLabel("fechaInicio (YYYY-MM-DD)")); form.add(fIni);
    form.add(new JLabel("fechaFin (YYYY-MM-DD)")); form.add(fFin);
    form.add(new JLabel("tipo")); form.add(tipo);
    form.add(new JLabel("subtipo (o vacío)")); form.add(subtipo);
    form.add(new JLabel("maxAyudantes")); form.add(maxAyu);
    form.add(new JLabel("maxArticulos")); form.add(maxArt);

    int ok = JOptionPane.showConfirmDialog(this, form, "Actualizar Proyecto", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;

    // Validaciones
    if (fIni.getText().trim().isEmpty() || fFin.getText().trim().isEmpty()) {
      showError("Las fechas son requeridas");
      return;
    }
    
    if (tipo.getText().trim().isEmpty()) {
      showError("El tipo es requerido");
      return;
    }

    try {
      Integer.parseInt(maxAyu.getText().trim());
      Integer.parseInt(maxArt.getText().trim());
    } catch (NumberFormatException e) {
      showError("maxAyudantes y maxArticulos deben ser números válidos");
      return;
    }

    JsonObject body = new JsonObject();
    body.addProperty("fechaInicio", fIni.getText().trim());
    body.addProperty("fechaFin", fFin.getText().trim());
    body.addProperty("tipo", tipo.getText().trim());
    body.addProperty("subtipo", subtipo.getText().trim());
    body.addProperty("maxAyudantes", Integer.parseInt(maxAyu.getText().trim()));
    body.addProperty("maxArticulos", Integer.parseInt(maxArt.getText().trim()));

    JsonObject resp = api.putJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado, body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement data = resp.get("data");
      String msg = "Error al actualizar. HTTP " + code;
      if (data != null && data.isJsonObject()) {
        JsonObject dataObj = data.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Proyecto actualizado correctamente.");
    cargarProyectos();
  }

  // =========================
  // TAB: AYUDANTES
  // =========================
  private JPanel buildTabAyudantes() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(ayudantesModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JButton btnList = new JButton("Listar del Proyecto Seleccionado");
    btnList.addActionListener(e -> listarAyudantes());

    JButton btnReg = new JButton("Registrar Ayudante");
    btnReg.addActionListener(e -> registrarAyudante());

    JButton btnFin = new JButton("Finalizar Contrato");
    btnFin.addActionListener(e -> finalizarContrato(table));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnList);
    top.add(btnReg);
    top.add(btnFin);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JLabel hint = new JLabel("Necesitas seleccionar un proyecto en la pestaña Proyectos.");
    hint.setBorder(new EmptyBorder(8, 0, 0, 0));
    p.add(hint, BorderLayout.SOUTH);

    return p;
  }

  private void listarAyudantes() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    ayudantesModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/ayudantes");
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude listar ayudantes. HTTP " + code);
      return;
    }

    JsonElement data = resp.get("data");
    if (data == null || !data.isJsonArray()) {
      showError("Respuesta inesperada al listar ayudantes.");
      return;
    }

    for (JsonElement el : data.getAsJsonArray()) {
      JsonObject o = el.getAsJsonObject();
      ayudantesModel.addRow(new Object[]{
          s(o, "contratoId"),
          s(o, "ayudanteId"),
          s(o, "correoInstitucional"),
          s(o, "nombres"),
          s(o, "apellidos"),
          s(o, "estado"),
          s(o, "fechaInicio"),
          s(o, "fechaFin")
      });
    }
  }

  private void registrarAyudante() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    JTextField nombres = new JTextField();
    JTextField apellidos = new JTextField();
    JTextField correo = new JTextField();
    JTextField facultad = new JTextField("FIS");
    JTextField quintil = new JTextField("2");
    JTextField tipoAyudante = new JTextField("AYUDANTE_INVESTIGACION");
    JTextField fci = new JTextField("2026-01-01");
    JTextField fcf = new JTextField("2026-03-31");

    JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
    form.add(new JLabel("Nombres *")); form.add(nombres);
    form.add(new JLabel("Apellidos *")); form.add(apellidos);
    form.add(new JLabel("Correo Institucional *")); form.add(correo);
    form.add(new JLabel("Facultad *")); form.add(facultad);
    form.add(new JLabel("Quintil (1-5) *")); form.add(quintil);
    form.add(new JLabel("Tipo Ayudante *")); form.add(tipoAyudante);
    form.add(new JLabel("Fecha Inicio Contrato *")); form.add(fci);
    form.add(new JLabel("Fecha Fin Contrato *")); form.add(fcf);

    int ok = JOptionPane.showConfirmDialog(this, form, "Registrar Ayudante", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;

    // Validaciones
    if (nombres.getText().trim().isEmpty() || apellidos.getText().trim().isEmpty() ||
        correo.getText().trim().isEmpty() || facultad.getText().trim().isEmpty() ||
        quintil.getText().trim().isEmpty() || tipoAyudante.getText().trim().isEmpty() ||
        fci.getText().trim().isEmpty() || fcf.getText().trim().isEmpty()) {
      showError("Todos los campos son requeridos");
      return;
    }

    int quintilValue;
    try {
      quintilValue = Integer.parseInt(quintil.getText().trim());
      if (quintilValue < 1 || quintilValue > 5) {
        showError("El quintil debe estar entre 1 y 5");
        return;
      }
    } catch (NumberFormatException e) {
      showError("El quintil debe ser un número válido");
      return;
    }

    JsonObject body = new JsonObject();
    body.addProperty("nombres", nombres.getText().trim());
    body.addProperty("apellidos", apellidos.getText().trim());
    body.addProperty("correoInstitucional", correo.getText().trim().toLowerCase());
    body.addProperty("facultad", facultad.getText().trim());
    body.addProperty("quintil", quintilValue);
    body.addProperty("tipoAyudante", tipoAyudante.getText().trim());
    body.addProperty("fechaInicioContrato", fci.getText().trim());
    body.addProperty("fechaFinContrato", fcf.getText().trim());

    JsonObject resp = api.postJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/ayudantes", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement data = resp.get("data");
      String msg = "Error al registrar. HTTP " + code;
      if (data != null && data.isJsonObject()) {
        JsonObject dataObj = data.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Ayudante registrado correctamente.");
    listarAyudantes();
  }

  private void finalizarContrato(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona un contrato en la tabla.");
      return;
    }

    String contratoId = String.valueOf(ayudantesModel.getValueAt(row, 0));

    String[] opts = {"RENUNCIA", "FIN_CONTRATO", "DESPIDO"};
    String motivo = (String) JOptionPane.showInputDialog(
        this, "Selecciona el motivo:", "Finalizar Contrato", 
        JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]
    );
    if (motivo == null) return;

    JsonObject body = new JsonObject();
    body.addProperty("motivo", motivo);

    JsonObject resp = api.postJson("/api/v1/director/contratos/" + contratoId + "/finalizar", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("Error al finalizar. HTTP " + code);
      return;
    }

    JOptionPane.showMessageDialog(this, "Contrato finalizado correctamente.");
    listarAyudantes();
  }

  // =========================
  // TAB: BITÁCORAS
  // =========================
  private JPanel buildTabBitacoras() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(pendientesModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JButton btnPend = new JButton("Pendientes del Proyecto Seleccionado");
    btnPend.addActionListener(e -> listarPendientes());

    JButton btnVer = new JButton("Ver Bitácora");
    btnVer.addActionListener(e -> verBitacora(table));

    JButton btnRev = new JButton("Revisar (Aprobar/Rechazar)");
    btnRev.addActionListener(e -> revisarBitacora(table));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(btnPend);
    top.add(btnVer);
    top.add(btnRev);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    return p;
  }

  private void listarPendientes() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    pendientesModel.setRowCount(0);

    JsonObject resp = api.get("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/bitacoras/pendientes");
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude listar pendientes. HTTP " + code);
      return;
    }

    JsonElement data = resp.get("data");
    if (data == null || !data.isJsonArray()) {
      showError("Respuesta inesperada al listar pendientes.");
      return;
    }

    for (JsonElement el : data.getAsJsonArray()) {
      JsonObject o = el.getAsJsonObject();
      pendientesModel.addRow(new Object[]{
          s(o, "bitacoraId"),
          s(o, "contratoId"),
          s(o, "anio"),
          s(o, "mes"),
          s(o, "estado"),
          s(o, "correoInstitucional"),
          s(o, "nombres"),
          s(o, "apellidos")
      });
    }
    
    if (pendientesModel.getRowCount() == 0) {
      JOptionPane.showMessageDialog(this, 
          "No hay bitácoras pendientes para este proyecto.", 
          "Info", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void verBitacora(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona una bitácora.");
      return;
    }
    String bitacoraId = String.valueOf(pendientesModel.getValueAt(row, 0));

    JsonObject resp = api.get("/api/v1/director/bitacoras/" + bitacoraId);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      showError("No pude ver bitácora. HTTP " + code);
      return;
    }

    // Formatear la respuesta de manera legible
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String formatted = gson.toJson(resp.get("data"));
    
    JTextArea textArea = new JTextArea(formatted);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(700, 500));
    
    JOptionPane.showMessageDialog(this,
        scrollPane,
        "Bitácora " + bitacoraId,
        JOptionPane.INFORMATION_MESSAGE
    );
  }

  private void revisarBitacora(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      showError("Selecciona una bitácora.");
      return;
    }

    String bitacoraId = String.valueOf(pendientesModel.getValueAt(row, 0));

    String[] opts = {"APROBAR", "RECHAZAR"};
    String decision = (String) JOptionPane.showInputDialog(
        this, "Decisión:", "Revisar Bitácora", 
        JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]
    );
    if (decision == null) return;

    String obs = JOptionPane.showInputDialog(this, "Observación (opcional):", "");
    if (obs == null) obs = "";

    JsonObject body = new JsonObject();
    body.addProperty("decision", decision);
    body.addProperty("observacion", obs);

    JsonObject resp = api.postJson("/api/v1/director/bitacoras/" + bitacoraId + "/revisar", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement data = resp.get("data");
      String msg = "No pude revisar. HTTP " + code;
      if (data != null && data.isJsonObject()) {
        JsonObject dataObj = data.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Revisión realizada correctamente.");
    listarPendientes();
  }

  // =========================
  // Helpers UI
  // =========================
  private void showError(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private String s(JsonObject o, String key) {
    if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "";
    JsonElement el = o.get(key);
    if (el.isJsonPrimitive()) {
      return el.getAsString();
    }
    return el.toString();
  }
}