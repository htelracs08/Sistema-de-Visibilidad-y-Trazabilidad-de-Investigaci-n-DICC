import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Modal from "../../components/Modal.jsx";
import Badge from "../../components/Badge.jsx";
import { exportBitacoraPdf } from "../../lib/pdf";

export default function AyuHistorialBitacoras() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  // ✨ FILTROS MEJORADOS
  const [filtroEstado, setFiltroEstado] = useState("TODOS");
  const [searchTerm, setSearchTerm] = useState("");
  const [filtroAnio, setFiltroAnio] = useState("TODOS");

  // Modal de visualización
  const [openView, setOpenView] = useState(false);
  const [viewData, setViewData] = useState(null);
  const [viewLoading, setViewLoading] = useState(false);
  const [selectedBitacoraId, setSelectedBitacoraId] = useState("");

  // ========================================
  // CARGA DE BITÁCORAS
  // ========================================
  
  async function load() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      // Obtener bitácoras aprobadas
      const aprobadas = await apiGet("/api/v1/ayudante/bitacoras/aprobadas");
      const arrAprobadas = Array.isArray(aprobadas) ? aprobadas : (aprobadas?.items ?? []);

      // TODO: Cuando el backend tenga endpoint para TODAS las bitácoras, usar ese
      // Por ahora solo mostramos las aprobadas
      setRows(arrAprobadas.map(b => ({...b, estado: b.estado || "APROBADA"})));
      
      setToast({ msg: `✅ ${arrAprobadas.length} bitácora${arrAprobadas.length !== 1 ? 's' : ''} cargada${arrAprobadas.length !== 1 ? 's' : ''}`, kind: "ok" });
    } catch (e) {
      setToast({ msg: `❌ Error: ${e.message}`, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  // ========================================
  // FILTRADO AVANZADO
  // ========================================
  
  const filteredRows = useMemo(() => {
    let filtered = rows;

    // Filtro por estado
    if (filtroEstado !== "TODOS") {
      filtered = filtered.filter(r => r.estado === filtroEstado);
    }

    // Filtro por año
    if (filtroAnio !== "TODOS") {
      filtered = filtered.filter(r => String(r.anio) === filtroAnio);
    }

    // Filtro por búsqueda
    if (searchTerm.trim()) {
      const term = searchTerm.trim().toLowerCase();
      filtered = filtered.filter(r => {
        const searchText = [
          r.bitacoraId,
          r.anio,
          r.mes,
          r.estado
        ].join(" ").toLowerCase();
        return searchText.includes(term);
      });
    }

    return filtered;
  }, [rows, filtroEstado, filtroAnio, searchTerm]);

  // ========================================
  // AÑOS ÚNICOS PARA FILTRO
  // ========================================
  
  const aniosUnicos = useMemo(() => {
    const anios = [...new Set(rows.map(r => String(r.anio)).filter(Boolean))];
    return ["TODOS", ...anios.sort((a, b) => b.localeCompare(a))];
  }, [rows]);

  // ========================================
  // COLUMNAS DE LA TABLA
  // ========================================
  
  const columns = [
    { 
      key: "bitacoraId", 
      label: "ID",
      render: (r) => <span className="font-mono text-sm font-semibold">#{r.bitacoraId}</span>
    },
    { 
      key: "periodo", 
      label: "Período",
      render: (r) => (
        <span className="px-3 py-1 rounded-lg bg-blue-100 text-blue-800 font-semibold text-sm">
          {r.anio}-{String(r.mes).padStart(2, '0')}
        </span>
      )
    },
    {
      key: "estado",
      label: "Estado",
      render: (r) => {
        if (r.estado === "APROBADA") return <Badge kind="ok">✅ APROBADA</Badge>;
        if (r.estado === "RECHAZADA") return <Badge kind="bad">❌ RECHAZADA</Badge>;
        if (r.estado === "PENDIENTE") return <Badge kind="warn">⏳ PENDIENTE</Badge>;
        return <Badge kind="info">📝 BORRADOR</Badge>;
      }
    },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        <div className="flex gap-2">
          <button
            className="rounded-xl px-3 py-2 bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold text-sm hover:shadow-lg transition-all"
            onClick={() => verDetalle(r.bitacoraId)}
          >
            👁️ Ver
          </button>
          <button
            className="rounded-xl px-3 py-2 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold text-sm hover:shadow-lg transition-all"
            onClick={() => descargarPdf(r.bitacoraId)}
          >
            📄 PDF
          </button>
          {(r.estado === "BORRADOR" || r.estado === "RECHAZADA") && (
            <button
              className="rounded-xl px-3 py-2 bg-gradient-to-r from-emerald-600 to-emerald-700 text-white font-bold text-sm hover:shadow-lg transition-all"
              onClick={() => enviar(r.bitacoraId)}
            >
              📤 Enviar
            </button>
          )}
        </div>
      )
    }
  ];

  // ========================================
  // VER DETALLE DE BITÁCORA
  // ========================================
  
  async function verDetalle(bitacoraId) {
    setSelectedBitacoraId(bitacoraId);
    setOpenView(true);
    setViewLoading(true);
    setViewData(null);

    try {
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      setViewData({ bitacora, semanas });
    } catch (e) {
      setToast({ msg: `❌ Error: ${e.message}`, kind: "bad" });
      setOpenView(false);
    } finally {
      setViewLoading(false);
    }
  }

  // ========================================
  // DESCARGAR PDF
  // ========================================
  
  async function descargarPdf(bitacoraId) {
    setToast({ msg: "⏳ Generando PDF...", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${bitacoraId}`);
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

  // ========================================
  // ENVIAR BITÁCORA
  // ========================================
  
  async function enviar(bitacoraId) {
    const confirmar = window.confirm("¿Estás seguro de enviar esta bitácora al director?");
    if (!confirmar) return;

    try {
      await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/enviar`, {});
      setToast({ msg: "✅ Bitácora enviada al director", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ Error: ${e.message}`, kind: "bad" });
    }
  }

  // ========================================
  // COLUMNAS Y DATOS DEL MODAL
  // ========================================
  
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

  // ========================================
  // ESTADÍSTICAS RÁPIDAS
  // ========================================
  
  const stats = useMemo(() => {
    return {
      total: rows.length,
      aprobadas: rows.filter(r => r.estado === "APROBADA").length,
      rechazadas: rows.filter(r => r.estado === "RECHAZADA").length,
      pendientes: rows.filter(r => r.estado === "PENDIENTE").length,
      borradores: rows.filter(r => r.estado === "BORRADOR").length
    };
  }, [rows]);

  // ========================================
  // RENDERIZADO
  // ========================================
  
  return (
    <div className="space-y-4">
      
      {/* ============ ESTADÍSTICAS RÁPIDAS ============ */}
      <div className="grid md:grid-cols-4 gap-4">
        <div className="rounded-xl bg-gradient-to-br from-blue-50 to-blue-100 p-4 border border-blue-200 shadow-sm">
          <div className="text-2xl font-bold text-blue-900">{stats.total}</div>
          <div className="text-sm text-blue-600 font-semibold">Total de bitácoras</div>
        </div>

        <div className="rounded-xl bg-gradient-to-br from-green-50 to-green-100 p-4 border border-green-200 shadow-sm">
          <div className="text-2xl font-bold text-green-900">{stats.aprobadas}</div>
          <div className="text-sm text-green-600 font-semibold">✅ Aprobadas</div>
        </div>

        <div className="rounded-xl bg-gradient-to-br from-yellow-50 to-yellow-100 p-4 border border-yellow-200 shadow-sm">
          <div className="text-2xl font-bold text-yellow-900">{stats.pendientes}</div>
          <div className="text-sm text-yellow-600 font-semibold">⏳ Pendientes</div>
        </div>

        <div className="rounded-xl bg-gradient-to-br from-red-50 to-red-100 p-4 border border-red-200 shadow-sm">
          <div className="text-2xl font-bold text-red-900">{stats.rechazadas}</div>
          <div className="text-sm text-red-600 font-semibold">❌ Rechazadas</div>
        </div>
      </div>

      {/* ============ TABLA PRINCIPAL ============ */}
      <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
        
        {/* Header con filtros */}
        <div className="flex flex-col gap-4">
          <div className="text-2xl font-bold text-poli-ink flex items-center gap-2">
            📚 Historial de Bitácoras
          </div>

          {/* Filtros */}
          <div className="grid md:grid-cols-4 gap-3">
            <div>
              <input
                type="text"
                placeholder="🔍 Buscar por ID, año, mes..."
                className="w-full rounded-xl border border-gray-300 px-4 py-2.5 outline-none focus:ring-2 focus:ring-poli-navy/30 transition-all"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div>
              <select
                className="w-full rounded-xl border border-gray-300 px-4 py-2.5 outline-none focus:ring-2 focus:ring-poli-navy/30 transition-all"
                value={filtroEstado}
                onChange={(e) => setFiltroEstado(e.target.value)}
              >
                <option value="TODOS">📋 Todos los estados</option>
                <option value="APROBADA">✅ Solo aprobadas</option>
                <option value="RECHAZADA">❌ Solo rechazadas</option>
                <option value="PENDIENTE">⏳ Solo pendientes</option>
                <option value="BORRADOR">📝 Solo borradores</option>
              </select>
            </div>

            <div>
              <select
                className="w-full rounded-xl border border-gray-300 px-4 py-2.5 outline-none focus:ring-2 focus:ring-poli-navy/30 transition-all"
                value={filtroAnio}
                onChange={(e) => setFiltroAnio(e.target.value)}
              >
                {aniosUnicos.map(anio => (
                  <option key={anio} value={anio}>
                    {anio === "TODOS" ? "📅 Todos los años" : `Año ${anio}`}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <button 
                onClick={load} 
                className="w-full rounded-xl px-4 py-2.5 bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold hover:shadow-lg transition-all"
              >
                🔄 Refrescar
              </button>
            </div>
          </div>

          {/* Resumen de filtros */}
          {(searchTerm || filtroEstado !== "TODOS" || filtroAnio !== "TODOS") && (
            <div className="flex items-center justify-between px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl">
              <div className="flex items-center gap-2 text-sm">
                <span className="font-semibold text-blue-900">
                  📊 Mostrando {filteredRows.length} de {rows.length} bitácoras
                </span>
              </div>
              <button
                onClick={() => {
                  setSearchTerm("");
                  setFiltroEstado("TODOS");
                  setFiltroAnio("TODOS");
                }}
                className="text-poli-red hover:underline text-sm font-semibold"
              >
                🗑️ Limpiar filtros
              </button>
            </div>
          )}
        </div>

        {loading ? <Loading /> : <Table columns={columns} rows={filteredRows} />}
      </div>

      {/* ============ MODAL DE VISUALIZACIÓN ============ */}
      <Modal 
        open={openView} 
        title={`📋 Bitácora #${selectedBitacoraId}`} 
        onClose={() => setOpenView(false)}
      >
        {viewLoading && <Loading />}
        {!viewLoading && viewData && (
          <div className="space-y-4">
            
            {/* Info de la bitácora */}
            <div className="rounded-xl bg-gradient-to-r from-blue-50 to-indigo-50 p-4 border border-blue-200">
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span className="text-gray-600">Año:</span>{" "}
                  <span className="font-bold">{viewData.bitacora?.anio}</span>
                </div>
                <div>
                  <span className="text-gray-600">Mes:</span>{" "}
                  <span className="font-bold">{viewData.bitacora?.mes}</span>
                </div>
                <div className="col-span-2">
                  <span className="text-gray-600">Estado:</span>{" "}
                  {viewData.bitacora?.estado === "APROBADA" ? (
                    <Badge kind="ok">✅ APROBADA</Badge>
                  ) : viewData.bitacora?.estado === "RECHAZADA" ? (
                    <Badge kind="bad">❌ RECHAZADA</Badge>
                  ) : viewData.bitacora?.estado === "PENDIENTE" ? (
                    <Badge kind="warn">⏳ PENDIENTE</Badge>
                  ) : (
                    <Badge kind="info">📝 BORRADOR</Badge>
                  )}
                </div>
              </div>
            </div>

            {/* Tabla de actividades */}
            <Table columns={viewColumns} rows={viewRows} />

            {/* Botones de acción */}
            <div className="flex gap-3 justify-end pt-4 border-t">
              <button
                onClick={() => descargarPdf(selectedBitacoraId)}
                className="rounded-xl px-5 py-2.5 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold hover:shadow-lg transition-all"
              >
                📄 Descargar PDF
              </button>
              {(viewData.bitacora?.estado === "BORRADOR" || viewData.bitacora?.estado === "RECHAZADA") && (
                <button
                  onClick={() => {
                    enviar(selectedBitacoraId);
                    setOpenView(false);
                  }}
                  className="rounded-xl px-5 py-2.5 bg-gradient-to-r from-emerald-600 to-emerald-700 text-white font-bold hover:shadow-lg transition-all"
                >
                  📤 Enviar al Director
                </button>
              )}
            </div>
          </div>
        )}
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}