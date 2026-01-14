package com.epn.dicc.ayudante.view;

import com.epn.dicc.ayudante.model.ContratoResponse;
import com.epn.dicc.ayudante.util.DateUtil;
import com.epn.dicc.ayudante.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para ver detalles de un contrato
 */
public class DetalleContratoDialog extends JDialog {
    
    private ContratoResponse contrato;
    
    public DetalleContratoDialog(Frame parent, ContratoResponse contrato) {
        super(parent, "Detalle del Contrato", true);
        this.contrato = contrato;
        initComponents();
    }
    
    private void initComponents() {
        setSize(600, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("Información del Contrato");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        titlePanel.add(lblTitle);
        
        // Información del contrato
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Número de contrato
        addInfoRow(infoPanel, gbc, row++, "Número de Contrato:", contrato.getNumeroContrato());
        
        // Proyecto
        addInfoRow(infoPanel, gbc, row++, "Proyecto:", contrato.getTituloProyecto());
        
        // Director
        addInfoRow(infoPanel, gbc, row++, "Director:", contrato.getNombreDirector());
        
        // Estado
        JLabel lblEstadoValue = new JLabel(contrato.getEstado());
        lblEstadoValue.setFont(lblEstadoValue.getFont().deriveFont(Font.BOLD));
        Color colorEstado = getColorEstado(contrato.getEstado());
        lblEstadoValue.setForeground(colorEstado);
        addInfoRow(infoPanel, gbc, row++, "Estado:", lblEstadoValue);
        
        // Fecha de solicitud
        addInfoRow(infoPanel, gbc, row++, "Fecha de Solicitud:", 
                DateUtil.formatearFechaHora(contrato.getFechaSolicitud()));
        
        // Fecha de aprobación
        if (contrato.getFechaAprobacion() != null) {
            addInfoRow(infoPanel, gbc, row++, "Fecha de Aprobación:", 
                    DateUtil.formatearFechaHora(contrato.getFechaAprobacion()));
        }
        
        // Fechas del contrato
        if (contrato.getFechaInicioContrato() != null) {
            addInfoRow(infoPanel, gbc, row++, "Fecha de Inicio:", 
                    DateUtil.formatearFecha(contrato.getFechaInicioContrato()));
            addInfoRow(infoPanel, gbc, row++, "Fecha de Fin:", 
                    DateUtil.formatearFecha(contrato.getFechaFinContrato()));
        }
        
        // Duración
        if (contrato.getMesesPactados() != null) {
            addInfoRow(infoPanel, gbc, row++, "Meses Pactados:", 
                    contrato.getMesesPactados() + " meses");
            addInfoRow(infoPanel, gbc, row++, "Meses Trabajados:", 
                    contrato.getMesesTrabajados() + " meses");
            
            if (contrato.getMesesRestantes() != null) {
                addInfoRow(infoPanel, gbc, row++, "Meses Restantes:", 
                        contrato.getMesesRestantes() + " meses");
            }
        }
        
        // Semestre
        if (contrato.getSemestreAsignado() != null) {
            addInfoRow(infoPanel, gbc, row++, "Semestre Asignado:", 
                    "Semestre " + contrato.getSemestreAsignado());
        }
        
        // Horas semanales
        if (contrato.getHorasSemanalesPactadas() != null) {
            addInfoRow(infoPanel, gbc, row++, "Horas Semanales:", 
                    contrato.getHorasSemanalesPactadas() + " horas");
        }
        
        // Remuneración
        if (contrato.getRemuneracionMensual() != null) {
            addInfoRow(infoPanel, gbc, row++, "Remuneración Mensual:", 
                    "$" + contrato.getRemuneracionMensual());
        }
        
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(null);
        
        // Botón cerrar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        buttonPanel.add(btnCerrar);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        SwingUtil.centrarVentana(this);
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(lblLabel.getFont().deriveFont(Font.BOLD));
        panel.add(lblLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(new JLabel(value != null ? value : "N/A"), gbc);
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(lblLabel.getFont().deriveFont(Font.BOLD));
        panel.add(lblLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(valueLabel, gbc);
    }
    
    private Color getColorEstado(String estado) {
        if (estado == null) return Color.BLACK;
        
        switch (estado) {
            case "ACTIVO":
                return new Color(0, 150, 0);
            case "PENDIENTE_APROBACION_DIRECTOR":
                return new Color(255, 140, 0);
            case "RECHAZADO":
            case "FINALIZADO_RENUNCIA":
                return new Color(200, 0, 0);
            case "FINALIZADO_NORMAL":
                return new Color(100, 100, 100);
            default:
                return Color.BLACK;
        }
    }
}