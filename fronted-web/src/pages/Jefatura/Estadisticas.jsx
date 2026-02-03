import React, { useEffect, useState } from "react";
import { apiGet } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";

export default function Estadisticas() {
  const [proy, setProy] = useState([]);
  const [ayud, setAyud] = useState({ totalActivos: 0, porTipo: [] });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  async function load() {
    setErr("");
    setLoading(true);
    try {
      const proyRes = await apiGet("/api/v1/jefatura/proyectos/estadisticas");
      const ayudRes = await apiGet("/api/v1/jefatura/ayudantes/estadisticas");

      setProy(Array.isArray(proyRes) ? proyRes : []);
      // tu contract dice totalActivos/porTipo, pero tu Swing corregía nombres también:
      const totalActivos = ayudRes?.totalActivos ?? ayudRes?.activosTotal ?? 0;
      const porTipo = ayudRes?.porTipo ?? [];
      setAyud({ totalActivos, porTipo });
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  const colsProy = [
    { key: "tipo", label: "Tipo" },
    { key: "activo", label: "Activo", render: (r) => (r.activo ? "ACTIVO" : "INACTIVO") },
    { key: "total", label: "Total" }
  ];

  const colsAyud = [
    { key: "tipoAyudante", label: "TipoAyudante", render: (r) => r.tipoAyudante || r.tipo || "-" },
    { key: "activos", label: "Activos", render: (r) => r.activos ?? r.total ?? 0 }
  ];

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Estadísticas</div>
          <div className="text-sm text-gray-500">Proyectos + Ayudantes</div>
        </div>
        <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
          Refrescar
        </button>
      </div>

      {loading && <Loading />}
      {err && <div className="text-red-600 font-semibold">Error: {err}</div>}

      {!loading && !err && (
        <div className="grid md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <div className="font-bold text-poli-ink">Proyectos por tipo/estado</div>
            <Table columns={colsProy} rows={proy} />
          </div>

          <div className="space-y-2">
            <div className="font-bold text-poli-ink">Ayudantes activos por tipo</div>
            <div className="text-sm text-gray-500">TOTAL: {ayud.totalActivos}</div>
            <Table columns={colsAyud} rows={ayud.porTipo} />
          </div>
        </div>
      )}
    </div>
  );
}
