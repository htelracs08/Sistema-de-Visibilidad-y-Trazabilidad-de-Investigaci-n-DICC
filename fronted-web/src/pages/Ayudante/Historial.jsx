import React, { useEffect, useState } from "react";
import { apiGet } from "../../lib/api";
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
  const [filterAnio, setFilterAnio] = useState("TODOS");
  
  // Modal detalles
  const [openDetalle, setOpenDetalle] = useState(false);
  const [bitacoraDetalle, setBitacoraDetalle] = useState(null);

  async function cargarHistorial() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      console.log("📡 Cargando bitácoras aprobadas...");
      
      // ✅ USAR ENDPOINT CORRECTO: /bitacoras/aprobadas
      const res = await apiGet("/api/v1/ayudante/bitacoras/aprobadas");

      const data = Array.isArray(res?.bitacoras) ? res.bitacoras : 
                   Array.isArray(res) ? res : [];
      
      console.log(`✅ ${data.length} bitácoras aprobadas cargadas`);
      setBitacoras(data);
      setFilteredBitacoras(data);
      setToast({ msg: `✅ ${data.length} bitácoras aprobadas`, kind: "ok" });
      
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
        String(b.anio || '').includes(term)
      );
    }

    setFilteredBitacoras(result);
  }, [bitacoras, filterAnio, searchTerm]);

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
      const estudiante = res?.bitacora?.estudiante || res?.estudiante || null;

      exportBitacoraPdf({
        bitacoraId,
        bitacora,
        semanas,
        estudiante
      });

      setToast({ msg: "✅ PDF descargado correctamente", kind: "ok" });
    } catch (e) {
      console.error("❌ Error generando PDF:", e);
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  // Obtener años únicos para filtro
  const aniosDisponibles = [...new Set(bitacoras.map(b => String(b.anio)))].sort((a, b) => Number(b) - Number(a));

  const columns = [
    { 
      key: "bitacoraId", 
      label: "ID Bitácora",
      render: (row) => (
        <div className="font-mono text-xs">{row.bitacoraId}</div>
      )
    },
    { 
      key: "periodo", 
      label: "Periodo",
      render: (row) => `${row.anio}-${String(row.mes).padStart(2, '0')}`
    },
    {
      key: "estado",
      label: "Estado",
      render: (row) => (
        <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold">
          ✅ APROBADA
        </span>
      )
    },
    {
      key: "fecha",
      label: "Fecha Aprobación",
      render: (row) => {
        try {
          return new Date(row.creadoEn).toLocaleDateString('es-ES');
        } catch {
          return row.creadoEn || '-';
        }
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
          <h2 className="text-xl font-bold text-poli-ink">✅ Historial de Bitácoras Aprobadas</h2>
          <p className="text-sm text-gray-500 mt-1">Revisa y descarga tus bitácoras aprobadas por el director</p>
        </div>
        
        <button
          onClick={cargarHistorial}
          className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold transition-all"
        >
          🔄 Refrescar
        </button>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <div className="p-4 bg-green-50 rounded-xl border border-green-200">
          <div className="text-3xl font-bold text-green-700">{bitacoras.length}</div>
          <div className="text-sm text-green-600 mt-1">Bitácoras Aprobadas</div>
        </div>
        <div className="p-4 bg-blue-50 rounded-xl border border-blue-200">
          <div className="text-3xl font-bold text-blue-700">{aniosDisponibles.length}</div>
          <div className="text-sm text-blue-600 mt-1">Años con Registros</div>
        </div>
        <div className="p-4 bg-purple-50 rounded-xl border border-purple-200">
          <div className="text-3xl font-bold text-purple-700">{filteredBitacoras.length}</div>
          <div className="text-sm text-purple-600 mt-1">Resultados Filtrados</div>
        </div>
      </div>

      {/* Filtros y búsqueda */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        <input
          type="text"
          placeholder="🔍 Buscar por ID, periodo..."
          className="rounded-xl border border-gray-300 px-4 py-2"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        <select
          className="rounded-xl border border-gray-300 px-4 py-2"
          value={filterAnio}
          onChange={(e) => setFilterAnio(e.target.value)}
        >
          <option value="TODOS">📅 Todos los años</option>
          {aniosDisponibles.map(anio => (
            <option key={anio} value={anio}>{anio}</option>
          ))}
        </select>
      </div>

      {/* Info importante */}
      <div className="p-4 bg-green-50 border border-green-200 rounded-xl">
        <div className="flex items-center justify-between">
          <div className="text-sm text-green-800">
            💡 <strong>Solo bitácoras aprobadas:</strong> Aquí aparecen únicamente las bitácoras que han sido aprobadas por el director del proyecto.
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

      {/* Tabla */}
      {loading && <Loading />}
      {!loading && (
        filteredBitacoras.length === 0 ? (
          <div className="p-8 text-center bg-gray-50 rounded-xl border border-gray-200">
            <div className="text-4xl mb-3">📭</div>
            <div className="text-gray-600">
              {bitacoras.length === 0 
                ? "Aún no tienes bitácoras aprobadas"
                : "No se encontraron bitácoras con los filtros aplicados"}
            </div>
          </div>
        ) : (
          <Table columns={columns} rows={filteredBitacoras} />
        )
      )}

      {/* Modal Detalle */}
      <Modal open={openDetalle} title="📋 Detalles de Bitácora Aprobada" onClose={() => setOpenDetalle(false)} size="xl">
        {bitacoraDetalle && (
          <div className="space-y-4">
            {/* Info básica */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <span className="text-sm text-gray-600 font-semibold">Bitácora ID:</span>
                <div className="text-base font-mono">{bitacoraDetalle.bitacora?.bitacoraId || bitacoraDetalle.bitacoraId}</div>
              </div>
              <div>
                <span className="text-sm text-gray-600 font-semibold">Estado:</span>
                <div className="text-base">
                  <span className="px-2 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold">
                    ✅ APROBADA
                  </span>
                </div>
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

            {/* Semanas */}
            <div className="border-t pt-3">
              <div className="font-bold mb-2">📅 Semanas registradas:</div>
              {Array.isArray(bitacoraDetalle.semanas) && bitacoraDetalle.semanas.length > 0 ? (
                bitacoraDetalle.semanas.map((semana, idx) => (
                  <div key={idx} className="mb-4 p-3 bg-gray-50 rounded-xl">
                    <div className="font-semibold text-sm text-poli-navy">
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

            {/* Botones */}
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
