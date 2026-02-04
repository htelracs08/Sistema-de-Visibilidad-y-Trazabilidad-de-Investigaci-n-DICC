import React, { useEffect, useState } from "react";
import { apiGet, apiPost, apiPut } from "../../lib/api";
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

  const [openEditAct, setOpenEditAct] = useState(false);
  const [actToEdit, setActToEdit] = useState(null);

  async function init() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      console.log("📡 Obteniendo bitácora actual...");
      const res = await apiPost("/api/v1/ayudante/bitacoras/actual", {});
      const ok = res?.ok === true;
      const id = res?.bitacoraId;

      if (!ok || !id) {
        throw new Error(res?.msg || "No pude obtener la bitácora actual");
      }

      console.log("✅ Bitácora actual obtenida:", id);
      setBitacoraId(id);
      await load(id);
    } catch (e) {
      console.error("❌ Error obteniendo bitácora actual:", e);
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
      console.log("📡 Cargando detalles de bitácora:", id);
      const res = await apiGet(`/api/v1/ayudante/bitacoras/${id}`);
      const bitacora = res?.bitacora ?? res;
      const semanas = Array.isArray(res?.semanas) ? res.semanas : [];
      console.log("✅ Bitácora cargada:", { estado: bitacora?.estado, semanasCount: semanas.length });
      setData({ bitacora, semanas });
    } catch (e) {
      console.error("❌ Error cargando bitácora:", e);
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { init(); }, []);

  function abrirSemana() {
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }
    if (data?.bitacora?.estado === "PENDIENTE") {
      setToast({ msg: "⚠️ Esta bitácora está pendiente de revisión. No se puede modificar.", kind: "bad" });
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
      console.log("📤 Creando semana en bitácora:", bitacoraId);
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
      console.error("❌ Error creando semana:", e);
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  function abrirActividad(semanaId) {
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }
    if (data?.bitacora?.estado === "PENDIENTE") {
      setToast({ msg: "⚠️ Esta bitácora está pendiente de revisión. No se puede modificar.", kind: "bad" });
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
      console.log("📤 Creando actividad en semana:", targetSemanaId);
      await apiPost(`/api/v1/ayudante/semanas/${targetSemanaId}/actividades`, {
        horaInicio: actForm.horaInicio.trim(),
        horaSalida: actForm.horaSalida.trim(),
        descripcion: actForm.descripcion.trim()
      });

      setOpenAct(false);
      setToast({ msg: "✅ Actividad creada", kind: "ok" });
      await load();
    } catch (e) {
      console.error("❌ Error creando actividad:", e);
      setToast({ msg: e.message, kind: "bad" });
    }
  }

  function abrirEditarActividad(actividad) {
    if (data?.bitacora?.estado === "APROBADA") {
      setToast({ msg: "⚠️ No puedes modificar una bitácora APROBADA", kind: "bad" });
      return;
    }
    if (data?.bitacora?.estado === "PENDIENTE") {
      setToast({ msg: "⚠️ Esta bitácora está pendiente de revisión. No se puede modificar.", kind: "bad" });
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
      console.log("📤 Editando actividad:", actToEdit.actividadId);
      await apiPut(`/api/v1/ayudante/actividades/${actToEdit.actividadId}`, {
        horaInicio: actForm.horaInicio.trim(),
        horaFin: actForm.horaSalida.trim(),
        descripcion: actForm.descripcion.trim()
      });

      setOpenEditAct(false);
      setToast({ msg: "✅ Actividad actualizada correctamente", kind: "ok" });
      await load();
    } catch (e) {
      console.error("❌ Error editando actividad:", e);
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  // ✅ FUNCIÓN ENVIAR - CRÍTICA
  async function enviar() {
    if (!bitacoraId) {
      setToast({ msg: "⚠️ No hay bitácora para enviar", kind: "bad" });
      return;
    }

    const estado = data?.bitacora?.estado;
    
    if (estado === "APROBADA") {
      setToast({ msg: "⚠️ Esta bitácora ya fue APROBADA", kind: "bad" });
      return;
    }

    if (estado === "PENDIENTE") {
      setToast({ msg: "⚠️ Esta bitácora ya está PENDIENTE de revisión", kind: "bad" });
      return;
    }

    // Validar que tenga al menos una semana
    if (!data?.semanas || data.semanas.length === 0) {
      setToast({ msg: "⚠️ Debes agregar al menos una semana antes de enviar", kind: "bad" });
      return;
    }

    const confirmar = window.confirm(
      "¿Estás seguro de enviar esta bitácora al director?\n\n" +
      "Una vez enviada, no podrás modificarla hasta que el director la revise.\n\n" +
      `Semanas registradas: ${data.semanas.length}`
    );
    
    if (!confirmar) return;

    try {
      console.log("📤 Enviando bitácora al director:", bitacoraId);
      setLoading(true);
      
      await apiPost(`/api/v1/ayudante/bitacoras/${bitacoraId}/enviar`, {});
      
      setToast({ msg: "✅ Bitácora enviada al director correctamente", kind: "ok" });
      
      // Recargar para ver el nuevo estado
      await load();
    } catch (e) {
      console.error("❌ Error enviando bitácora:", e);
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    } finally {
      setLoading(false);
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
        const puedeEditar = data?.bitacora?.estado !== "APROBADA" && data?.bitacora?.estado !== "PENDIENTE";
        
        if (r.type === 'semana') {
          return (
            <button
              disabled={!puedeEditar}
              className={`rounded-xl px-3 py-2 text-white font-bold text-sm ${
                puedeEditar
                  ? 'bg-poli-navy hover:bg-blue-900'
                  : 'bg-gray-400 cursor-not-allowed' 
              }`}
              onClick={() => abrirActividad(r.semanaId)}
            >
              ➕ Actividad
            </button>
          );
        } else {
          return (
            <button
              disabled={!puedeEditar}
              className={`rounded-xl px-3 py-2 text-white font-bold text-sm ${
                puedeEditar
                  ? 'bg-amber-500 hover:bg-amber-600'
                  : 'bg-gray-400 cursor-not-allowed' 
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

  // ✅ Determinar permisos
  const puedeEnviar = (data?.bitacora?.estado === "BORRADOR" || data?.bitacora?.estado === "RECHAZADA") && 
                      data?.semanas && data.semanas.length > 0;
  const puedeModificar = data?.bitacora?.estado !== "APROBADA" && data?.bitacora?.estado !== "PENDIENTE";

  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      
      {/* ============ HEADER CON INFORMACIÓN Y BOTONES ============ */}
      <div className="flex flex-col gap-4">
        <div className="flex flex-col md:flex-row gap-3 md:items-start md:justify-between">
          <div className="flex-1">
            <div className="text-lg font-bold text-poli-ink">Bitácora mensual actual</div>
            <div className="text-sm text-gray-500 flex items-center gap-2 mt-1">
              BitácoraId: <b>{bitacoraId || "-"}</b>
              <span className="mx-2">|</span>
              Estado: {estadoBadge}
            </div>
            
            {/* ✅ Mensajes informativos */}
            {data?.bitacora?.estado === "APROBADA" && (
              <div className="mt-3 p-3 bg-green-50 rounded-xl border border-green-200">
                <div className="flex items-start gap-2">
                  <span className="text-xl">✅</span>
                  <div>
                    <div className="font-semibold text-green-800">Bitácora Aprobada</div>
                    <div className="text-sm text-green-700 mt-1">
                      Esta bitácora ya fue aprobada por el director. No se pueden realizar modificaciones.
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            {data?.bitacora?.estado === "PENDIENTE" && (
              <div className="mt-3 p-3 bg-blue-50 rounded-xl border border-blue-200">
                <div className="flex items-start gap-2">
                  <span className="text-xl">⏳</span>
                  <div>
                    <div className="font-semibold text-blue-800">Pendiente de Revisión</div>
                    <div className="text-sm text-blue-700 mt-1">
                      Esta bitácora está siendo revisada por el director. No se pueden realizar modificaciones hasta que sea aprobada o rechazada.
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            {data?.bitacora?.estado === "RECHAZADA" && (
              <div className="mt-3 p-3 bg-red-50 rounded-xl border border-red-200">
                <div className="flex items-start gap-2">
                  <span className="text-xl">❌</span>
                  <div>
                    <div className="font-semibold text-red-800">Bitácora Rechazada</div>
                    <div className="text-sm text-red-700 mt-1">
                      Esta bitácora fue rechazada. Realiza las correcciones necesarias y vuelve a enviarla.
                    </div>
                  </div>
                </div>
              </div>
            )}

            {data?.bitacora?.estado === "BORRADOR" && (!data?.semanas || data.semanas.length === 0) && (
              <div className="mt-3 p-3 bg-amber-50 rounded-xl border border-amber-200">
                <div className="flex items-start gap-2">
                  <span className="text-xl">📝</span>
                  <div>
                    <div className="font-semibold text-amber-800">Bitácora en Borrador</div>
                    <div className="text-sm text-amber-700 mt-1">
                      Agrega al menos una semana con sus actividades antes de enviar la bitácora al director.
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* ✅ BOTONES DE ACCIÓN */}
          <div className="flex gap-2 flex-wrap">
            <button 
              onClick={() => load()} 
              className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold transition-all"
            >
              🔄 Refrescar
            </button>
            
            {puedeModificar && (
              <button 
                onClick={abrirSemana} 
                className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold hover:bg-blue-900 transition-all"
              >
                ➕ Semana
              </button>
            )}

            {/* ✅ BOTÓN ENVIAR - MUY VISIBLE */}
            {puedeEnviar && (
              <button
                onClick={enviar}
                disabled={loading}
                className="rounded-xl px-5 py-2.5 bg-gradient-to-r from-emerald-600 to-emerald-700 text-white font-bold hover:shadow-xl transition-all flex items-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
              >
                <span className="text-lg">📤</span>
                <span>Enviar al Director</span>
              </button>
            )}

            <button
              onClick={() => nav(`/ayudante/historial`)}
              className="rounded-xl px-4 py-2 bg-purple-600 text-white font-bold hover:bg-purple-700 transition-all"
            >
              📚 Ver Historial
            </button>
          </div>
        </div>
      </div>

      {/* ============ TABLA DE CONTENIDO ============ */}
      {loading && <Loading />}
      {!loading && <Table columns={cols} rows={expandedRows} />}

      {/* ============ MODALES ============ */}
      
      {/* Modal: Crear Semana */}
      <Modal open={openSemana} title="➕ Crear semana" onClose={() => setOpenSemana(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha inicio *</label>
            <input type="date" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={semanaForm.fechaInicioSemana}
              onChange={(e) => setSemanaForm({ ...semanaForm, fechaInicioSemana: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha fin *</label>
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
              placeholder="Describe las actividades realizadas durante esta semana..."
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Observaciones</label>
            <textarea className="mt-1 w-full rounded-xl border px-3 py-2 min-h-[70px]"
              value={semanaForm.observaciones}
              onChange={(e) => setSemanaForm({ ...semanaForm, observaciones: e.target.value })}
              placeholder="Observaciones adicionales (opcional)"
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Anexos</label>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              value={semanaForm.anexos}
              onChange={(e) => setSemanaForm({ ...semanaForm, anexos: e.target.value })}
              placeholder="Enlaces o referencias a documentos"
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={() => setOpenSemana(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={crearSemana} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold hover:bg-red-700">
            ✅ Crear Semana
          </button>
        </div>
      </Modal>

      {/* Modal: Crear Actividad */}
      <Modal open={openAct} title="➕ Crear actividad" onClose={() => setOpenAct(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora inicio (HH:mm) *</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaInicio}
              onChange={(e) => setActForm({ ...actForm, horaInicio: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora salida (HH:mm) *</label>
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
              placeholder="Describe detalladamente la actividad realizada..."
            />
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={() => setOpenAct(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={crearActividad} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold hover:bg-red-700">
            ✅ Crear Actividad
          </button>
        </div>
      </Modal>

      {/* Modal: Editar Actividad */}
      <Modal open={openEditAct} title="✏️ Editar actividad" onClose={() => setOpenEditAct(false)}>
        <div className="mb-4 p-3 bg-amber-50 rounded-xl border border-amber-200">
          <p className="text-sm text-amber-800">
            <strong>📝 Editando:</strong> Actividad ID {actToEdit?.actividadId}
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora inicio (HH:mm) *</label>
            <input type="time" className="mt-1 w-full rounded-xl border px-3 py-2"
              value={actForm.horaInicio}
              onChange={(e) => setActForm({ ...actForm, horaInicio: e.target.value })}
            />
          </div>
          <div>
            <label className="text-sm text-gray-600 font-semibold">Hora salida (HH:mm) *</label>
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