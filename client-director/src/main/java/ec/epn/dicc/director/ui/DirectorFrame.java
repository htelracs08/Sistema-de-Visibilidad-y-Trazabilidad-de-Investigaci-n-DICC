package ec.epn.dicc.director.ui;

import com.google.gson.*;
import ec.epn.dicc.director.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DirectorFrame extends JFrame {
  private final ApiClient api;

  // Tab Proyectos - TODAS LAS COLUMNAS
  private final DefaultTableModel proyectosModel = new DefaultTableModel(
      new Object[]{"proyectoId", "codigo", "nombre", "directorCorreo", "activo", 
                   "tipo", "subtipo", "fechaInicio", "fechaFin", "maxAyudantes", "maxArticulos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Ayudantes
  private final DefaultTableModel ayudantesModel = new DefaultTableModel(
      new Object[]{"contratoId", "ayudanteId", "correoInstitucional", "nombres", "apellidos", 
                   "estado", "fechaInicio", "fechaFin", "facultad", "quintil", "tipoAyudante"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Tab Bitácoras
  private final DefaultTableModel pendientesModel = new DefaultTableModel(
      new Object[]{"bitacoraId", "contratoId", "anio", "mes", "estado", 
                   "correoInstitucional", "nombres", "apellidos"}, 0
  ) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
  };

  // Selección actual
  private String proyectoIdSeleccionado = null;
  private String proyectoTipoSeleccionado = null;
  private String proyectoSubtipoSeleccionado = null;
  private String proyectoFechaInicioSeleccionada = null;
  private String proyectoFechaFinSeleccionada = null;

  public DirectorFrame(ApiClient api) {
    super("Director - Panel");
    this.api = api;

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1200, 700);
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
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Ajustar anchos de columnas
    table.getColumnModel().getColumn(0).setPreferredWidth(80);  // proyectoId
    table.getColumnModel().getColumn(1).setPreferredWidth(100); // codigo
    table.getColumnModel().getColumn(2).setPreferredWidth(200); // nombre
    table.getColumnModel().getColumn(3).setPreferredWidth(180); // directorCorreo
    table.getColumnModel().getColumn(4).setPreferredWidth(60);  // activo
    table.getColumnModel().getColumn(5).setPreferredWidth(120); // tipo
    table.getColumnModel().getColumn(6).setPreferredWidth(120); // subtipo
    table.getColumnModel().getColumn(7).setPreferredWidth(100); // fechaInicio
    table.getColumnModel().getColumn(8).setPreferredWidth(100); // fechaFin
    table.getColumnModel().getColumn(9).setPreferredWidth(100); // maxAyudantes
    table.getColumnModel().getColumn(10).setPreferredWidth(100); // maxArticulos
    
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int row = table.getSelectedRow();
        if (row >= 0) {
          proyectoIdSeleccionado = String.valueOf(proyectosModel.getValueAt(row, 0));
          Object tipoSel = proyectosModel.getValueAt(row, 5);
          Object subtipoSel = proyectosModel.getValueAt(row, 6);
          Object fechaIniSel = proyectosModel.getValueAt(row, 7);
          Object fechaFinSel = proyectosModel.getValueAt(row, 8);
          proyectoTipoSeleccionado = tipoSel != null ? String.valueOf(tipoSel) : null;
          proyectoSubtipoSeleccionado = subtipoSel != null ? String.valueOf(subtipoSel) : null;
          proyectoFechaInicioSeleccionada = fechaIniSel != null ? String.valueOf(fechaIniSel) : null;
          proyectoFechaFinSeleccionada = fechaFinSel != null ? String.valueOf(fechaFinSel) : null;
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
          o.has("activo") && o.get("activo").getAsBoolean() ? "SÍ" : "NO",
          s(o, "tipo"),
          s(o, "subtipo"),
          s(o, "fechaInicio"),
          s(o, "fechaFin"),
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

    JDateChooser fIni = new JDateChooser();
    JDateChooser fFin = new JDateChooser();
    fIni.setDateFormatString("yyyy-MM-dd");
    fFin.setDateFormatString("yyyy-MM-dd");
    ((JTextFieldDateEditor) fIni.getDateEditor()).setEditable(false);
    ((JTextFieldDateEditor) fFin.getDateEditor()).setEditable(false);
    JCheckBox sinFechaIni = new JCheckBox("Sin fecha");
    JCheckBox sinFechaFin = new JCheckBox("Sin fecha");
    JComboBox<String> comboTipo = new JComboBox<>(new String[]{"INVESTIGACION", "VINCULACION", "DOCENCIA"});
    JComboBox<String> comboSubtipo = new JComboBox<>(new String[]{"INTERNO", "EXPERIMENTAL", "APLICADA"});
    JTextField maxAyu = new JTextField();
    JTextField maxArt = new JTextField();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date parsedIni = parseDateOrNull(dateFormat, proyectoFechaInicioSeleccionada);
    Date parsedFin = parseDateOrNull(dateFormat, proyectoFechaFinSeleccionada);

    if (parsedIni != null) {
      fIni.setDate(parsedIni);
      sinFechaIni.setSelected(false);
      fIni.setEnabled(true);
    } else {
      sinFechaIni.setSelected(true);
      fIni.setEnabled(false);
      fIni.setDate(null);
    }

    if (parsedFin != null) {
      fFin.setDate(parsedFin);
      sinFechaFin.setSelected(false);
      fFin.setEnabled(true);
    } else {
      sinFechaFin.setSelected(true);
      fFin.setEnabled(false);
      fFin.setDate(null);
    }

    sinFechaIni.addActionListener(e -> {
      boolean enabled = !sinFechaIni.isSelected();
      fIni.setEnabled(enabled);
      if (!enabled) fIni.setDate(null);
    });
    sinFechaFin.addActionListener(e -> {
      boolean enabled = !sinFechaFin.isSelected();
      fFin.setEnabled(enabled);
      if (!enabled) fFin.setDate(null);
    });

    comboTipo.setSelectedIndex(-1);
    comboSubtipo.setSelectedIndex(-1);
    comboSubtipo.setEnabled(false);

    if (proyectoTipoSeleccionado != null && !proyectoTipoSeleccionado.isEmpty()) {
      comboTipo.setSelectedItem(proyectoTipoSeleccionado);
    }
    if (proyectoSubtipoSeleccionado != null && !proyectoSubtipoSeleccionado.isEmpty()) {
      comboSubtipo.setSelectedItem(proyectoSubtipoSeleccionado);
    }

    String tipoInicial = (String) comboTipo.getSelectedItem();
    boolean habilitarInicial = "INVESTIGACION".equalsIgnoreCase(tipoInicial);
    comboSubtipo.setEnabled(habilitarInicial);
    if (!habilitarInicial) {
      comboSubtipo.setSelectedIndex(-1);
    }

    comboTipo.addActionListener(e -> {
      String sel = (String) comboTipo.getSelectedItem();
      boolean habilitar = sel != null && "INVESTIGACION".equalsIgnoreCase(sel);
      if (!habilitar) {
        comboSubtipo.setSelectedIndex(-1);
        comboSubtipo.setEnabled(false);
      } else {
        comboSubtipo.setEnabled(true);
      }
    });

    JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
    JPanel fechaIniPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    fechaIniPanel.add(fIni);
    fechaIniPanel.add(sinFechaIni);
    JPanel fechaFinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    fechaFinPanel.add(fFin);
    fechaFinPanel.add(sinFechaFin);

    form.add(new JLabel("fechaInicio (YYYY-MM-DD)")); form.add(fechaIniPanel);
    form.add(new JLabel("fechaFin (YYYY-MM-DD)")); form.add(fechaFinPanel);
    form.add(new JLabel("tipo")); form.add(comboTipo);
    form.add(new JLabel("subtipo (o vacío)")); form.add(comboSubtipo);
    form.add(new JLabel("maxAyudantes")); form.add(maxAyu);
    form.add(new JLabel("maxArticulos")); form.add(maxArt);

    int ok = JOptionPane.showConfirmDialog(this, form, "Actualizar Proyecto", JOptionPane.OK_CANCEL_OPTION);
    if (ok != JOptionPane.OK_OPTION) return;

    // Validaciones
    String tipoSel = (String) comboTipo.getSelectedItem();
    if (tipoSel == null || tipoSel.trim().isEmpty()) {
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
    Date fechaIniSel = fIni.getDate();
    Date fechaFinSel = fFin.getDate();
    if (sinFechaIni.isSelected() || fechaIniSel == null) body.add("fechaInicio", JsonNull.INSTANCE);
    else body.addProperty("fechaInicio", dateFormat.format(fechaIniSel));
    if (sinFechaFin.isSelected() || fechaFinSel == null) body.add("fechaFin", JsonNull.INSTANCE);
    else body.addProperty("fechaFin", dateFormat.format(fechaFinSel));
    body.addProperty("tipo", tipoSel.trim());
    String subtipoSel = (String) comboSubtipo.getSelectedItem();
    if (tipoSel == null || !"INVESTIGACION".equalsIgnoreCase(tipoSel)) {
      body.add("subtipo", JsonNull.INSTANCE);
    } else if (subtipoSel == null || subtipoSel.trim().isEmpty()) {
      body.add("subtipo", JsonNull.INSTANCE);
    } else {
      body.addProperty("subtipo", subtipoSel.trim());
    }
    body.addProperty("maxAyudantes", Integer.parseInt(maxAyu.getText().trim()));
    body.addProperty("maxArticulos", Integer.parseInt(maxArt.getText().trim()));

    JsonObject resp = api.putJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado, body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement dataEl = resp.get("data");
      String msg = "Error al actualizar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
        if (dataObj.has("msg")) {
          msg += ": " + dataObj.get("msg").getAsString();
        }
      }
      showError(msg);
      return;
    }

    JOptionPane.showMessageDialog(this, "Proyecto actualizado correctamente.");
    cargarProyectos(); // RECARGAR para ver los cambios
  }

  private Date parseDateOrNull(SimpleDateFormat fmt, String value) {
    if (value == null) return null;
    String v = value.trim();
    if (v.isEmpty() || "-".equals(v) || "null".equalsIgnoreCase(v)) return null;
    try {
      return fmt.parse(v);
    } catch (ParseException e) {
      return null;
    }
  }

  // =========================
  // TAB: AYUDANTES - CORREGIDO
  // =========================
  private JPanel buildTabAyudantes() {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new EmptyBorder(12, 12, 12, 12));

    JTable table = new JTable(ayudantesModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Ajustar anchos
    table.getColumnModel().getColumn(0).setPreferredWidth(80);  // contratoId
    table.getColumnModel().getColumn(1).setPreferredWidth(80);  // ayudanteId
    table.getColumnModel().getColumn(2).setPreferredWidth(180); // correo
    table.getColumnModel().getColumn(3).setPreferredWidth(100); // nombres
    table.getColumnModel().getColumn(4).setPreferredWidth(100); // apellidos
    table.getColumnModel().getColumn(5).setPreferredWidth(80);  // estado
    table.getColumnModel().getColumn(6).setPreferredWidth(100); // fechaInicio
    table.getColumnModel().getColumn(7).setPreferredWidth(100); // fechaFin
    table.getColumnModel().getColumn(8).setPreferredWidth(60);  // facultad
    table.getColumnModel().getColumn(9).setPreferredWidth(60);  // quintil
    table.getColumnModel().getColumn(10).setPreferredWidth(150); // tipoAyudante

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
    
    // ✅ FIX: El backend retorna directamente un array, no {ok: true, items: [...]}
    if (data == null) {
      showError("Respuesta vacía del servidor.");
      return;
    }

    // El data puede ser un array directamente
    JsonArray arr;
    if (data.isJsonArray()) {
      arr = data.getAsJsonArray();
    } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
      arr = data.getAsJsonObject().getAsJsonArray("items");
    } else {
      showError("Formato de respuesta inesperado al listar ayudantes.");
      System.out.println("DEBUG - Response data: " + data);
      return;
    }

    for (JsonElement el : arr) {
      JsonObject o = el.getAsJsonObject();
      ayudantesModel.addRow(new Object[]{
          s(o, "contratoId"),
          s(o, "ayudanteId"),
          s(o, "correoInstitucional"),
          s(o, "nombres"),
          s(o, "apellidos"),
          s(o, "estado"),
          s(o, "fechaInicio"),
          s(o, "fechaFin"),
          s(o, "facultad"),
          s(o, "quintil"),
          s(o, "tipoAyudante")
      });
    }
    
    JOptionPane.showMessageDialog(this, 
        "Se cargaron " + arr.size() + " contratos/ayudantes.", 
        "Éxito", JOptionPane.INFORMATION_MESSAGE);
  }

  private void registrarAyudante() {
    if (proyectoIdSeleccionado == null || proyectoIdSeleccionado.isEmpty()) {
      showError("Selecciona un proyecto primero (pestaña Proyectos).");
      return;
    }

    JTextField nombres = new JTextField();
    JTextField apellidos = new JTextField();
    JTextField correo = new JTextField();
    JTextField facultad = new JTextField();
    JComboBox<Integer> quintil = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    JComboBox<String> tipoAyudante = new JComboBox<>(new String[]{
      "ASISTENTE_INVESTIGACION",
      "AYUDANTE_INVESTIGACION",
      "TECNICO_INVESTIGACION"
    });
    JDateChooser fci = new JDateChooser();
    JDateChooser fcf = new JDateChooser();
    fci.setDateFormatString("yyyy-MM-dd");
    fcf.setDateFormatString("yyyy-MM-dd");
    ((JTextFieldDateEditor) fci.getDateEditor()).setEditable(false);
    ((JTextFieldDateEditor) fcf.getDateEditor()).setEditable(false);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    quintil.setSelectedIndex(-1);
    tipoAyudante.setSelectedIndex(-1);
    fci.setDate(null);
    fcf.setDate(null);

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
        quintil.getSelectedItem() == null || tipoAyudante.getSelectedItem() == null ||
        fci.getDate() == null || fcf.getDate() == null) {
      showError("Todos los campos son requeridos");
      return;
    }

    int quintilValue;
    try {
      quintilValue = (Integer) quintil.getSelectedItem();
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
    body.addProperty("tipoAyudante", ((String) tipoAyudante.getSelectedItem()).trim());
    body.addProperty("fechaInicioContrato", dateFormat.format(fci.getDate()));
    body.addProperty("fechaFinContrato", dateFormat.format(fcf.getDate()));

    JsonObject resp = api.postJson("/api/v1/director/proyectos/" + proyectoIdSeleccionado + "/ayudantes", body);
    int code = resp.get("_httpStatus").getAsInt();
    
    if (code != 200) {
      JsonElement dataEl = resp.get("data");
      String msg = "Error al registrar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
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
  // TAB: BITÁCORAS - CORREGIDO
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
    
    // ✅ FIX: Similar al anterior, el backend puede retornar directamente un array
    if (data == null) {
      showError("Respuesta vacía del servidor.");
      return;
    }

    JsonArray arr;
    if (data.isJsonArray()) {
      arr = data.getAsJsonArray();
    } else if (data.isJsonObject() && data.getAsJsonObject().has("items")) {
      arr = data.getAsJsonObject().getAsJsonArray("items");
    } else {
      showError("Formato de respuesta inesperado al listar bitácoras.");
      System.out.println("DEBUG - Response data: " + data);
      return;
    }

    for (JsonElement el : arr) {
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
    
    if (arr.size() == 0) {
      JOptionPane.showMessageDialog(this, 
          "No hay bitácoras pendientes para este proyecto.", 
          "Info", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(this, 
          "Se encontraron " + arr.size() + " bitácoras pendientes.", 
          "Éxito", JOptionPane.INFORMATION_MESSAGE);
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

    JsonElement dataEl = resp.get("data");
    if (dataEl == null || !dataEl.isJsonObject()) {
      showError("Respuesta inválida al ver bitácora.");
      return;
    }

    showBitacoraDialog(bitacoraId, dataEl.getAsJsonObject());
  }

  private void showBitacoraDialog(String bitacoraId, JsonObject data) {
    JDialog dialog = new JDialog(this, "Bitácora " + bitacoraId, true);
    dialog.setLayout(new BorderLayout(12, 12));
    dialog.setSize(980, 600);
    dialog.setLocationRelativeTo(this);

    DefaultTableModel model = new DefaultTableModel(new Object[]{
        "Semana", "Actividades Semana", "Observaciones", "Anexos",
        "Actividad", "Inicio", "Salida", "Horas"
    }, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(24);

    TableColumn col0 = table.getColumnModel().getColumn(0);
    col0.setPreferredWidth(140);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    table.getColumnModel().getColumn(2).setPreferredWidth(180);
    table.getColumnModel().getColumn(3).setPreferredWidth(120);
    table.getColumnModel().getColumn(4).setPreferredWidth(220);
    table.getColumnModel().getColumn(5).setPreferredWidth(70);
    table.getColumnModel().getColumn(6).setPreferredWidth(70);
    table.getColumnModel().getColumn(7).setPreferredWidth(60);

    List<String[]> pdfRows = new ArrayList<>();

    JsonArray semanas = data.has("semanas") && data.get("semanas").isJsonArray()
        ? data.getAsJsonArray("semanas")
        : new JsonArray();

    for (JsonElement se : semanas) {
      if (!se.isJsonObject()) continue;
      JsonObject semana = se.getAsJsonObject();

      String semanaLabel = (s(semana, "fechaInicioSemana") + " - " + s(semana, "fechaFinSemana")).trim();
      String actSemana = s(semana, "actividadesRealizadas");
      String obs = s(semana, "observaciones");
      String anexos = s(semana, "anexos");

      JsonArray actividades = semana.has("actividades") && semana.get("actividades").isJsonArray()
          ? semana.getAsJsonArray("actividades")
          : new JsonArray();

      if (actividades.size() == 0) {
        String[] row = new String[]{semanaLabel, actSemana, obs, anexos, "", "", "", ""};
        model.addRow(row);
        pdfRows.add(row);
        continue;
      }

      for (JsonElement ae : actividades) {
        if (!ae.isJsonObject()) continue;
        JsonObject act = ae.getAsJsonObject();

        String desc = s(act, "descripcion");
        String hIni = s(act, "horaInicio");
        String hFin = s(act, "horaSalida");
        String horas = s(act, "totalHoras");

        String[] row = new String[]{semanaLabel, actSemana, obs, anexos, desc, hIni, hFin, horas};
        model.addRow(row);
        pdfRows.add(row);
      }
    }

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JsonObject bitacora = data.has("bitacora") && data.get("bitacora").isJsonObject()
      ? data.getAsJsonObject("bitacora")
      : null;

    JButton btnPdf = new JButton("Descargar PDF");
    JButton btnCerrar = new JButton("Cerrar");

    btnPdf.addActionListener(e -> exportBitacoraPdf(bitacoraId, pdfRows, bitacora));
    btnCerrar.addActionListener(e -> dialog.dispose());

    footer.add(btnPdf);
    footer.add(btnCerrar);

    dialog.add(new JScrollPane(table), BorderLayout.CENTER);
    dialog.add(footer, BorderLayout.SOUTH);
    dialog.setVisible(true);
  }

  private void exportBitacoraPdf(String bitacoraId, List<String[]> rows, JsonObject bitacora) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Guardar Bitácora como PDF");
    chooser.setSelectedFile(new File("bitacora_" + bitacoraId + ".pdf"));

    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File file = chooser.getSelectedFile();

    try {
      generateBitacoraPdf(file, bitacoraId, rows, bitacora);
      JOptionPane.showMessageDialog(this, "PDF generado correctamente.");
    } catch (IOException ex) {
      showError("No pude generar el PDF: " + ex.getMessage());
    }
  }

  private void generateBitacoraPdf(File file, String bitacoraId, List<String[]> rows, JsonObject bitacora) throws IOException {
    try (PDDocument doc = new PDDocument()) {
      PDRectangle base = PDRectangle.LETTER;
      PDRectangle pageSize = new PDRectangle(base.getHeight(), base.getWidth());
      PDPage page = new PDPage(pageSize);
      doc.addPage(page);

      float margin = 36f;
      float yStart = pageSize.getHeight() - margin;
      float y = yStart;
      float fontSize = 9f;
      float headerFontSize = 12f;
      float leading = 14f;
      float tableLeading = 12f;
      float tablePaddingX = 3f;
      float tablePaddingY = 4f;

      DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate minIni = null;
      LocalDate maxFin = null;
      for (String[] row : rows) {
        if (row == null || row.length == 0) continue;
        String semana = safe(row[0]);
        String[] parts = semana.split("\\s*-\\s*");
        if (parts.length == 2) {
          try {
            LocalDate ini = LocalDate.parse(parts[0].trim(), df);
            LocalDate fin = LocalDate.parse(parts[1].trim(), df);
            if (minIni == null || ini.isBefore(minIni)) minIni = ini;
            if (maxFin == null || fin.isAfter(maxFin)) maxFin = fin;
          } catch (Exception ignored) {
          }
        }
      }

      String estado = bitacora != null ? s(bitacora, "estado") : "";
      String fechaCreacion = bitacora != null ? s(bitacora, "creadoEn") : "";
      String ayudanteNombre = "";
      String ayudanteCorreo = "";
      if (bitacora != null) {
        String nom = s(bitacora, "ayudanteNombres");
        String ape = s(bitacora, "ayudanteApellidos");
        ayudanteNombre = (nom + " " + ape).trim();
        ayudanteCorreo = s(bitacora, "correoInstitucional");
      }
      String periodo = "-";
      if (bitacora != null && bitacora.has("anio") && bitacora.has("mes")
          && !bitacora.get("anio").isJsonNull() && !bitacora.get("mes").isJsonNull()) {
        try {
          int anio = bitacora.get("anio").getAsInt();
          int mes = bitacora.get("mes").getAsInt();
          if (anio > 0 && mes > 0) periodo = String.format("%02d/%d", mes, anio);
        } catch (Exception ignored) {
        }
      } else if (minIni != null && maxFin != null) {
        periodo = df.format(minIni) + " - " + df.format(maxFin);
      }
      if (estado.isBlank()) estado = "-";
      if (fechaCreacion.isBlank()) fechaCreacion = "-";

      PDPageContentStream cs = new PDPageContentStream(doc, page);

      String titulo = "BITÁCORA MENSUAL";
      float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(titulo) / 1000f * headerFontSize;
      float titleX = (pageSize.getWidth() - titleWidth) / 2f;
      cs.beginText();
      cs.setFont(PDType1Font.HELVETICA_BOLD, headerFontSize);
      cs.newLineAtOffset(titleX, y);
      cs.showText(titulo);
      cs.endText();
      y -= leading;

      cs.setFont(PDType1Font.HELVETICA, fontSize);
      cs.beginText();
      cs.newLineAtOffset(margin, y);
      cs.showText("ID: " + bitacoraId);
      cs.endText();
      y -= leading;

      cs.beginText();
      cs.newLineAtOffset(margin, y);
      cs.showText("Periodo: " + periodo);
      cs.endText();
      y -= leading;

      cs.beginText();
      cs.newLineAtOffset(margin, y);
      cs.showText("Estado: " + estado + "   |   Fecha de creación: " + fechaCreacion);
      cs.endText();
      y -= leading;

      cs.beginText();
      cs.newLineAtOffset(margin, y);
      cs.showText("Ayudante: " + (ayudanteNombre.isBlank() ? "-" : ayudanteNombre));
      cs.endText();
      y -= leading;

      cs.beginText();
      cs.newLineAtOffset(margin, y);
      cs.showText("Correo: " + (ayudanteCorreo.isBlank() ? "-" : ayudanteCorreo));
      cs.endText();
      y -= leading;

      cs.moveTo(margin, y);
      cs.lineTo(pageSize.getWidth() - margin, y);
      cs.stroke();
      y -= leading;

      String[] header = new String[]{"Semana", "Act.Semana", "Obs", "Anexos", "Actividad", "Ini", "Fin", "Hrs"};
      float tableWidth = pageSize.getWidth() - 2 * margin;
      float[] colWidths = new float[]{
          tableWidth * 0.16f,
          tableWidth * 0.18f,
          tableWidth * 0.16f,
          tableWidth * 0.10f,
          tableWidth * 0.24f,
          tableWidth * 0.06f,
          tableWidth * 0.06f,
          tableWidth * 0.04f
      };

      int[] wrapWidths = new int[colWidths.length];
      for (int i = 0; i < colWidths.length; i++) {
        wrapWidths[i] = Math.max(4, (int) Math.floor((colWidths[i] - tablePaddingX * 2) / (fontSize * 0.55f)));
      }

      float headerHeight = tableLeading + tablePaddingY * 2;
      if (y - headerHeight <= margin) {
        cs.close();
        page = new PDPage(pageSize);
        doc.addPage(page);
        cs = new PDPageContentStream(doc, page);
        y = yStart;
      }

      float x = margin;
      cs.setLineWidth(0.8f);
      cs.moveTo(margin, y);
      cs.lineTo(margin + tableWidth, y);
      cs.stroke();
      cs.moveTo(margin, y - headerHeight);
      cs.lineTo(margin + tableWidth, y - headerHeight);
      cs.stroke();
      for (int i = 0; i <= colWidths.length; i++) {
        cs.moveTo(x, y);
        cs.lineTo(x, y - headerHeight);
        cs.stroke();
        if (i < colWidths.length) x += colWidths[i];
      }

      float textY = y - tablePaddingY - tableLeading + 3;
      x = margin;
      cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
      for (int i = 0; i < header.length; i++) {
        cs.beginText();
        cs.newLineAtOffset(x + tablePaddingX, textY);
        cs.showText(header[i]);
        cs.endText();
        x += colWidths[i];
      }

      y -= headerHeight;

      cs.setFont(PDType1Font.HELVETICA, fontSize);
      for (String[] row : rows) {
        List<String> wrapped = wrapRow(row, wrapWidths);
        int maxLines = wrapped.size() == 0 ? 1 : wrapped.size();
        float rowHeight = maxLines * tableLeading + tablePaddingY * 2;

        if (y - rowHeight <= margin) {
          cs.close();
          page = new PDPage(pageSize);
          doc.addPage(page);
          cs = new PDPageContentStream(doc, page);
          y = yStart;

          cs.setLineWidth(0.8f);
          cs.moveTo(margin, y);
          cs.lineTo(margin + tableWidth, y);
          cs.stroke();
          cs.moveTo(margin, y - headerHeight);
          cs.lineTo(margin + tableWidth, y - headerHeight);
          cs.stroke();
          x = margin;
          for (int i = 0; i <= colWidths.length; i++) {
            cs.moveTo(x, y);
            cs.lineTo(x, y - headerHeight);
            cs.stroke();
            if (i < colWidths.length) x += colWidths[i];
          }

          textY = y - tablePaddingY - tableLeading + 3;
          x = margin;
          cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
          for (int i = 0; i < header.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x + tablePaddingX, textY);
            cs.showText(header[i]);
            cs.endText();
            x += colWidths[i];
          }
          cs.setFont(PDType1Font.HELVETICA, fontSize);
          y -= headerHeight;
        }

        cs.setLineWidth(0.6f);
        cs.moveTo(margin, y);
        cs.lineTo(margin + tableWidth, y);
        cs.stroke();
        cs.moveTo(margin, y - rowHeight);
        cs.lineTo(margin + tableWidth, y - rowHeight);
        cs.stroke();
        x = margin;
        for (int i = 0; i <= colWidths.length; i++) {
          cs.moveTo(x, y);
          cs.lineTo(x, y - rowHeight);
          cs.stroke();
          if (i < colWidths.length) x += colWidths[i];
        }

        List<List<String>> wrappedCols = new ArrayList<>();
        int maxLineCount = 1;
        for (int i = 0; i < wrapWidths.length; i++) {
          String cell = i < row.length ? row[i] : "";
          List<String> parts = wrapCell(cell, wrapWidths[i]);
          wrappedCols.add(parts);
          if (parts.size() > maxLineCount) maxLineCount = parts.size();
        }

        float baseY = y - tablePaddingY - tableLeading + 3;
        for (int line = 0; line < maxLineCount; line++) {
          x = margin;
          for (int i = 0; i < wrappedCols.size(); i++) {
            String text = line < wrappedCols.get(i).size() ? wrappedCols.get(i).get(line) : "";
            cs.beginText();
            cs.newLineAtOffset(x + tablePaddingX, baseY - line * tableLeading);
            cs.showText(text);
            cs.endText();
            x += colWidths[i];
          }
        }

        y -= rowHeight;
      }

      cs.close();

      doc.save(file);
    }
  }

  private List<String> wrapRow(String[] row, int[] widths) {
    List<List<String>> wrapped = new ArrayList<>();
    int maxLines = 1;
    for (int i = 0; i < widths.length; i++) {
      String text = i < row.length ? safe(row[i]) : "";
      List<String> parts = wrapCell(text, widths[i]);
      wrapped.add(parts);
      if (parts.size() > maxLines) maxLines = parts.size();
    }

    List<String> lines = new ArrayList<>();
    for (int line = 0; line < maxLines; line++) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < widths.length; i++) {
        String part = line < wrapped.get(i).size() ? wrapped.get(i).get(line) : "";
        sb.append(padRight(part, widths[i]));
        if (i < widths.length - 1) sb.append(" | ");
      }
      lines.add(sb.toString());
    }
    return lines;
  }

  private List<String> wrapCell(String text, int width) {
    List<String> out = new ArrayList<>();
    String t = safe(text);
    if (t.isEmpty()) {
      out.add("");
      return out;
    }

    String[] words = t.split("\\s+");
    StringBuilder line = new StringBuilder();
    for (String word : words) {
      if (word.length() > width) {
        if (line.length() > 0) {
          out.add(line.toString());
          line.setLength(0);
        }
        int i = 0;
        while (i < word.length()) {
          int end = Math.min(i + width, word.length());
          out.add(word.substring(i, end));
          i = end;
        }
        continue;
      }

      if (line.length() == 0) {
        line.append(word);
      } else if (line.length() + 1 + word.length() <= width) {
        line.append(' ').append(word);
      } else {
        out.add(line.toString());
        line.setLength(0);
        line.append(word);
      }
    }

    if (line.length() > 0) out.add(line.toString());
    return out;
  }

  private String formatLine(String[] cells, int[] widths) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < widths.length; i++) {
      String cell = i < cells.length ? safe(cells[i]) : "";
      sb.append(padRight(cell, widths[i]));
      if (i < widths.length - 1) sb.append(" | ");
    }
    return sb.toString();
  }

  private String padRight(String text, int width) {
    String t = safe(text);
    if (t.length() >= width) return t.substring(0, width);
    return t + " ".repeat(width - t.length());
  }

  private String repeat(String s, int count) {
    return s.repeat(Math.max(0, count));
  }

  private int sum(int[] arr) {
    int total = 0;
    for (int v : arr) total += v;
    return total;
  }

  private String safe(String v) {
    return v == null ? "" : v.replace("\n", " ").replace("\r", " ").trim();
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
      JsonElement dataEl = resp.get("data");
      String msg = "No pude revisar. HTTP " + code;
      if (dataEl != null && dataEl.isJsonObject()) {
        JsonObject dataObj = dataEl.getAsJsonObject();
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