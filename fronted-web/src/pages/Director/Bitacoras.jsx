import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Modal from "../../components/Modal.jsx";
import Toast from "../../components/Toast.jsx";
import ConfirmDialog from "../../components/ConfirmDialog.jsx"; // ✅ IMPORTAR ESTO
import { getDirectorSelectedProject } from "../../lib/state";
import { exportBitacoraPdf } from "../../lib/pdf";

export default function DirBitacoras() {
  const selected = getDirectorSelectedProject();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  const [openView, setOpenView] = useState(false);
  const [viewLoading, setViewLoading] = useState(false);
  const [bitacoraData, setBitacoraData] = useState(null);
  const [bitacoraId, setBitacoraId] = useState("");

  // ✅ Estados para confirmaciones
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    action: null,
    bitacoraId: null
  });
  const [observacion, setObservacion] = useState("");

  async function load() {
    if (!selected?.id) {
      setToast({ msg: "Primero selecciona un proyecto en la pestaña Proyectos.", kind: "bad" });
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
          👁️ Ver
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
      const bitacora = res?.bitacora ?? null;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      
      const estudiante = bitacora ? {
        nombres: res.nombres || "",
        apellidos: res.apellidos || "",
        correoInstitucional: res.correoInstitucional || "",
        facultad: res.facultad || ""
      } : null;

      setBitacoraData({ bitacora, semanas, estudiante });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
      setOpenView(false);
    } finally {
      setViewLoading(false);
    }
  }

  // ✅ Función para descargar PDF
  function descargarPdf() {
    if (!bitacoraData) return;

    try {
      const fileName = exportBitacoraPdf({
        bitacoraId,
        bitacora: bitacoraData.bitacora,
        semanas: bitacoraData.semanas,
        estudiante: bitacoraData.estudiante
      });

      setToast({ msg: `✅ PDF generado: ${fileName}`, kind: "ok" });
    } catch (e) {
      setToast({ msg: `❌ Error generando PDF: ${e.message}`, kind: "bad" });
    }
  }

  // ✅ Abrir confirmación
  function abrirConfirmacion(action) {
    setConfirmDialog({
      open: true,
      action,
      bitacoraId
    });
    setObservacion("");
  }

  // ✅ Revisar bitácora
  async function revisar() {
    const { action, bitacoraId: id } = confirmDialog;
    if (!id || !action) return;

    const decision = action === "aprobar" ? "APROBAR" : "RECHAZAR";

    // Validar observación si es rechazo
    if (action === "rechazar" && !observacion.trim()) {
      setToast({ msg: "La observación es requerida al rechazar", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/director/bitacoras/${id}/revisar`, {
        decision,
        observacion: observacion.trim() || ""
      });

      setToast({ 
        msg: `✅ Bitácora ${decision === "APROBAR" ? "aprobada" : "rechazada"} correctamente`, 
        kind: "ok" 
      });
      setOpenView(false);
      setConfirmDialog({ open: false, action: null, bitacoraId: null });
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

  const viewRows = useMemo(() => {
    if (!bitacoraData) return [];
    
    const rows = [];
    for (const semana of bitacoraData.semanas || []) {
      const acts = Array.isArray(semana.actividades) ? semana.actividades : [];
      if (!acts.length) {
        rows.push({
          semana: `${semana.fechaInicioSemana || ""} - ${semana.fechaFinSemana || ""}`.trim(),
          actSemana: semana.actividadesRealizadas || "",
          obs: semana.observaciones || "",
          anexos: semana.anexos || "",
          actividad: "",
          ini: "",
          sal: "",
          hrs: ""
        });
      } else {
        for (const a of acts) {
          rows.push({
            semana: `${semana.fechaInicioSemana || ""} - ${semana.fechaFinSemana || ""}`.trim(),
            actSemana: semana.actividadesRealizadas || "",
            obs: semana.observaciones || "",
            anexos: semana.anexos || "",
            actividad: a.descripcion || "",
            ini: a.horaInicio || "",
            sal: a.horaSalida || "",
            hrs: a.totalHoras || ""
          });
        }
      }
    }
    return rows;
  }, [bitacoraData]);

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
          🔄 Refrescar
        </button>
      </div>

      {loading ? <Loading /> : <Table columns={columns} rows={rows} />}

      <Modal open={openView} title={`📋 Bitácora ${bitacoraId}`} onClose={() => setOpenView(false)}>
        {viewLoading && <Loading />}
        {!viewLoading && bitacoraData && (
          <div className="space-y-3">
            {bitacoraData.estudiante && (
              <div className="rounded-xl bg-blue-50 p-4 border border-blue-200">
                <div className="font-bold text-blue-900">👤 Ayudante:</div>
                <div className="text-sm text-blue-800 mt-1">
                  {bitacoraData.estudiante.nombres} {bitacoraData.estudiante.apellidos}
                </div>
                <div className="text-xs text-blue-600">{bitacoraData.estudiante.correoInstitucional}</div>
              </div>
            )}

            <div className="rounded-xl bg-poli-gray p-3 border border-gray-200 text-sm">
              <b>Estado:</b> {bitacoraData.bitacora?.estado ?? "-"}{" "}
              <span className="mx-2">|</span>
              <b>Año:</b> {bitacoraData.bitacora?.anio ?? "-"}{" "}
              <span className="mx-2">|</span>
              <b>Mes:</b> {bitacoraData.bitacora?.mes ?? "-"}
            </div>

            <div className="flex flex-wrap gap-2 justify-end">
              <button 
                onClick={descargarPdf} 
                className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold hover:bg-blue-900"
              >
                📄 Descargar PDF
              </button>
              <button 
                onClick={() => abrirConfirmacion("aprobar")} 
                className="rounded-xl px-4 py-2 bg-emerald-600 text-white font-bold hover:bg-emerald-700"
              >
                ✅ Aprobar
              </button>
              <button 
                onClick={() => abrirConfirmacion("rechazar")} 
                className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold hover:bg-red-700"
              >
                ❌ Rechazar
              </button>
            </div>

            <Table columns={viewColumns} rows={viewRows} />
          </div>
        )}
      </Modal>

      <ConfirmDialog
        open={confirmDialog.open}
        title={
          confirmDialog.action === "aprobar"
            ? "¿Aprobar esta bitácora?"
            : "¿Rechazar esta bitácora?"
        }
        message={
          <div>
            <p className="mb-3">
              {confirmDialog.action === "aprobar"
                ? "Al aprobar, el ayudante recibirá una notificación y la bitácora quedará registrada."
                : "Al rechazar, el ayudante deberá corregir y reenviar la bitácora."}
            </p>
            <div>
              <label className="text-sm text-gray-600 font-semibold">
                Observación {confirmDialog.action === "rechazar" && <span className="text-red-600">(requerida)</span>}:
              </label>
              <textarea
                className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[80px]"
                value={observacion}
                onChange={(e) => setObservacion(e.target.value)}
                placeholder="Escribe aquí tus comentarios..."
              />
            </div>
          </div>
        }
        confirmText={confirmDialog.action === "aprobar" ? "Aprobar" : "Rechazar"}
        cancelText="Cancelar"
        onConfirm={revisar}
        onCancel={() => setConfirmDialog({ open: false, action: null, bitacoraId: null })}
        type={confirmDialog.action === "aprobar" ? "info" : "danger"}
      />

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}