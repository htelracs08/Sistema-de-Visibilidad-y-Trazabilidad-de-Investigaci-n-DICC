import React, { useEffect, useState } from "react";
import { apiGet } from "../../lib/api";
import Loading from "../../components/Loading.jsx";

export default function Dashboard() {
  const [activos, setActivos] = useState(null);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function load() {
    setErr("");
    setLoading(true);
    try {
      const res = await apiGet("/api/v1/jefatura/ayudantes/activos");
      setActivos(res?.activos ?? null);
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Ayudantes activos (global)</div>
          <div className="text-sm text-gray-500">GET /api/v1/jefatura/ayudantes/activos</div>
        </div>
        <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
          Refrescar
        </button>
      </div>

      <div className="mt-4">
        {loading && <Loading />}
        {err && <div className="text-red-600 font-semibold">Error: {err}</div>}
        {!loading && !err && (
          <div className="text-5xl font-black text-poli-navy">{activos ?? "-"}</div>
        )}
      </div>
    </div>
  );
}
