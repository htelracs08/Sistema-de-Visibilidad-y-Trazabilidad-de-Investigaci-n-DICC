import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Modal from "../../components/Modal.jsx";
import Toast from "../../components/Toast.jsx";
import { getDirectorSelectedProject } from "../../lib/state";
import { exportBitacoraPdf } from "../../lib/pdf";

function safe(v) {
  return (v ?? "").toString().replace(/\n/g, " ").replace(/\r/g, " ").trim();
}

function padRight(text, width) {
  const t = safe(text);
  if (t.length >= width) return t.substring(0, width);
  return t + " ".repeat(width - t.length);
}

function wrapCell(text, width) {
  const t = safe(text);
  if (!t) return [padRight("", width)];
  const words = t.split(/\s+/);
  const lines = [];
  let line = "";
  for (const w of words) {
    if (!line) line = w;
    else if ((line.length + 1 + w.length) <= width) line += " " + w;
    else {
      lines.push(padRight(line, width));
      line = w;
    }
  }
  if (line) lines.push(padRight(line, width));
  return lines;
}

function wrapRow(row, widths) {
  const wrapped = row.map((c, i) => wrapCell(c, widths[i]));
  const maxLines = Math.max(...wrapped.map((x) => x.length), 1);

  const out = [];
  for (let i = 0; i < maxLines; i++) {
    const cells = wrapped.map((cellLines) => (i < cellLines.length ? cellLines[i] : padRight("", cellLines[0].length)));
    out.push(cells.map((c, idx) => (idx < cells.length - 1 ? c + " | " : c)).join(""));
  }
  return out;
}

export default function DirBitacoras() {
  const selected = getDirectorSelectedProject();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  const [openView, setOpenView] = useState(false);
  const [viewLoading, setViewLoading] = useState(false);
  const [bitacoraData, setBitacoraData] = useState(null); // { bitacora, semanas }
  const [bitacoraId, setBitacoraId] = useState("");

  async function load() {
    if (!selected?.id) {
      setToast({ msg: "Primero selecciona un proyecto en Proyectos.", kind: "bad" });
      return;
    }

    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/bitacoras/pendientes`);
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(arr);
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [selected?.id]);

  const columns = [
    { key: "bitacoraId", label: "BitacoraId" },
    { key: "contratoId", label: "ContratoId" },
    { key: "anio", label: "Año" },
    { key: "mes", label: "Mes" },
    { key: "estado", label: "Estado" },
    { key: "correoInstitucional", label: "Correo" },
    { key: "nombres", label: "Nombres" },
    { key: "apellidos", label: "Apellidos" },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        <button
          className="rounded-xl px-3 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
          onClick={() => ver(r.bitacoraId)}
        >
          Ver
        </button>
      )
    }
  ];

  async function ver(id) {
    setBitacoraId(String(id));
    setOpenView(true);
    setViewLoading(true);
    setBitacoraData(null);

    try {
      const res = await apiGet(`/api/v1/director/bitacoras/${id}`);
      // contrato esperado: { ok:true, bitacora:{...}, semanas:[...] }
      const bitacora = res?.bitacora ?? null;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      setBitacoraData({ bitacora, semanas });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
      setOpenView(false);
    } finally {
      setViewLoading(false);
    }
  }

  const tableRows = useMemo(() => {
    if (!bitacoraData) return [];
    const widths = [18, 22, 18, 12, 24, 8, 8, 6]; // como tu Swing
    const out = [];

    for (const semana of bitacoraData.semanas || []) {
      const semanaLabel = `${safe(semana.fechaInicioSemana)} - ${safe(semana.fechaFinSemana)}`.trim();
      const actSemana = safe(semana.actividadesRealizadas);
      const obs = safe(semana.observaciones);
      const anexos = safe(semana.anexos);

      const acts = Array.isArray(semana.actividades) ? semana.actividades : [];

      if (!acts.length) {
        out.push({ semana: semanaLabel, actSemana, obs, anexos, actividad: "", ini: "", sal: "", hrs: "" });
        continue;
      }

      for (const act of acts) {
        out.push({
          semana: semanaLabel,
          actSemana,
          obs,
          anexos,
          actividad: safe(act.descripcion),
          ini: safe(act.horaInicio),
          sal: safe(act.horaSalida),
          hrs: safe(act.totalHoras)
        });
      }
    }

    return { out, widths };
  }, [bitacoraData]);

  function descargarPdf() {
    if (!bitacoraData) return;

    const widths = tableRows.widths;
    const flatLines = [];

    // Encabezado tabla tipo texto
    const header = ["Semana", "Act.Semana", "Observ.", "Anexos", "Actividad", "Ini", "Sal", "Hrs"]
      .map((c, i) => padRight(c, widths[i]))
      .map((c, idx, arr) => (idx < arr.length - 1 ? c + " | " : c))
      .join("");

    flatLines.push(header);

    for (const r of tableRows.out) {
      const row = [r.semana, r.actSemana, r.obs, r.anexos, r.actividad, r.ini, r.sal, r.hrs];
      const lines = wrapRow(row, widths);
      flatLines.push(...lines);
    }

    exportBitacoraPdf({
      bitacoraId,
      bitacora: bitacoraData.bitacora,
      rows: flatLines
    });
  }

  async function revisar(decision) {
    if (!bitacoraId) return;

    const obs = prompt("Observación (opcional):", "") ?? "";

    try {
      await apiPost(`/api/v1/director/bitacoras/${bitacoraId}/revisar`, {
        decision,
        observacion: obs
      });

      setToast({ msg: `Revisión realizada: ${decision}`, kind: "ok" });
      setOpenView(false);
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  const viewColumns = [
    { key: "semana", label: "Semana" },
    { key: "actSemana", label: "Actividades Semana" },
    { key: "obs", label: "Observaciones" },
    { key: "anexos", label: "Anexos" },
    { key: "actividad", label: "Actividad" },
    { key: "ini", label: "Inicio" },
    { key: "sal", label: "Salida" },
    { key: "hrs", label: "Horas" }
  ];

  const viewRows = bitacoraData
    ? (tableRows.out || []).map((r) => ({
        semana: r.semana,
        actSemana: r.actSemana,
        obs: r.obs,
        anexos: r.anexos,
        actividad: r.actividad,
        ini: r.ini,
        sal: r.sal,
        hrs: r.hrs
      }))
    : [];

  const header = selected?.id
    ? `Proyecto seleccionado: ${selected.codigo} - ${selected.nombre}`
    : "No hay proyecto seleccionado";

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Bitácoras pendientes</div>
          <div className="text-sm text-gray-500">{header}</div>
        </div>

        <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
          Refrescar
        </button>
      </div>

      {loading ? <Loading /> : <Table columns={columns} rows={rows} />}

      <Modal open={openView} title={`Bitácora ${bitacoraId}`} onClose={() => setOpenView(false)}>
        {viewLoading && <Loading />}
        {!viewLoading && bitacoraData && (
          <div className="space-y-3">
            <div className="rounded-xl bg-poli-gray p-3 border border-gray-200 text-sm">
              <b>Estado:</b> {bitacoraData.bitacora?.estado ?? "-"}{" "}
              <span className="mx-2">|</span>
              <b>Año:</b> {bitacoraData.bitacora?.anio ?? "-"}{" "}
              <span className="mx-2">|</span>
              <b>Mes:</b> {bitacoraData.bitacora?.mes ?? "-"}
            </div>

            <div className="flex flex-wrap gap-2 justify-end">
              <button onClick={descargarPdf} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
                Descargar PDF
              </button>
              <button onClick={() => revisar("APROBAR")} className="rounded-xl px-4 py-2 bg-emerald-600 text-white font-bold">
                Aprobar
              </button>
              <button onClick={() => revisar("RECHAZAR")} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
                Rechazar
              </button>
            </div>

            <Table columns={viewColumns} rows={viewRows} />
          </div>
        )}
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}
