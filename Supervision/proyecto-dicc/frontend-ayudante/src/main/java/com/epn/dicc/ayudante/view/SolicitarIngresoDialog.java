package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.api.ApiClient;
import com.epn.dicc.ayudante.api.ContratoApi;
import com.epn.dicc.ayudante.api.ProyectoApi;
import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.model.ProyectoResponse;
import com.epn.dicc.ayudante.model.SolicitudIngresoRequest;
import com.epn.dicc.ayudante.util.SessionManager;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para solicitar ingreso a un proyecto
 */
public class SolicitarIngresoDialog extends JDialog {
    
    private JTextField txtCodigoProyecto;
    private JTextArea txtInformacionProyecto;
    private JTextArea txtComentarios;
    private JButton btnBuscar;
    private JButton btnEnviar;
    private JButton btnCancelar;
    
    private ProyectoApi proyectoApi;
    private ContratoApi contratoApi;
    private ProyectoResponse proyectoEncontrado;
    private boolean enviado = false;
    
    public SolicitarIngresoDialog(Frame parent) {
        super(parent, "Solicitar Ingreso a Proyecto", true);
        initApis();
        initComponents();
    }
    
    private void initApis() {
        ApiClient apiClient = SessionManager.getInstance().getApiClient();
        this.proyectoApi = new ProyectoApi(apiClient);
        this.contratoApi = new ContratoApi(apiClient);
    }
    
    private void initComponents() {
        setSize(500, 450);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de título
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Solicitar Ingreso a Proyecto");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 14f));
        titlePanel.add(lblTitle);
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Buscar Proyecto"));
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 5));
        searchInputPanel.add(new JLabel("Código del Proyecto:"), BorderLayout.WEST);
        txtCodigoProyecto = new JTextField();
        searchInputPanel.add(txtCodigoProyecto, BorderLayout.CENTER);
        
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarProyecto());
        searchInputPanel.add(btnBuscar, BorderLayout.EAST);
        
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        
        // Información del proyecto
        txtInformacionProyecto = new JTextArea(5, 40);
        txtInformacionProyecto.setEditable(false);
        txtInformacionProyecto.setLineWrap(true);
        txtInformacionProyecto.setWrapStyleWord(true);
        txtInformacionProyecto.setText("Ingrese el código del proyecto y presione 'Buscar'");
        JScrollPane scrollInfo = new JScrollPane(txtInformacionProyecto);
        searchPanel.add(scrollInfo, BorderLayout.CENTER);
        
        // Panel de comentarios
        JPanel commentPanel = new JPanel(new BorderLayout(5, 5));
        commentPanel.setBorder(BorderFactory.createTitledBorder("Comentarios (opcional)"));
        
        txtComentarios = new JTextArea(4, 40);
        txtComentarios.setLineWrap(true);
        txtComentarios.setWrapStyleWord(true);
        JScrollPane scrollComent = new JScrollPane(txtComentarios);
        commentPanel.add(scrollComent, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnEnviar = new JButton("Enviar Solicitud");
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(e -> enviarSolicitud());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnCancelar);
        
        // Enter en código para buscar
        txtCodigoProyecto.addActionListener(e -> buscarProyecto());
        
        // Agregar componentes
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(commentPanel, BorderLayout.CENTER);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
        
        SwingUtilities.invokeLater(() -> txtCodigoProyecto.requestFocus());
    }
    
    private void buscarProyecto() {
        String codigo = txtCodigoProyecto.getText().trim();
        
        if (codigo.isEmpty()) {
            SwingUtil.mostrarAdvertencia(this, "Ingrese un código de proyecto");
            return;
        }
        
        btnBuscar.setEnabled(false);
        
        SwingWorker<ProyectoResponse, Void> worker = new SwingWorker<ProyectoResponse, Void>() {
            @Override
            protected ProyectoResponse doInBackground() throws Exception {
                return proyectoApi.buscarPorCodigo(codigo);
            }
            
            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                
                try {
                    ProyectoResponse proyecto = get();
                    proyectoEncontrado = proyecto;
                    mostrarInformacionProyecto(proyecto);
                    btnEnviar.setEnabled(true);
                    
                } catch (Exception e) {
                    proyectoEncontrado = null;
                    btnEnviar.setEnabled(false);
                    String mensaje = e.getCause() != null ? 
                            e.getCause().getMessage() : e.getMessage();
                    txtInformacionProyecto.setText("Error: " + mensaje);
                    SwingUtil.mostrarError(SolicitarIngresoDialog.this, 
                            "No se pudo encontrar el proyecto: " + mensaje);
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarInformacionProyecto(ProyectoResponse proyecto) {
        StringBuilder info = new StringBuilder();
        info.append("PROYECTO ENCONTRADO\n\n");
        info.append("Código: ").append(proyecto.getCodigoProyecto()).append("\n");
        info.append("Título: ").append(proyecto.getTitulo()).append("\n");
        info.append("Director: ").append(proyecto.getNombreDirector()).append("\n");
        info.append("Tipo: ").append(proyecto.getTipoProyecto()).append("\n");
        info.append("Estado: ").append(proyecto.getEstado()).append("\n");
        info.append("Semestre actual: ").append(proyecto.getSemestreActual())
            .append(" de ").append(proyecto.getDuracionSemestres()).append("\n");
        info.append("\nDescripción:\n").append(proyecto.getDescripcion());
        
        txtInformacionProyecto.setText(info.toString());
        txtInformacionProyecto.setCaretPosition(0);
    }
    
    private void enviarSolicitud() {
        if (proyectoEncontrado == null) {
            SwingUtil.mostrarAdvertencia(this, "Primero busque un proyecto válido");
            return;
        }
        
        if (!SwingUtil.confirmar(this, 
                "¿Está seguro que desea solicitar ingreso al proyecto\n'" + 
                proyectoEncontrado.getTitulo() + "'?")) {
            return;
        }
        
        SolicitudIngresoRequest request = new SolicitudIngresoRequest();
        request.setCodigoProyecto(proyectoEncontrado.getCodigoProyecto());
        request.setComentarios(txtComentarios.getText().trim());
        
        btnEnviar.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        SwingWorker<ContratoResponse, Void> worker = new SwingWorker<ContratoResponse, Void>() {
            @Override
            protected ContratoResponse doInBackground() throws Exception {
                Long ayudanteId = SessionManager.getInstance().getUserId();
                return contratoApi.solicitarIngreso(ayudanteId, request);
            }
            
            @Override
            protected void done() {
                btnEnviar.setEnabled(true);
                btnCancelar.setEnabled(true);
                
                try {
                    ContratoResponse contrato = get();
                    
                    SwingUtil.mostrarInfo(SolicitarIngresoDialog.this, 
                            "¡Solicitud enviada exitosamente!\n\n" +
                            "Número de solicitud: " + contrato.getNumeroContrato() + "\n" +
                            "El director revisará tu solicitud y te notificará.");
                    
                    enviado = true;
                    dispose();
                    
                } catch (Exception e) {
                    String mensaje = e.getCause() != null ? 
                            e.getCause().getMessage() : e.getMessage();
                    SwingUtil.mostrarError(SolicitarIngresoDialog.this, 
                            "Error al enviar solicitud: " + mensaje);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isEnviado() {
        return enviado;
    }
}