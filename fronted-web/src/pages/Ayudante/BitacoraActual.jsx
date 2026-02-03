import React, { useEffect, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Modal from "../../components/Modal.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Table from "../../components/Table.jsx";
import { useNavigate } from "react-router-dom";

export default function AyuBitacoraActual() {
  const nav = useNavigate();

  const [bitacoraId, setBitacoraId] = useState("");
  const [data, setData] = useState(null); // { bitacora, semanas }
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  const [openSemana, setOpenSemana] = useState(false);
  const [semanaForm, setSemanaForm] = useState({
    fechaInicioSemana: "",
    fechaFinSemana: "",
    actividadesRealizadas: "",
    observaciones: "",
    anexos: "-"
  });

  const [openAct, setOpenAct] = useState(false);
  const [targetSemanaId, setTargetSemanaId] = useState("");
  const [actForm, setActForm] = useState({
    horaInicio: "",
    horaSalida: "",
    descripcion: ""
  });

  async function init() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiPost("/api/v1/ayudante/bitacoras/actual", {});
      const ok = res?.ok === true;
      const id = res?.bitacoraId;

      if (!ok || !id) throw new Error(res?.msg || "No pude obtener la bitácora actual");

      setBitacoraId(id);
      await load(id);
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  async function load(id = bitacoraId) {
    if (!id) return;
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${id}`);
      // puede venir { ok:true, bitacora:{}, semanas:[...] } o similar
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      setData({ bitacora, semanas });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { init(); }, []);

  function abrirSemana() {
    setSemanaForm({
      fechaInicioSemana: "",
      fechaFinSemana: "",
      actividadesRealizadas: "",
      observaciones: "",
      anexos: "-"
    });
    setOpenSemana(true);
  }

  async function crearSemana() {
    if (!bitacoraId) return;

    const required = [
      semanaForm.fechaInicioSemana,
      semanaForm.fechaFinSemana,
      semanaForm.actividadesRealizadas
    ];
    if (required.some((x) => !String(x || "").trim())) {
      setToast({ msg: "Completa fechas y actividades realizadas", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/semanas`, {
        fechaInicioSemana: semanaForm.fechaInicioSemana.trim(),
        fechaFinSemana: semanaForm.fechaFinSemana.trim(),
        actividadesRealizadas: semanaForm.actividadesRealizadas.trim(),
        observaciones: (semanaForm.observaciones || "").trim(),
        anexos: (semanaForm.anexos || "-").trim()
      });

      setOpenSemana(false);
      setToast({ msg: "Semana creada", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  function abrirActividad(semanaId) {
    setTargetSemanaId(String(semanaId));
    setActForm({ horaInicio: "", horaSalida: "", descripcion: "" });
    setOpenAct(true);
  }

  async function crearActividad() {
    if (!targetSemanaId) return;

    const required = [actForm.horaInicio, actForm.horaSalida, actForm.descripcion];
    if (required.some((x) => !String(x || "").trim())) {
      setToast({ msg: "Completa horaInicio, horaSalida y descripción", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/ayudante/semanas/${targetSemanaId}/actividades`, {
        horaInicio: actForm.horaInicio.trim(),
        horaSalida: actForm.horaSalida.trim(),
        descripcion: actForm.descripcion.trim()
      });

      setOpenAct(false);
      setToast({ msg: "Actividad creada", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  async function enviar() {
    if (!bitacoraId) return;
    try {
      await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/enviar`, {});
      setToast({ msg: "Bitácora enviada", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  const semanas = data?.semanas || [];

  const cols = [
    { key: "id", label: "SemanaId", render: (r) => r.semanaId || r.id || "-" },
    { key: "rango", label: "Semana", render: (r) => `${r.fechaInicioSemana || ""} - ${r.fechaFinSemana || ""}`.trim() },
    { key: "actividadesRealizadas", label: "Actividades Semana" },
    { key: "observaciones", label: "Observaciones" },
    { key: "anexos", label: "Anexos" },
    {
      key: "_act",
      label: "Acciones",
      render: (r) => (
        <button
          className="rounded-xl px-3 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
          onClick={() => abrirActividad(r.semanaId || r.id)}
        >
          + Actividad
        </button>
      )
    }
  ];

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Bitácora mensual actual</div>
          <div className="text-sm text-gray-500">
            BitácoraId: <b>{bitacoraId || "-"}</b>{" "}
            <span className="mx-2">|</span>
            Estado: <b>{data?.bitacora?.estado ?? "-"}</b>
          </div>
        </div>

        <div className="flex gap-2">
          <button onClick={() => load()} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Refrescar
          </button>
          <button onClick={abrirSemana} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
            + Semana
          </button>
          <button onClick={enviar} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
            Enviar
          </button>
          <button
            onClick={() => nav(`/ayudante/bitacora/${bitacoraId}`)}
            className="rounded-xl px-4 py-2 bg-emerald-600 text-white font-bold"
          >
            Ver completo
          </button>
        </div>
      </div>

      {loading && <Loading />}
      {!loading && <Table columns={cols} rows={semanas} />}

      <Modal open={openSemana} title="Crear semana" onClose={() => setOpenSemana(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <div className="text-sm text-gray-600">Fecha inicio semana</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="2026-01-05"
              value={semanaForm.fechaInicioSemana}
              onChange={(e) => setSemanaForm({ ...semanaForm, fechaInicioSemana: e.target.value })}
            />
          </div>
          <div>
            <div className="text-sm text-gray-600">Fecha fin semana</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="2026-01-09"
              value={semanaForm.fechaFinSemana}
              onChange={(e) => setSemanaForm({ ...semanaForm, fechaFinSemana: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-gray-600">Actividades realizadas</div>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[90px]"
              value={semanaForm.actividadesRealizadas}
              onChange={(e) => setSemanaForm({ ...semanaForm, actividadesRealizadas: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-gray-600">Observaciones</div>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[70px]"
              value={semanaForm.observaciones}
              onChange={(e) => setSemanaForm({ ...semanaForm, observaciones: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-gray-600">Anexos</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              value={semanaForm.anexos}
              onChange={(e) => setSemanaForm({ ...semanaForm, anexos: e.target.value })}
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={() => setOpenSemana(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={crearSemana} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
            Crear semana
          </button>
        </div>
      </Modal>

      <Modal open={openAct} title="Crear actividad" onClose={() => setOpenAct(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <div className="text-sm text-gray-600">Hora inicio (HH:mm)</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="08:00"
              value={actForm.horaInicio}
              onChange={(e) => setActForm({ ...actForm, horaInicio: e.target.value })}
            />
          </div>
          <div>
            <div className="text-sm text-gray-600">Hora salida (HH:mm)</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="10:00"
              value={actForm.horaSalida}
              onChange={(e) => setActForm({ ...actForm, horaSalida: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-gray-600">Descripción</div>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[90px]"
              value={actForm.descripcion}
              onChange={(e) => setActForm({ ...actForm, descripcion: e.target.value })}
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={() => setOpenAct(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={crearActividad} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
            Crear actividad
          </button>
        </div>
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}
