import jsPDF from "jspdf";

// Export simple tipo el PDFBox que tenías (tabla en texto)
export function exportBitacoraPdf({ bitacoraId, bitacora, rows }) {
  const doc = new jsPDF({ unit: "pt", format: "letter" });

  let y = 50;
  doc.setFont("helvetica", "bold");
  doc.setFontSize(16);
  doc.text(`Bitácora ${bitacoraId}`, 40, y);

  y += 22;
  doc.setFont("helvetica", "normal");
  doc.setFontSize(10);

  if (bitacora) {
    const anio = bitacora.anio ?? "";
    const mes = bitacora.mes ?? "";
    const estado = bitacora.estado ?? "";
    doc.text(`Año: ${anio}   Mes: ${mes}   Estado: ${estado}`, 40, y);
    y += 18;
  }

  // Encabezado (monoespaciado)
  doc.setFont("courier", "bold");
  doc.setFontSize(9);
  doc.text("Semana | Act.Semana | Observ. | Anexos | Actividad | Ini | Sal | Hrs", 40, y);
  y += 14;

  doc.setFont("courier", "normal");
  for (const line of rows) {
    if (y > 740) break; // evita overflow
    doc.text(line, 40, y);
    y += 12;
  }

  doc.save(`bitacora_${bitacoraId}.pdf`);
}
