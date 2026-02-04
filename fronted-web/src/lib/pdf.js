import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

/**
 * 📄 EXPORTACIÓN PDF PROFESIONAL - Sistema DICC
 * Compatible con: Aprobadas, Rechazadas, Borradores
 * 
 * CORRECIONES:
 * - Eliminados caracteres especiales problemáticos (emojis)
 * - Formateo de horas con 2 decimales
 */
export function exportBitacoraPdf({ bitacoraId, bitacora, semanas, estudiante }) {
  const doc = new jsPDF({
    orientation: "landscape",
    unit: "pt",
    format: "letter"
  });

  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const margin = 40;

  let yPos = margin;

  // ========== TÍTULO ==========
  doc.setFont("helvetica", "bold");
  doc.setFontSize(20);
  doc.setTextColor(11, 42, 74);
  doc.text("BITÁCORA MENSUAL DE ACTIVIDADES", pageWidth / 2, yPos, { align: "center" });
  yPos += 25;

  doc.setFontSize(12);
  doc.setTextColor(80, 80, 80);
  doc.text("Escuela Politécnica Nacional", pageWidth / 2, yPos, { align: "center" });
  yPos += 15;
  doc.setFontSize(10);
  doc.text("Departamento de Investigación y Control de Calidad", pageWidth / 2, yPos, { align: "center" });
  yPos += 20;

  // Línea divisoria
  doc.setDrawColor(230, 57, 70);
  doc.setLineWidth(3);
  doc.line(margin, yPos, pageWidth - margin, yPos);
  yPos += 30;

  // ========== INFORMACIÓN DEL ESTUDIANTE Y BITÁCORA ==========
  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);
  doc.setTextColor(0, 0, 0);

  const infoData = [];
  if (estudiante && (estudiante.nombres || estudiante.apellidos)) {
    infoData.push(
      ["Estudiante:", `${estudiante.nombres || ''} ${estudiante.apellidos || ''}`.trim()],
      ["Correo:", estudiante.correoInstitucional || '-'],
      ["Facultad:", estudiante.facultad || '-']
    );
  }

  if (bitacora) {
    infoData.push(
      ["Bitácora ID:", bitacoraId || '-'],
      ["Periodo:", `${bitacora.anio || ''}-${String(bitacora.mes || '').padStart(2, '0')}`],
      ["Estado:", bitacora.estado || '-']
    );
  }

  if (infoData.length > 0) {
    autoTable(doc, {
      startY: yPos,
      body: infoData,
      theme: 'plain',
      styles: { fontSize: 10, cellPadding: 6 },
      columnStyles: {
        0: { fontStyle: 'bold', cellWidth: 140, textColor: [11, 42, 74] },
        1: { cellWidth: 'auto' }
      },
      margin: { left: margin, right: margin }
    });
    yPos = doc.lastAutoTable.finalY + 25;
  }

  // ========== TABLA DE ACTIVIDADES ==========
  const tableData = [];
  
  for (const semana of semanas || []) {
    const acts = Array.isArray(semana.actividades) ? semana.actividades : [];
    
    // Encabezado de semana (SIN emojis)
    tableData.push([
      {
        content: `SEMANA: ${semana.fechaInicioSemana || ''} - ${semana.fechaFinSemana || ''}`,
        colSpan: 7,
        styles: { 
          fillColor: [11, 42, 74],
          textColor: [255, 255, 255],
          fontStyle: 'bold',
          halign: 'left',
          fontSize: 10
        }
      }
    ]);

    // Actividades realizadas
    tableData.push([
      {
        content: `Actividades: ${semana.actividadesRealizadas || '-'}`,
        colSpan: 7,
        styles: { fillColor: [240, 240, 240], fontStyle: 'italic', halign: 'left', fontSize: 9 }
      }
    ]);

    // Observaciones y anexos (SIN emojis)
    if (semana.observaciones || (semana.anexos && semana.anexos !== '-')) {
      const extras = [];
      if (semana.observaciones) extras.push(`Observaciones: ${semana.observaciones}`);
      if (semana.anexos && semana.anexos !== '-') extras.push(`Anexos: ${semana.anexos}`);
      
      tableData.push([
        {
          content: extras.join(' | '),
          colSpan: 7,
          styles: { fillColor: [255, 250, 230], fontSize: 8, textColor: [100, 100, 100], halign: 'left' }
        }
      ]);
    }

    // Actividades detalladas
    if (acts.length === 0) {
      tableData.push([
        { 
          content: '(Sin actividades detalladas)', 
          colSpan: 7, 
          styles: { halign: 'center', textColor: [150, 150, 150], fontStyle: 'italic', fontSize: 9 } 
        }
      ]);
    } else {
      for (const act of acts) {
        // ✅ FORMATEAR HORAS CON 2 DECIMALES
        const horasFormateadas = typeof act.totalHoras === 'number' 
          ? act.totalHoras.toFixed(2) 
          : (act.totalHoras || '-');

        tableData.push([
          { content: act.actividadId || '-', styles: { fontSize: 7, halign: 'center' } },
          { content: act.horaInicio || '-', styles: { halign: 'center' } },
          { content: act.horaSalida || '-', styles: { halign: 'center' } },
          { content: horasFormateadas, styles: { halign: 'center', fontStyle: 'bold' } },
          { content: act.descripcion || '-', styles: { cellWidth: 'auto' } },
          '',
          ''
        ]);
      }
    }

    // Separador entre semanas
    tableData.push([
      { content: '', colSpan: 7, styles: { fillColor: [255, 255, 255], minCellHeight: 8 } }
    ]);
  }

  // Generar tabla
  autoTable(doc, {
    startY: yPos,
    head: [[
      { content: 'ID', styles: { halign: 'center' } },
      { content: 'Inicio', styles: { halign: 'center' } },
      { content: 'Fin', styles: { halign: 'center' } },
      { content: 'Hrs', styles: { halign: 'center' } },
      { content: 'Descripción', styles: { halign: 'center' } },
      { content: 'V B', styles: { halign: 'center' } },
      { content: 'Obs', styles: { halign: 'center' } }
    ]],
    body: tableData,
    theme: 'grid',
    styles: { fontSize: 9, cellPadding: 5, lineColor: [180, 180, 180], lineWidth: 0.5 },
    headStyles: {
      fillColor: [230, 57, 70],
      textColor: [255, 255, 255],
      fontStyle: 'bold',
      halign: 'center',
      fontSize: 9
    },
    columnStyles: {
      0: { cellWidth: 60, halign: 'center' },
      1: { cellWidth: 60, halign: 'center' },
      2: { cellWidth: 60, halign: 'center' },
      3: { cellWidth: 50, halign: 'center' },
      4: { cellWidth: 'auto', halign: 'left' },
      5: { cellWidth: 50, halign: 'center' },
      6: { cellWidth: 80, halign: 'left' }
    },
    margin: { left: margin, right: margin, bottom: 80 },
    didDrawPage: function(data) {
      const footerY = pageHeight - 30;
      doc.setFontSize(8);
      doc.setTextColor(100, 100, 100);
      doc.setFont("helvetica", "normal");
      
      doc.text(`Página ${data.pageNumber}`, margin, footerY);
      doc.text(`Bitácora: ${bitacoraId || '-'}`, pageWidth / 2, footerY, { align: 'center' });
      doc.text(`Generado: ${new Date().toLocaleDateString('es-ES')}`, pageWidth - margin, footerY, { align: 'right' });
    },
    didDrawCell: function(data) {
      // Este callback se ejecuta después de dibujar cada celda
      // Nos permite saber cuándo termina la tabla
    }
  });

  // ========== FIRMAS (SIEMPRE AL FINAL DE LA ÚLTIMA PÁGINA) ==========
  // Añadir nueva página si no hay espacio suficiente
  const finalY = doc.lastAutoTable.finalY + 40;
  const espacioNecesario = 60; // espacio mínimo para las firmas
  
  if (finalY > pageHeight - 100) {
    // No hay suficiente espacio, agregar nueva página
    doc.addPage();
    yPos = margin + 20;
  } else {
    yPos = finalY;
  }
  
  doc.setFontSize(9);
  doc.setTextColor(0, 0, 0);
  doc.setFont("helvetica", "normal");
  
  const col1X = margin + 150;
  const col2X = pageWidth - margin - 150;
  const lineY = yPos + 30;
  
  // Líneas para las firmas
  doc.setLineWidth(1);
  doc.setDrawColor(100, 100, 100);
  doc.line(margin, lineY, margin + 200, lineY);
  doc.line(pageWidth - margin - 200, lineY, pageWidth - margin, lineY);
  
  // Etiquetas de firma
  doc.text("Firma del Estudiante", col1X, lineY + 15, { align: 'center' });
  doc.text("Director", col2X, lineY + 15, { align: 'center' });
  
  // Nombre del estudiante bajo su firma
  if (estudiante && (estudiante.nombres || estudiante.apellidos)) {
    doc.setFontSize(8);
    doc.setTextColor(100, 100, 100);
    doc.text(`${estudiante.nombres || ''} ${estudiante.apellidos || ''}`.trim(), col1X, lineY + 25, { align: 'center' });
  }

  // ========== GUARDAR PDF ==========
  const mes = bitacora?.mes ? String(bitacora.mes).padStart(2, '0') : 'XX';
  const anio = bitacora?.anio || 'XXXX';
  const apellido = estudiante?.apellidos?.split(' ')[0] || 'Estudiante';
  const estado = bitacora?.estado ? `_${bitacora.estado}` : '';
  
  const filename = `Bitacora_${apellido}_${anio}_${mes}${estado}.pdf`;
  doc.save(filename);
  
  return filename;
}