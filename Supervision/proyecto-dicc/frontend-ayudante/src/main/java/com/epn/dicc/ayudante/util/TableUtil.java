package com.epn.dicc.ayudante.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Utilidades para tablas
 */
public class TableUtil {
    
    /**
     * Crea un modelo de tabla no editable
     */
    public static DefaultTableModel crearModeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    /**
     * Limpia todas las filas de una tabla
     */
    public static void limpiarTabla(JTable tabla) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
    }
    
    /**
     * Agrega una fila a una tabla
     */
    public static void agregarFila(DefaultTableModel model, Object... datos) {
        model.addRow(datos);
    }
    
    /**
     * Obtiene el valor seleccionado de una tabla
     */
    public static Object obtenerValorSeleccionado(JTable tabla, int columna) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return null;
        }
        return tabla.getValueAt(fila, columna);
    }
    
    /**
     * Verifica si hay una fila seleccionada
     */
    public static boolean hayFilaSeleccionada(JTable tabla) {
        return tabla.getSelectedRow() != -1;
    }
    
    /**
     * Obtiene el ID (primera columna) de la fila seleccionada
     */
    public static Long obtenerIdSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return null;
        }
        Object valor = tabla.getValueAt(fila, 0);
        if (valor instanceof Long) {
            return (Long) valor;
        }
        return null;
    }
    
    /**
     * Selecciona una fila por índice
     */
    public static void seleccionarFila(JTable tabla, int fila) {
        if (fila >= 0 && fila < tabla.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
            tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
        }
    }
    
    /**
     * Deselecciona todas las filas
     */
    public static void deseleccionarTodo(JTable tabla) {
        tabla.clearSelection();
    }
}