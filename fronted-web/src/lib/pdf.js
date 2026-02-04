// import jsPDF from "jspdf";

// // Export simple tipo el PDFBox que tenías (tabla en texto)
// export function exportBitacoraPdf({ bitacoraId, bitacora, rows }) {
//   const doc = new jsPDF({ unit: "pt", format: "letter" });

//   let y = 50;
//   doc.setFont("helvetica", "bold");
//   doc.setFontSize(16);
//   doc.text(`Bitácora ${bitacoraId}`, 40, y);

//   y += 22;
//   doc.setFont("helvetica", "normal");
//   doc.setFontSize(10);

//   if (bitacora) {
//     const anio = bitacora.anio ?? "";
//     const mes = bitacora.mes ?? "";
//     const estado = bitacora.estado ?? "";
//     doc.text(`Año: ${anio}   Mes: ${mes}   Estado: ${estado}`, 40, y);
//     y += 18;
//   }

//   // Encabezado (monoespaciado)
//   doc.setFont("courier", "bold");
//   doc.setFontSize(9);
//   doc.text("Semana | Act.Semana | Observ. | Anexos | Actividad | Ini | Sal | Hrs", 40, y);
//   y += 14;

//   doc.setFont("courier", "normal");
//   for (const line of rows) {
//     if (y > 740) break; // evita overflow
//     doc.text(line, 40, y);
//     y += 12;
//   }

//   doc.save(`bitacora_${bitacoraId}.pdf`);
// }
import jsPDF from "jspdf";
import "jspdf-autotable";

/**
 * 📄 EXPORTACIÓN PDF PROFESIONAL - Sistema DICC
 * Compatible con: Aprobadas, Rechazadas, Borradores
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

  // ============================================
  // 1. ENCABEZADO PROFESIONAL
  // ============================================
  let yPos = margin;

  // Título principal
  doc.setFont("helvetica", "bold");
  doc.setFontSize(20);
  doc.setTextColor(11, 42, 74); // Azul EPN
  doc.text("BITÁCORA MENSUAL DE ACTIVIDADES", pageWidth / 2, yPos, { align: "center" });
  yPos += 25;

  doc.setFontSize(12);
  doc.setTextColor(80, 80, 80);
  doc.text("Escuela Politécnica Nacional", pageWidth / 2, yPos, { align: "center" });
  yPos += 15;
  doc.setFontSize(10);
  doc.text("Departamento de Investigación y Control de Calidad", pageWidth / 2, yPos, { align: "center" });
  yPos += 20;

  // Línea decorativa
  doc.setDrawColor(230, 57, 70); // Rojo EPN
  doc.setLineWidth(3);
  doc.line(margin, yPos, pageWidth - margin, yPos);
  yPos += 30;

  // ============================================
  // 2. INFORMACIÓN DEL ESTUDIANTE Y BITÁCORA
  // ============================================
  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);
  doc.setTextColor(0, 0, 0);

  const infoData = [];

  if (estudiante) {
    infoData.push(
      ["Estudiante:", `${estudiante.nombres || ''} ${estudiante.apellidos || ''}`.trim()],
      ["Correo Institucional:", estudiante.correoInstitucional || '-'],
      ["Facultad:", estudiante.facultad || '-']
    );
  }

  if (bitacora) {
    const estadoColor = bitacora.estado === "APROBADA" ? "#10B981" : 
                        bitacora.estado === "RECHAZADA" ? "#E63946" : "#F59E0B";
    
    infoData.push(
      ["Bitácora ID:", bitacoraId || '-'],
      ["Periodo:", `${bitacora.anio || ''}-${String(bitacora.mes || '').padStart(2, '0')}`],
      ["Estado:", bitacora.estado || '-']
    );
  }

  if (infoData.length > 0) {
    doc.autoTable({
      startY: yPos,
      head: [],
      body: infoData,
      theme: 'plain',
      styles: {
        fontSize: 10,
        cellPadding: 6,
      },
      columnStyles: {
        0: { 
          fontStyle: 'bold', 
          cellWidth: 140,
          textColor: [11, 42, 74]
        },
        1: { 
          cellWidth: 'auto',
          fontStyle: 'normal'
        }
      },
      margin: { left: margin, right: margin }
    });
    yPos = doc.lastAutoTable.finalY + 25;
  }

  // ============================================
  // 3. TABLA DE ACTIVIDADES PROFESIONAL
  // ============================================
  const tableData = [];
  
  for (const semana of semanas || []) {
    const acts = Array.isArray(semana.actividades) ? semana.actividades : [];
    
    // Encabezado de semana
    tableData.push([
      {
        content: `📅 SEMANA: ${semana.fechaInicioSemana || ''} - ${semana.fechaFinSemana || ''}`,
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

    // Descripción de actividades de la semana
    tableData.push([
      {
        content: `Actividades realizadas: ${semana.actividadesRealizadas || '-'}`,
        colSpan: 7,
        styles: { 
          fillColor: [240, 240, 240],
          fontStyle: 'italic',
          halign: 'left',
          fontSize: 9
        }
      }
    ]);

    // Observaciones y anexos
    if (semana.observaciones || (semana.anexos && semana.anexos !== '-')) {
      const extras = [];
      if (semana.observaciones) extras.push(`📝 ${semana.observaciones}`);
      if (semana.anexos && semana.anexos !== '-') extras.push(`📎 ${semana.anexos}`);
      
      tableData.push([
        {
          content: extras.join(' | '),
          colSpan: 7,
          styles: { 
            fillColor: [255, 250, 230],
            fontSize: 8,
            textColor: [100, 100, 100],
            halign: 'left'
          }
        }
      ]);
    }

    // Actividades individuales
    if (acts.length === 0) {
      tableData.push([
        { 
          content: '(Sin actividades detalladas registradas)', 
          colSpan: 7, 
          styles: { 
            halign: 'center', 
            textColor: [150, 150, 150],
            fontStyle: 'italic',
            fontSize: 9
          } 
        }
      ]);
    } else {
      for (const act of acts) {
        tableData.push([
          { content: act.actividadId || '-', styles: { fontSize: 7, halign: 'center' } },
          { content: act.horaInicio || '-', styles: { halign: 'center' } },
          { content: act.horaSalida || '-', styles: { halign: 'center' } },
          { content: act.totalHoras || '-', styles: { halign: 'center', fontStyle: 'bold' } },
          { content: act.descripcion || '-', styles: { cellWidth: 'auto' } },
          '', // V°B°
          ''  // Observaciones
        ]);
      }
    }

    // Línea separadora entre semanas
    tableData.push([
      {
        content: '',
        colSpan: 7,
        styles: { 
          fillColor: [255, 255, 255],
          minCellHeight: 8
        }
      }
    ]);
  }

  // Generar tabla
  doc.autoTable({
    startY: yPos,
    head: [[
      { content: 'ID', styles: { halign: 'center' } },
      { content: 'Hora Inicio', styles: { halign: 'center' } },
      { content: 'Hora Fin', styles: { halign: 'center' } },
      { content: 'Total Hrs', styles: { halign: 'center' } },
      { content: 'Descripción de la Actividad', styles: { halign: 'center' } },
      { content: 'V°B°', styles: { halign: 'center' } },
      { content: 'Observaciones', styles: { halign: 'center' } }
    ]],
    body: tableData,
    theme: 'grid',
    styles: {
      fontSize: 9,
      cellPadding: 5,
      lineColor: [180, 180, 180],
      lineWidth: 0.5,
    },
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
      // Pie de página
      const footerY = pageHeight - 30;
      doc.setFontSize(8);
      doc.setTextColor(100, 100, 100);
      doc.setFont("helvetica", "normal");
      
      const pageInfo = `Página ${data.pageNumber}`;
      const dateInfo = `Generado: ${new Date().toLocaleDateString('es-ES')}`;
      const bitacoraInfo = `Bitácora: ${bitacoraId || '-'}`;
      
      doc.text(pageInfo, margin, footerY);
      doc.text(bitacoraInfo, pageWidth / 2, footerY, { align: 'center' });
      doc.text(dateInfo, pageWidth - margin, footerY, { align: 'right' });
    }
  });

  // ============================================
  // 4. SECCIÓN DE FIRMAS
  // ============================================
  const finalY = doc.lastAutoTable.finalY + 40;
  
  if (finalY < pageHeight - 100) {
    doc.setFontSize(9);
    doc.setTextColor(0, 0, 0);
    doc.setFont("helvetica", "normal");
    
    const col1X = margin + 150;
    const col2X = pageWidth - margin - 150;
    const lineY = finalY + 30;
    
    // Líneas de firma
    doc.setLineWidth(1);
    doc.setDrawColor(100, 100, 100);
    doc.line(margin, lineY, margin + 200, lineY);
    doc.line(pageWidth - margin - 200, lineY, pageWidth - margin, lineY);
    
    // Textos de firma
    doc.setFontSize(9);
    doc.text("Firma del Estudiante", col1X, lineY + 15, { align: 'center' });
    doc.text("V°B° Director de Proyecto", col2X, lineY + 15, { align: 'center' });
    
    if (estudiante) {
      doc.setFontSize(8);
      doc.setTextColor(100, 100, 100);
      doc.text(
        `${estudiante.nombres || ''} ${estudiante.apellidos || ''}`.trim(), 
        col1X, 
        lineY + 25, 
        { align: 'center' }
      );
    }
  }

  // ============================================
  // 5. GUARDAR PDF
  // ============================================
  const mes = bitacora?.mes ? String(bitacora.mes).padStart(2, '0') : 'XX';
  const anio = bitacora?.anio || 'XXXX';
  const apellido = estudiante?.apellidos?.split(' ')[0] || 'Estudiante';
  const estado = bitacora?.estado ? `_${bitacora.estado}` : '';
  
  const filename = `Bitacora_${apellido}_${anio}_${mes}${estado}.pdf`;
  doc.save(filename);
  
  return filename;
}

/**
 * Exportación simple (backward compatibility)
 */
export function exportBitacoraPdfSimple({ bitacoraId, bitacora, rows }) {
  const doc = new jsPDF({ unit: "pt", format: "letter" });

  let y = 50;
  doc.setFont("helvetica", "bold");
  doc.setFontSize(16);
  doc.text(`Bitácora ${bitacoraId}`, 40, y);

  y += 22;
  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);

  if (bitacora) {
    doc.text(`Año: ${bitacora.anio || ''}   Mes: ${bitacora.mes || ''}   Estado: ${bitacora.estado || ''}`, 40, y);
    y += 18;
  }

  doc.setFont("courier", "bold");
  doc.setFontSize(9);
  doc.text("Semana | Act.Semana | Observ. | Anexos | Actividad | Ini | Sal | Hrs", 40, y);
  y += 14;

  doc.setFont("courier", "normal");
  for (const line of rows) {
    if (y > 740) break;
    doc.text(line, 40, y);
    y += 12;
  }

  doc.save(`bitacora_${bitacoraId}.pdf`);
}