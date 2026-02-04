import React, { useEffect, useState } from "react";
import { apiGet, apiPost, apiPut } from "../../lib/api"; // ✅ CORREGIDO: apiPut agregado
import Modal from "../../components/Modal.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Table from "../../components/Table.jsx";
import { useNavigate } from "react-router-dom";

export default function AyuBitacoraActual() {
  const nav = useNavigate();

  const [bitacoraId, setBitacoraId] = useState("");
  const [data, setData] = useState(null);
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

  // ✅ Modal para EDITAR actividad
  const [openEditAct, setOpenEditAct] = useState(false);
  const [actToEdit, setActToEdit] = useState(null);

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
    // ✅ Solo permitir si NO está aprobada
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }

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
      setToast({ msg: "⚠️ Completa fechas y actividades realizadas", kind: "bad" });
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
      setToast({ msg: "✅ Semana creada", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  function abrirActividad(semanaId) {
    // ✅ Solo permitir si NO está aprobada
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }

    setTargetSemanaId(String(semanaId));
    setActForm({ horaInicio: "", horaSalida: "", descripcion: "" });
    setOpenAct(true);
  }

  async function crearActividad() {
    if (!targetSemanaId) return;

    const required = [actForm.horaInicio, actForm.horaSalida, actForm.descripcion];
    if (required.some((x) => !String(x || "").trim())) {
      setToast({ msg: "⚠️ Completa todos los campos", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/ayudante/semanas/${targetSemanaId}/actividades`, {
        horaInicio: actForm.horaInicio.trim(),
        horaSalida: actForm.horaSalida.trim(),
        descripcion: actForm.descripcion.trim()
      });

      setOpenAct(false);
      setToast({ msg: "✅ Actividad creada", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  // ✅ EDITAR ACTIVIDAD
  function abrirEditarActividad(actividad) {
    // ✅ Solo permitir si NO está aprobada
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }

    setActToEdit(actividad);
    setActForm({
      horaInicio: actividad.horaInicio || "",
      horaSalida: actividad.horaSalida || "",
      descripcion: actividad.descripcion || ""
    });
    setOpenEditAct(true);
  }

  async function editarActividad() {
    if (!actToEdit?.actividadId) {
      setToast({ msg: "⚠️ No hay actividad para editar", kind: "bad" });
      return;
    }

    const required = [actForm.horaInicio, actForm.horaSalida, actForm.descripcion];
    if (required.some((x) => !String(x || "").trim())) {
      setToast({ msg: "⚠️ Completa todos los campos", kind: "bad" });
      return;
    }

    try {
      await apiPut(`/api/v1/ayudante/actividades/${actToEdit.actividadId}`, {
        horaInicio: actForm.horaInicio.trim(),
        horaFin: actForm.horaSalida.trim(),
        descripcion: actForm.descripcion.trim()
      });

      setOpenEditAct(false);
      setToast({ msg: "✅ Actividad actualizada correctamente", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  async function enviar() {
    if (!bitacoraId) return;

    // ✅ Solo permitir si está en BORRADOR o RECHAZADA
    const estado = data?.bitacora?.estado;
    if (estado === "APROBADA") {
      setToast({ msg: "⚠️ Esta bitácora ya fue APROBADA", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/enviar`, {});
      setToast({ msg: "✅ Bitácora enviada al director", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  const expandedRows = [];
  for (const semana of data?.semanas || []) {
    const acts = Array.isArray(semana.actividades) ? semana.actividades : [];
    
    expandedRows.push({
      type: 'semana',
      semanaId: semana.semanaId || semana.id,
      fechaInicioSemana: semana.fechaInicioSemana,
      fechaFinSemana: semana.fechaFinSemana,
      actividadesRealizadas: semana.actividadesRealizadas,
      observaciones: semana.observaciones,
      anexos: semana.anexos,
      numActividades: acts.length
    });

    for (const act of acts) {
      expandedRows.push({
        type: 'actividad',
        semanaId: semana.semanaId || semana.id,
        ...act
      });
    }
  }

  const cols = [
    { 
      key: "rango", 
      label: "Semana / Actividad",
      render: (r) => {
        if (r.type === 'semana') {
          return (
            <div className="font-bold text-poli-navy">
              📅 {r.fechaInicioSemana} - {r.fechaFinSemana}
              <div className="text-xs text-gray-500 font-normal mt-1">
                {r.numActividades} actividad(es)
              </div>
            </div>
          );
        } else {
          return (
            <div className="pl-6 text-sm text-gray-600">
              🕒 {r.horaInicio} - {r.horaSalida} ({r.totalHoras})
            </div>
          );
        }
      }
    },
    { 
      key: "content", 
      label: "Descripción",
      render: (r) => {
        if (r.type === 'semana') {
          return (
            <div>
              <div className="font-semibold text-sm mb-1">Actividades de la semana:</div>
              <div className="text-sm">{r.actividadesRealizadas}</div>
              {r.observaciones && (
                <div className="text-xs text-gray-600 mt-2">
                  <strong>Obs:</strong> {r.observaciones}
                </div>
              )}
            </div>
          );
        } else {
          return <div className="pl-6 text-sm">{r.descripcion}</div>;
        }
      }
    },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => {
        const esAprobada = data?.bitacora?.estado === "APROBADA";
        
        if (r.type === 'semana') {
          return (
            <button
              disabled={esAprobada}
              className={`rounded-xl px-3 py-2 text-white font-bold text-sm ${
                esAprobada 
                  ? 'bg-gray-400 cursor-not-allowed' 
                  : 'bg-poli-navy hover:bg-blue-900'
              }`}
              onClick={() => abrirActividad(r.semanaId)}
            >
              ➕ Actividad
            </button>
          );
        } else {
          return (
            <button
              disabled={esAprobada}
              className={`rounded-xl px-3 py-2 text-white font-bold text-sm ${
                esAprobada 
                  ? 'bg-gray-400 cursor-not-allowed' 
                  : 'bg-amber-500 hover:bg-amber-600'
              }`}
              onClick={() => abrirEditarActividad(r)}
            >
              ✏️ Editar
            </button>
          );
        }
      }
    }
  ];

  const estadoBadge = data?.bitacora?.estado === "APROBADA" ? (
    <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-bold">✅ APROBADA</span>
  ) : data?.bitacora?.estado === "RECHAZADA" ? (
    <span className="px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm font-bold">❌ RECHAZADA</span>
  ) : data?.bitacora?.estado === "PENDIENTE" ? (
    <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-bold">⏳ PENDIENTE</span>
  ) : (
    <span className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm font-bold">📝 BORRADOR</span>
  );

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div>
          <div className="text-lg font-bold text-poli-ink">Bitácora mensual actual</div>
          <div className="text-sm text-gray-500 flex items-center gap-2">
            BitácoraId: <b>{bitacoraId || "-"}</b>
            <span className="mx-2">|</span>
            Estado: {estadoBadge}
          </div>
        </div>

        <div className="flex gap-2 flex-wrap">
          <button onClick={() => load()} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            🔄 Refrescar
          </button>
          <button 
            onClick={abrirSemana} 
            disabled={data?.bitacora?.estado === "APROBADA"}
            className={`rounded-xl px-4 py-2 text-white font-bold ${
              data?.bitacora?.estado === "APROBADA"
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-poli-navy hover:bg-blue-900'
            }`}
          >
            ➕ Semana
          </button>
          <button
            onClick={() => nav(`/ayudante/historial`)}
            className="rounded-xl px-4 py-2 bg-emerald-600 text-white font-bold hover:bg-emerald-700"
          >
            📚 Ver Historial
          </button>
        </div>
      </div>

      {loading && <Loading />}
      {!loading && <Table columns={cols} rows={expandedRows} />}

      {/* Modales */}
      <Modal open={openSemana} title="➕ Crear semana" onClose={() => setOpenSemana(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha inicio</label>
            <input type="date" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={semanaForm.fechaInicioSemana}
              onChange={(e) => setSemanaForm({ ...semanaForm, fechaInicioSemana: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha fin</label>
            <input type="date" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={semanaForm.fechaFinSemana}
              onChange={(e) => setSemanaForm({ ...semanaForm, fechaFinSemana: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Actividades realizadas *</label>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[90px]"
              value={semanaForm.actividadesRealizadas}
              onChange={(e) => setSemanaForm({ ...semanaForm, actividadesRealizadas: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Observaciones</label>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[70px]"
              value={semanaForm.observaciones}
              onChange={(e) => setSemanaForm({ ...semanaForm, observaciones: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Anexos</label>
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
            ✅ Crear
          </button>
        </div>
      </Modal>

      <Modal open={openAct} title="➕ Crear actividad" onClose={() => setOpenAct(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora inicio (HH:mm)</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaInicio}
              onChange={(e) => setActForm({ ...actForm, horaInicio: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora salida (HH:mm)</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaSalida}
              onChange={(e) => setActForm({ ...actForm, horaSalida: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Descripción *</label>
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
            ✅ Crear
          </button>
        </div>
      </Modal>

      <Modal open={openEditAct} title="✏️ Editar actividad" onClose={() => setOpenEditAct(false)}>
        <div className="mb-4 p-3 bg-amber-50 rounded-xl border border-amber-200">
          <p className="text-sm text-amber-800">
            <strong>📝 Editando:</strong> Actividad ID {actToEdit?.actividadId}
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora inicio (HH:mm)</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaInicio}
              onChange={(e) => setActForm({ ...actForm, horaInicio: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora salida (HH:mm)</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaSalida}
              onChange={(e) => setActForm({ ...actForm, horaSalida: e.target.value })}
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Descripción *</label>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[90px]"
              value={actForm.descripcion}
              onChange={(e) => setActForm({ ...actForm, descripcion: e.target.value })}
            />
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <button onClick={() => setOpenEditAct(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={editarActividad} className="rounded-xl px-4 py-2 bg-amber-500 text-white font-bold hover:bg-amber-600">
            💾 Guardar cambios
          </button>
        </div>
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}