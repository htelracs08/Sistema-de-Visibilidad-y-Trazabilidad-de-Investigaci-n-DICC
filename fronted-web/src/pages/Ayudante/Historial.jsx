import React, { useEffect, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Modal from "../../components/Modal.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Table from "../../components/Table.jsx";
import { exportBitacoraPdf } from "../../lib/pdf";

export default function AyuHistorial() {
  const [bitacoras, setBitacoras] = useState([]);
  const [filteredBitacoras, setFilteredBitacoras] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });
  
  // Filtros
  const [searchTerm, setSearchTerm] = useState("");
  const [filterEstado, setFilterEstado] = useState("TODAS");
  const [filterAnio, setFilterAnio] = useState("TODOS");

  // Selección múltiple
  const [selectedIds, setSelectedIds] = useState(new Set());
  
  // Modal detalles
  const [openDetalle, setOpenDetalle] = useState(false);
  const [bitacoraDetalle, setBitacoraDetalle] = useState(null);

  async function cargarHistorial() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      console.log("📡 Cargando historial de bitácoras...");
      
      // Intentar endpoint /historial primero
      let res;
      try {
        res = await apiGet("/api/v1/ayudante/bitacoras/historial");
      } catch (e) {
        console.warn("⚠️ Endpoint /historial no disponible, usando /aprobadas");
        res = await apiGet("/api/v1/ayudante/bitacoras/aprobadas");
      }

      const data = Array.isArray(res?.bitacoras) ? res.bitacoras : 
                   Array.isArray(res) ? res : [];
      
      console.log(`✅ ${data.length} bitácoras cargadas`);
      setBitacoras(data);
      setFilteredBitacoras(data);
      
      // Contadores por estado
      const counts = {
        APROBADA: data.filter(b => b.estado === "APROBADA").length,
        RECHAZADA: data.filter(b => b.estado === "RECHAZADA").length,
        PENDIENTE: data.filter(b => b.estado === "PENDIENTE").length,
        BORRADOR: data.filter(b => b.estado === "BORRADOR").length
      };
      console.log("📊 Contadores:", counts);
      
    } catch (e) {
      console.error("❌ Error cargando historial:", e);
      setToast({ msg: `Error: ${e.message}`, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    cargarHistorial();
  }, []);

  // Aplicar filtros
  useEffect(() => {
    let result = [...bitacoras];

    // Filtro por estado
    if (filterEstado !== "TODAS") {
      result = result.filter(b => b.estado === filterEstado);
    }

    // Filtro por año
    if (filterAnio !== "TODOS") {
      result = result.filter(b => String(b.anio) === filterAnio);
    }

    // Búsqueda
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      result = result.filter(b => 
        String(b.bitacoraId || '').toLowerCase().includes(term) ||
        String(b.mes || '').includes(term) ||
        String(b.anio || '').includes(term) ||
        String(b.estado || '').toLowerCase().includes(term)
      );
    }

    setFilteredBitacoras(result);
  }, [bitacoras, filterEstado, filterAnio, searchTerm]);

  // Selección individual
  function toggleSelect(bitacoraId) {
    const newSet = new Set(selectedIds);
    if (newSet.has(bitacoraId)) {
      newSet.delete(bitacoraId);
    } else {
      newSet.add(bitacoraId);
    }
    setSelectedIds(newSet);
  }

  // Seleccionar todas las filtradas
  function selectAll() {
    const allFiltered = new Set(filteredBitacoras.map(b => b.bitacoraId));
    setSelectedIds(allFiltered);
  }

  // Deseleccionar todas
  function deselectAll() {
    setSelectedIds(new Set());
  }

  // ✅ ENVIAR MÚLTIPLES BITÁCORAS AL DIRECTOR
  async function enviarSeleccionadas() {
    if (selectedIds.size === 0) {
      setToast({ msg: "⚠️ No hay bitácoras seleccionadas", kind: "bad" });
      return;
    }

    // Verificar que todas sean BORRADOR o RECHAZADA
    const seleccionadas = bitacoras.filter(b => selectedIds.has(b.bitacoraId));
    const invalidas = seleccionadas.filter(b => 
      b.estado !== "BORRADOR" && b.estado !== "RECHAZADA"
    );

    if (invalidas.length > 0) {
      setToast({ 
        msg: `⚠️ Solo puedes enviar bitácoras en estado BORRADOR o RECHAZADA. ${invalidas.length} bitácoras no cumplen.`, 
        kind: "bad" 
      });
      return;
    }

    const confirmar = window.confirm(
      `¿Enviar ${selectedIds.size} bitácora(s) al director?\n\n` +
      "Una vez enviadas, no podrás modificarlas hasta que sean revisadas.\n\n" +
      `Bitácoras: ${Array.from(selectedIds).join(', ')}`
    );

    if (!confirmar) return;

    setLoading(true);
    let exitosas = 0;
    let fallidas = 0;

    for (const bitacoraId of selectedIds) {
      try {
        console.log(`📤 Enviando bitácora ${bitacoraId}...`);
        await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/enviar`, {});
        exitosas++;
      } catch (e) {
        console.error(`❌ Error enviando ${bitacoraId}:`, e);
        fallidas++;
      }
    }

    setLoading(false);
    setSelectedIds(new Set());

    if (fallidas === 0) {
      setToast({ msg: `✅ ${exitosas} bitácora(s) enviadas correctamente`, kind: "ok" });
    } else {
      setToast({ msg: `⚠️ ${exitosas} enviadas, ${fallidas} fallaron`, kind: "bad" });
    }

    await cargarHistorial();
  }

  async function verDetalle(bitacoraId) {
    setLoading(true);
    try {
      console.log("📡 Obteniendo detalles de bitácora:", bitacoraId);
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${bitacoraId}`);
      setBitacoraDetalle(res);
      setOpenDetalle(true);
    } catch (e) {
      console.error("❌ Error obteniendo detalles:", e);
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  async function descargarPdf(bitacoraId) {
    try {
      console.log("📄 Descargando PDF de bitácora:", bitacoraId);
      setToast({ msg: "⏳ Generando PDF...", kind: "info" });

      const res = await apiGet(`/api/v1/ayudante/bitacoras/${bitacoraId}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      
      // Obtener info del estudiante si está disponible
      const estudiante = res?.estudiante || null;

      exportBitacoraPdf({
        bitacoraId,
        bitacora,
        semanas,
        estudiante
      });

      setToast({ msg: "✅ PDF descargado", kind: "ok" });
    } catch (e) {
      console.error("❌ Error generando PDF:", e);
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  // Estadísticas
  const stats = {
    total: bitacoras.length,
    aprobadas: bitacoras.filter(b => b.estado === "APROBADA").length,
    rechazadas: bitacoras.filter(b => b.estado === "RECHAZADA").length,
    pendientes: bitacoras.filter(b => b.estado === "PENDIENTE").length,
    borradores: bitacoras.filter(b => b.estado === "BORRADOR").length
  };

  const aniosDisponibles = [...new Set(bitacoras.map(b => b.anio))].sort((a, b) => b - a);

  const columns = [
    {
      key: "select",
      label: (
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            className="w-4 h-4 cursor-pointer"
            checked={selectedIds.size > 0 && selectedIds.size === filteredBitacoras.length}
            onChange={(e) => e.target.checked ? selectAll() : deselectAll()}
          />
          <span>Sel</span>
        </div>
      ),
      render: (row) => {
        // Solo permitir seleccionar BORRADOR o RECHAZADA
        const puedeSeleccionar = row.estado === "BORRADOR" || row.estado === "RECHAZADA";
        
        return (
          <input
            type="checkbox"
            className="w-4 h-4 cursor-pointer disabled:opacity-30"
            disabled={!puedeSeleccionar}
            checked={selectedIds.has(row.bitacoraId)}
            onChange={() => toggleSelect(row.bitacoraId)}
          />
        );
      }
    },
    { key: "bitacoraId", label: "ID Bitácora" },
    { 
      key: "periodo", 
      label: "Periodo",
      render: (row) => `${row.anio}-${String(row.mes).padStart(2, '0')}`
    },
    {
      key: "estado",
      label: "Estado",
      render: (row) => {
        const badges = {
          APROBADA: <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold">✅ APROBADA</span>,
          RECHAZADA: <span className="px-2 py-1 bg-red-100 text-red-700 rounded-full text-xs font-bold">❌ RECHAZADA</span>,
          PENDIENTE: <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-bold">⏳ PENDIENTE</span>,
          BORRADOR: <span className="px-2 py-1 bg-gray-100 text-gray-700 rounded-full text-xs font-bold">📝 BORRADOR</span>
        };
        return badges[row.estado] || row.estado;
      }
    },
    {
      key: "acciones",
      label: "Acciones",
      render: (row) => (
        <div className="flex gap-2">
          <button
            onClick={() => verDetalle(row.bitacoraId)}
            className="px-3 py-1.5 bg-poli-navy text-white rounded-lg text-sm font-bold hover:bg-blue-900"
          >
            👁️ Ver
          </button>
          <button
            onClick={() => descargarPdf(row.bitacoraId)}
            className="px-3 py-1.5 bg-poli-red text-white rounded-lg text-sm font-bold hover:bg-red-700"
          >
            📄 PDF
          </button>
        </div>
      )
    }
  ];

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      
      {/* Header */}
      <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div>
          <h2 className="text-xl font-bold text-poli-ink">📚 Historial de Bitácoras</h2>
          <p className="text-sm text-gray-500 mt-1">Revisa y gestiona todas tus bitácoras mensuales</p>
        </div>
        
        <button
          onClick={cargarHistorial}
          className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold transition-all"
        >
          🔄 Refrescar
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
        <div className="p-3 bg-gray-50 rounded-xl border border-gray-200">
          <div className="text-2xl font-bold text-poli-navy">{stats.total}</div>
          <div className="text-xs text-gray-600 mt-1">Total</div>
        </div>
        <div className="p-3 bg-green-50 rounded-xl border border-green-200">
          <div className="text-2xl font-bold text-green-700">{stats.aprobadas}</div>
          <div className="text-xs text-green-600 mt-1">Aprobadas</div>
        </div>
        <div className="p-3 bg-blue-50 rounded-xl border border-blue-200">
          <div className="text-2xl font-bold text-blue-700">{stats.pendientes}</div>
          <div className="text-xs text-blue-600 mt-1">Pendientes</div>
        </div>
        <div className="p-3 bg-red-50 rounded-xl border border-red-200">
          <div className="text-2xl font-bold text-red-700">{stats.rechazadas}</div>
          <div className="text-xs text-red-600 mt-1">Rechazadas</div>
        </div>
        <div className="p-3 bg-amber-50 rounded-xl border border-amber-200">
          <div className="text-2xl font-bold text-amber-700">{stats.borradores}</div>
          <div className="text-xs text-amber-600 mt-1">Borradores</div>
        </div>
      </div>

      {/* Filtros y búsqueda */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        <input
          type="text"
          placeholder="🔍 Buscar..."
          className="rounded-xl border border-gray-300 px-4 py-2"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        <select
          className="rounded-xl border border-gray-300 px-4 py-2"
          value={filterEstado}
          onChange={(e) => setFilterEstado(e.target.value)}
        >
          <option value="TODAS">Todos los estados</option>
          <option value="APROBADA">✅ Aprobadas</option>
          <option value="PENDIENTE">⏳ Pendientes</option>
          <option value="RECHAZADA">❌ Rechazadas</option>
          <option value="BORRADOR">📝 Borradores</option>
        </select>

        <select
          className="rounded-xl border border-gray-300 px-4 py-2"
          value={filterAnio}
          onChange={(e) => setFilterAnio(e.target.value)}
        >
          <option value="TODOS">Todos los años</option>
          {aniosDisponibles.map(anio => (
            <option key={anio} value={anio}>{anio}</option>
          ))}
        </select>

        {/* Botón Enviar Seleccionadas */}
        <button
          onClick={enviarSeleccionadas}
          disabled={selectedIds.size === 0 || loading}
          className={`rounded-xl px-4 py-2 font-bold transition-all flex items-center justify-center gap-2 ${
            selectedIds.size > 0 && !loading
              ? 'bg-gradient-to-r from-emerald-600 to-emerald-700 text-white hover:shadow-xl'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          <span>📤</span>
          <span>Enviar ({selectedIds.size})</span>
        </button>
      </div>

      {/* Info de selección */}
      {selectedIds.size > 0 && (
        <div className="p-3 bg-blue-50 rounded-xl border border-blue-200">
          <div className="flex items-center justify-between">
            <div className="text-sm text-blue-800">
              <span className="font-bold">{selectedIds.size}</span> bitácora(s) seleccionada(s)
            </div>
            <button
              onClick={deselectAll}
              className="text-xs text-blue-600 hover:text-blue-800 underline"
            >
              Deseleccionar todas
            </button>
          </div>
        </div>
      )}

      {/* Tabla */}
      {loading && <Loading />}
      {!loading && <Table columns={columns} rows={filteredBitacoras} />}

      {/* Modal Detalle */}
      <Modal open={openDetalle} title="📋 Detalles de Bitácora" onClose={() => setOpenDetalle(false)} size="xl">
        {bitacoraDetalle && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <span className="text-sm text-gray-600 font-semibold">Bitácora ID:</span>
                <div className="text-base">{bitacoraDetalle.bitacora?.bitacoraId || bitacoraDetalle.bitacoraId}</div>
              </div>
              <div>
                <span className="text-sm text-gray-600 font-semibold">Estado:</span>
                <div className="text-base">{bitacoraDetalle.bitacora?.estado || bitacoraDetalle.estado}</div>
              </div>
              <div>
                <span className="text-sm text-gray-600 font-semibold">Año:</span>
                <div className="text-base">{bitacoraDetalle.bitacora?.anio || bitacoraDetalle.anio}</div>
              </div>
              <div>
                <span className="text-sm text-gray-600 font-semibold">Mes:</span>
                <div className="text-base">{bitacoraDetalle.bitacora?.mes || bitacoraDetalle.mes}</div>
              </div>
            </div>

            <div className="border-t pt-3">
              <div className="font-bold mb-2">Semanas registradas:</div>
              {Array.isArray(bitacoraDetalle.semanas) && bitacoraDetalle.semanas.length > 0 ? (
                bitacoraDetalle.semanas.map((semana, idx) => (
                  <div key={idx} className="mb-4 p-3 bg-gray-50 rounded-xl">
                    <div className="font-semibold text-sm">
                      📅 {semana.fechaInicioSemana} - {semana.fechaFinSemana}
                    </div>
                    <div className="text-sm mt-1">{semana.actividadesRealizadas}</div>
                    {semana.actividades && semana.actividades.length > 0 && (
                      <div className="mt-2 text-xs text-gray-600">
                        {semana.actividades.length} actividad(es) detallada(s)
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <div className="text-sm text-gray-500 italic">Sin semanas registradas</div>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t">
              <button
                onClick={() => descargarPdf(bitacoraDetalle.bitacora?.bitacoraId || bitacoraDetalle.bitacoraId)}
                className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold hover:bg-red-700"
              >
                📄 Descargar PDF
              </button>
              <button
                onClick={() => setOpenDetalle(false)}
                className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
              >
                Cerrar
              </button>
            </div>
          </div>
        )}
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}