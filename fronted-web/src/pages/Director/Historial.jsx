import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Modal from "../../components/Modal.jsx";
import Badge from "../../components/Badge.jsx";
import ConfirmDialog from "../../components/ConfirmDialog.jsx";
import { exportBitacoraPdf } from "../../lib/pdf";
import { getDirectorSelectedProject } from "../../lib/state";

export default function DirHistorialBitacoras() {
  const selected = getDirectorSelectedProject();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  // ✅ FILTROS
  const [filtroEstado, setFiltroEstado] = useState("TODOS");
  const [searchTerm, setSearchTerm] = useState("");

  // Modal de visualización
  const [openView, setOpenView] = useState(false);
  const [viewData, setViewData] = useState(null);
  const [viewLoading, setViewLoading] = useState(false);
  const [selectedBitacoraId, setSelectedBitacoraId] = useState("");

  // Modal de confirmación
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    action: null,
    bitacoraId: null
  });
  const [observacion, setObservacion] = useState("");

  async function load() {
    if (!selected?.id) {
      setToast({ msg: "⚠️ Primero selecciona un proyecto en Proyectos", kind: "bad" });
      return;
    }

    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      // Obtener TODAS las bitácoras del proyecto (no solo pendientes)
      const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/bitacoras/todas`);
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(arr);
      setToast({ msg: `✅ ${arr.length} bitácoras cargadas`, kind: "ok" });
    } catch (e) {
      // Si el endpoint "todas" no existe, usar "pendientes"
      try {
        const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/bitacoras/pendientes`);
        const arr = Array.isArray(res) ? res : (res?.items ?? []);
        setRows(arr);
        setToast({ msg: `⚠️ Solo se muestran bitácoras pendientes (endpoint /todas no disponible)`, kind: "warn" });
      } catch (e2) {
        setToast({ msg: e2.message, kind: "bad" });
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [selected?.id]);

  // ✅ FILTRADO
  const filteredRows = useMemo(() => {
    let filtered = rows;

    if (filtroEstado !== "TODOS") {
      filtered = filtered.filter(r => r.estado === filtroEstado);
    }

    if (searchTerm.trim()) {
      const term = searchTerm.trim().toLowerCase();
      filtered = filtered.filter(r => {
        const searchText = [
          r.bitacoraId,
          r.correoInstitucional,
          r.nombres,
          r.apellidos,
          r.anio,
          r.mes
        ].join(" ").toLowerCase();
        return searchText.includes(term);
      });
    }

    return filtered;
  }, [rows, filtroEstado, searchTerm]);

  const columns = [
    { key: "bitacoraId", label: "BitacoraId" },
    {
      key: "ayudante",
      label: "Ayudante",
      render: (r) => (
        <div>
          <div className="font-semibold">{r.nombres} {r.apellidos}</div>
          <div className="text-xs text-gray-500">{r.correoInstitucional}</div>
        </div>
      )
    },
    {
      key: "periodo",
      label: "Periodo",
      render: (r) => `${r.anio}-${String(r.mes).padStart(2, '0')}`
    },
    {
      key: "estado",
      label: "Estado",
      render: (r) => {
        if (r.estado === "APROBADA") return <Badge kind="ok">APROBADA</Badge>;
        if (r.estado === "RECHAZADA") return <Badge kind="bad">RECHAZADA</Badge>;
        if (r.estado === "PENDIENTE") return <Badge kind="warn">PENDIENTE</Badge>;
        return <Badge kind="info">BORRADOR</Badge>;
      }
    },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        <div className="flex gap-2">
          <button
            className="rounded-xl px-3 py-2 bg-poli-navy text-white font-bold text-sm hover:bg-blue-900"
            onClick={() => verDetalle(r.bitacoraId)}
          >
            👁️ Ver
          </button>
          <button
            className="rounded-xl px-3 py-2 bg-poli-red text-white font-bold text-sm hover:bg-red-700"
            onClick={() => descargarPdf(r.bitacoraId)}
          >
            📄 PDF
          </button>
        </div>
      )
    }
  ];

  async function verDetalle(bitacoraId) {
    setSelectedBitacoraId(bitacoraId);
    setOpenView(true);
    setViewLoading(true);
    setViewData(null);

    try {
      const res = await apiGet(`/api/v1/director/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];

      const estudiante = {
        nombres: res.nombres || "",
        apellidos: res.apellidos || "",
        correoInstitucional: res.correoInstitucional || "",
        facultad: res.facultad || ""
      };

      setViewData({ bitacora, semanas, estudiante });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
      setOpenView(false);
    } finally {
      setViewLoading(false);
    }
  }

  async function descargarPdf(bitacoraId) {
    setToast({ msg: "⏳ Generando PDF...", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/director/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];

      const estudiante = {
        nombres: res.nombres || "",
        apellidos: res.apellidos || "",
        correoInstitucional: res.correoInstitucional || "",
        facultad: res.facultad || ""
      };

      const filename = exportBitacoraPdf({
        bitacoraId,
        bitacora,
        semanas,
        estudiante
      });

      setToast({ msg: `✅ PDF generado: ${filename}`, kind: "ok" });
    } catch (e) {
      setToast({ msg: `❌ Error generando PDF: ${e.message}`, kind: "bad" });
    }
  }

  function abrirConfirmacion(action) {
    setConfirmDialog({
      open: true,
      action,
      bitacoraId: selectedBitacoraId
    });
    setObservacion("");
  }

  async function revisar() {
    const { action, bitacoraId } = confirmDialog;
    if (!bitacoraId || !action) return;

    const decision = action === "aprobar" ? "APROBAR" : "RECHAZAR";

    try {
      await apiPost(`/api/v1/director/bitacoras/${bitacoraId}/revisar`, {
        decision,
        observacion: observacion.trim() || ""
      });

      setToast({ msg: `✅ Bitácora ${decision === "APROBAR" ? "aprobada" : "rechazada"}`, kind: "ok" });
      setOpenView(false);
      setConfirmDialog({ open: false, action: null, bitacoraId: null });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  const viewColumns = [
    { key: "semana", label: "Semana" },
    { key: "actSemana", label: "Actividades" },
    { key: "obs", label: "Observaciones" },
    { key: "anexos", label: "Anexos" },
    { key: "actividad", label: "Detalle Actividad" },
    { key: "ini", label: "Inicio" },
    { key: "sal", label: "Salida" },
    { key: "hrs", label: "Horas" }
  ];

  const viewRows = useMemo(() => {
    if (!viewData) return [];
    
    const rows = [];
    for (const semana of viewData.semanas || []) {
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
  }, [viewData]);

  const header = selected?.id
    ? `📁 ${selected.codigo} - ${selected.nombre}`
    : "⚠️ No hay proyecto seleccionado";

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <div>
          <div className="text-lg font-bold text-poli-ink">📚 Historial de Bitácoras</div>
          <div className="text-sm text-gray-500">{header}</div>
        </div>

        {/* Filtros */}
        {selected?.id && (
          <div className="grid md:grid-cols-3 gap-3">
            <div>
              <input
                type="text"
                placeholder="🔍 Buscar por ayudante, ID, periodo..."
                className="w-full rounded-xl border px-4 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div>
              <select
                className="w-full rounded-xl border px-4 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
                value={filtroEstado}
                onChange={(e) => setFiltroEstado(e.target.value)}
              >
                <option value="TODOS">📋 Todas las bitácoras</option>
                <option value="PENDIENTE">⏳ Solo pendientes</option>
                <option value="APROBADA">✅ Solo aprobadas</option>
                <option value="RECHAZADA">❌ Solo rechazadas</option>
                <option value="BORRADOR">📝 Solo borradores</option>
              </select>
            </div>

            <div>
              <button 
                onClick={load} 
                className="w-full rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
              >
                🔄 Refrescar
              </button>
            </div>
          </div>
        )}

        {/* Resumen */}
        {selected?.id && (searchTerm || filtroEstado !== "TODOS") && (
          <div className="flex items-center gap-2 text-sm">
            <span className="text-gray-600">
              Mostrando {filteredRows.length} de {rows.length} bitácoras
            </span>
            <button
              onClick={() => {
                setSearchTerm("");
                setFiltroEstado("TODOS");
              }}
              className="text-poli-red hover:underline"
            >
              Limpiar filtros
            </button>
          </div>
        )}
      </div>

      {!selected?.id && (
        <div className="p-8 text-center bg-amber-50 rounded-xl border border-amber-200">
          <div className="text-5xl mb-4">⚠️</div>
          <div className="text-lg font-bold text-amber-800">No hay proyecto seleccionado</div>
          <div className="text-amber-600 mt-2">
            Ve a <strong>Proyectos</strong> y selecciona un proyecto primero.
          </div>
        </div>
      )}

      {selected?.id && (loading ? <Loading /> : <Table columns={columns} rows={filteredRows} />)}

      {/* Modal de visualización */}
      <Modal open={openView} title={`📋 Bitácora ${selectedBitacoraId}`} onClose={() => setOpenView(false)}>
        {viewLoading && <Loading />}
        {!viewLoading && viewData && (
          <div className="space-y-3">
            {/* Info estudiante */}
            {viewData.estudiante && (
              <div className="rounded-xl bg-blue-50 p-4 border border-blue-200">
                <div className="font-bold text-blue-900">👤 Ayudante:</div>
                <div className="text-sm text-blue-800 mt-1">
                  {viewData.estudiante.nombres} {viewData.estudiante.apellidos}
                </div>
                <div className="text-xs text-blue-600">{viewData.estudiante.correoInstitucional}</div>
              </div>
            )}

            {/* Info bitácora */}
            <div className="rounded-xl bg-poli-gray p-3 border border-gray-200 text-sm">
              <b>Estado:</b> {viewData.bitacora?.estado}{" "}
              <span className="mx-2">|</span>
              <b>Año:</b> {viewData.bitacora?.anio}{" "}
              <span className="mx-2">|</span>
              <b>Mes:</b> {viewData.bitacora?.mes}
            </div>

            {/* Botones */}
            <div className="flex flex-wrap gap-2 justify-end">
              <button 
                onClick={() => descargarPdf(selectedBitacoraId)} 
                className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold hover:bg-blue-900"
              >
                📄 Descargar PDF
              </button>
              
              {viewData.bitacora?.estado === "PENDIENTE" && (
                <>
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
                </>
              )}
            </div>

            {/* Tabla */}
            <Table columns={viewColumns} rows={viewRows} />
          </div>
        )}
      </Modal>

      {/* Dialog de confirmación */}
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
                ? "El ayudante recibirá una notificación de aprobación."
                : "El ayudante deberá corregir y reenviar la bitácora."}
            </p>
            <div>
              <label className="text-sm text-gray-600 font-semibold">
                Observación {confirmDialog.action === "rechazar" && "(requerida)"}:
              </label>
              <textarea
                className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[80px]"
                value={observacion}
                onChange={(e) => setObservacion(e.target.value)}
                placeholder="Comentarios..."
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