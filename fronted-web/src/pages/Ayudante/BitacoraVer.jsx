import React, { useEffect, useState } from "react";
import { apiGet } from "../../lib/api";
import { useParams } from "react-router-dom";
import Loading from "../../components/Loading.jsx";
import Table from "../../components/Table.jsx";
import Toast from "../../components/Toast.jsx";

export default function AyuBitacoraVer() {
  const { id } = useParams();

  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });
  const [data, setData] = useState(null); // { bitacora, semanas }

  async function load() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${id}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      setData({ bitacora, semanas });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [id]);

  const flatRows = [];
  for (const semana of data?.semanas || []) {
    const acts = Array.isArray(semana.actividades) ? semana.actividades : [];
    if (!acts.length) {
      flatRows.push({
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
        flatRows.push({
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

  const cols = [
    { key: "semana", label: "Semana" },
    { key: "actSemana", label: "Actividades Semana" },
    { key: "obs", label: "Observaciones" },
    { key: "anexos", label: "Anexos" },
    { key: "actividad", label: "Actividad" },
    { key: "ini", label: "Inicio" },
    { key: "sal", label: "Salida" },
    { key: "hrs", label: "Horas" }
  ];

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Ver Bitácora</div>
          <div className="text-sm text-gray-500">
            BitácoraId: <b>{id}</b> | Estado: <b>{data?.bitacora?.estado ?? "-"}</b>
          </div>
        </div>

        <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
          Refrescar
        </button>
      </div>

      {loading && <Loading />}
      {!loading && <Table columns={cols} rows={flatRows} />}

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}
