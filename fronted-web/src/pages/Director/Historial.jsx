import React, { useEffect, useMemo, useState } from "react";
import { apiGet } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Modal from "../../components/Modal.jsx";
import Badge from "../../components/Badge.jsx";
import { exportBitacoraPdf } from "../../lib/pdf";
import { getDirectorSelectedProject } from "../../lib/state";

export default function DirHistorialBitacoras() {
  const selected = getDirectorSelectedProject();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  // ✅ FILTROS
  const [searchTerm, setSearchTerm] = useState("");
  const [filterAnio, setFilterAnio] = useState("TODOS");

  // Modal de visualización
  const [openView, setOpenView] = useState(false);
  const [viewData, setViewData] = useState(null);
  const [viewLoading, setViewLoading] = useState(false);
  const [selectedBitacoraId, setSelectedBitacoraId] = useState("");

  async function load() {
    if (!selected?.id) {
      setToast({ msg: "⚠️ Primero selecciona un proyecto en Proyectos", kind: "bad" });
      return;
    }

    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      console.log(`📡 Cargando bitácoras aprobadas del proyecto ${selected.id}...`);
      
      // ✅ LLAMAR AL NUEVO ENDPOINT DE BITÁCORAS APROBADAS
      const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/bitacoras/aprobadas`);
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      
      console.log(`✅ ${arr.length} bitácoras aprobadas cargadas`);
      setRows(arr);
      setToast({ msg: `✅ ${arr.length} bitácoras aprobadas`, kind: "ok" });
    } catch (e) {
      console.error("❌ Error cargando historial:", e);
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [selected?.id]);

  // ✅ FILTRADO
  const filteredRows = useMemo(() => {
    let filtered = rows;

    // Filtro por año
    if (filterAnio !== "TODOS") {
      filtered = filtered.filter(r => String(r.anio) === filterAnio);
    }

    // Búsqueda
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
  }, [rows, filterAnio, searchTerm]);

  // Obtener años únicos para el filtro
  const aniosDisponibles = useMemo(() => {
    const anios = [...new Set(rows.map(r => String(r.anio)))];
    return anios.sort((a, b) => Number(b) - Number(a));
  }, [rows]);

  const columns = [
    { 
      key: "bitacoraId", 
      label: "ID Bitácora",
      render: (r) => (
        <div className="font-mono text-xs">{r.bitacoraId}</div>
      )
    },
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
      render: (r) => <Badge kind="ok">✅ APROBADA</Badge>
    },
    {
      key: "fecha",
      label: "Fecha Aprobación",
      render: (r) => {
        try {
          return new Date(r.creadoEn).toLocaleDateString('es-ES');
        } catch {
          return r.creadoEn || '-';
        }
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
      console.log(`📡 Obteniendo detalles de bitácora ${bitacoraId}...`);
      const res = await apiGet(`/api/v1/director/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];

      const estudiante = {
        nombres: res?.ayudanteNombres || res?.nombres || "",
        apellidos: res?.ayudanteApellidos || res?.apellidos || "",
        correoInstitucional: res?.correoInstitucional || "",
        facultad: res?.facultad || ""
      };

      setViewData({ bitacora, semanas, estudiante });
      console.log("✅ Detalles cargados");
    } catch (e) {
      console.error("❌ Error obteniendo detalles:", e);
      setToast({ msg: e.message, kind: "bad" });
      setOpenView(false);
    } finally {
      setViewLoading(false);
    }
  }

  async function descargarPdf(bitacoraId) {
    setToast({ msg: "⏳ Generando PDF...", kind: "info" });
    try {
      console.log(`📄 Generando PDF de bitácora ${bitacoraId}...`);
      const res = await apiGet(`/api/v1/director/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];

      const estudiante = {
        nombres: res?.ayudanteNombres || res?.nombres || "",
        apellidos: res?.ayudanteApellidos || res?.apellidos || "",
        correoInstitucional: res?.correoInstitucional || "",
        facultad: res?.facultad || ""
      };

      const filename = exportBitacoraPdf({
        bitacoraId,
        bitacora,
        semanas,
        estudiante
      });

      setToast({ msg: `✅ PDF generado: ${filename}`, kind: "ok" });
    } catch (e) {
      console.error("❌ Error generando PDF:", e);
      setToast({ msg: `❌ Error generando PDF: ${e.message}`, kind: "bad" });
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
          <div className="text-lg font-bold text-poli-ink">✅ Historial de Bitácoras Aprobadas</div>
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
                value={filterAnio}
                onChange={(e) => setFilterAnio(e.target.value)}
              >
                <option value="TODOS">📅 Todos los años</option>
                {aniosDisponibles.map(anio => (
                  <option key={anio} value={anio}>{anio}</option>
                ))}
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

        {/* Info y resumen */}
        {selected?.id && (
          <div className="p-4 bg-green-50 border border-green-200 rounded-xl">
            <div className="flex items-center justify-between">
              <div className="text-sm text-green-800">
                💡 <strong>Mostrando solo bitácoras aprobadas</strong> del proyecto. Total: <strong>{rows.length}</strong>
                {(searchTerm || filterAnio !== "TODOS") && (
                  <> | Filtradas: <strong>{filteredRows.length}</strong></>
                )}
              </div>
              {(searchTerm || filterAnio !== "TODOS") && (
                <button
                  onClick={() => {
                    setSearchTerm("");
                    setFilterAnio("TODOS");
                  }}
                  className="text-xs text-green-700 hover:text-green-900 underline font-semibold"
                >
                  Limpiar filtros
                </button>
              )}
            </div>
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

      {selected?.id && (
        loading ? <Loading /> : (
          filteredRows.length === 0 ? (
            <div className="p-8 text-center bg-gray-50 rounded-xl border border-gray-200">
              <div className="text-4xl mb-3">📭</div>
              <div className="text-gray-600">
                {rows.length === 0 
                  ? "No hay bitácoras aprobadas en este proyecto"
                  : "No se encontraron bitácoras con los filtros aplicados"}
              </div>
            </div>
          ) : (
            <Table columns={columns} rows={filteredRows} />
          )
        )
      )}

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
            <div className="rounded-xl bg-green-50 p-3 border border-green-200 text-sm">
              <b>Estado:</b> <Badge kind="ok">APROBADA</Badge>{" "}
              <span className="mx-2">|</span>
              <b>Año:</b> {viewData.bitacora?.anio}{" "}
              <span className="mx-2">|</span>
              <b>Mes:</b> {viewData.bitacora?.mes}
            </div>

            {/* Botones */}
            <div className="flex flex-wrap gap-2 justify-end">
              <button 
                onClick={() => descargarPdf(selectedBitacoraId)} 
                className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold hover:bg-red-700"
              >
                📄 Descargar PDF
              </button>
            </div>

            {/* Tabla */}
            <Table columns={viewColumns} rows={viewRows} />
          </div>
        )}
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}
