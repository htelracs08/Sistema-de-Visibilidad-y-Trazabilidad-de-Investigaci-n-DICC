import React, { useEffect, useMemo, useState } from "react";
import { apiGet } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Badge from "../../components/Badge.jsx";
import Loading from "../../components/Loading.jsx";

export default function Semaforo() {
  const [rows, setRows] = useState([]);
  const [filtro, setFiltro] = useState("TODOS");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  async function load() {
    setErr("");
    setLoading(true);
    try {
      const res = await apiGet("/api/v1/jefatura/semaforo");

      // tu API_CONTRACT tiene 2 variantes:
      // A) { ok:true, items:[...] }
      // B) [...]
      const items = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(items);
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  const filtered = useMemo(() => {
    if (filtro === "TODOS") return rows;
    return rows.filter((r) => (r.color || r.estadoSemaforo || "").toUpperCase() === filtro);
  }, [rows, filtro]);

  const columns = [
    { key: "contratoId", label: "ContratoId" },
    {
      key: "proyecto",
      label: "Proyecto",
      render: (r) => ((r.proyectoCodigo || "") + " - " + (r.proyectoNombre || "")).trim() || "-"
    },
    {
      key: "ayudante",
      label: "Ayudante",
      render: (r) => ((r.nombres || "") + " " + (r.apellidos || "")).trim() || "-"
    },
    { key: "fechaInicio", label: "Inicio" },
    { key: "mesesEsperados", label: "Meses Esperados" },
    { key: "mesesAprobados", label: "Aprobados" },
    { key: "faltantes", label: "Faltantes" },
    {
      key: "color",
      label: "Color",
      render: (r) => {
        const c = (r.color || r.estadoSemaforo || "N/A").toUpperCase();
        if (c === "VERDE") return <Badge kind="ok">VERDE</Badge>;
        if (c === "AMARILLO") return <Badge kind="warn">AMARILLO</Badge>;
        if (c === "ROJO") return <Badge kind="bad">ROJO</Badge>;
        return <Badge>OTRO</Badge>;
      }
    }
  ];

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Semáforo</div>
          <div className="text-sm text-gray-500">GET /api/v1/jefatura/semaforo</div>
        </div>

        <div className="flex gap-2">
          <select
            className="rounded-xl border px-3 py-2"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
          >
            <option value="TODOS">TODOS</option>
            <option value="VERDE">VERDE</option>
            <option value="AMARILLO">AMARILLO</option>
            <option value="ROJO">ROJO</option>
          </select>

          <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Refrescar
          </button>
        </div>
      </div>

      {loading && <Loading />}
      {err && <div className="text-red-600 font-semibold">Error: {err}</div>}
      {!loading && !err && <Table columns={columns} rows={filtered} />}
    </div>
  );
}
