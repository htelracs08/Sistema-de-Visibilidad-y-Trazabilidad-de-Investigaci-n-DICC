// import React, { useEffect, useMemo, useState } from "react";
// import { apiGet, apiPost } from "../../lib/api";
// import Table from "../../components/Table.jsx";
// import Loading from "../../components/Loading.jsx";
// import Modal from "../../components/Modal.jsx";
// import Toast from "../../components/Toast.jsx";
// import { getDirectorSelectedProject } from "../../lib/state";

// const TIPOS_AYUDANTE = [
//   "ASISTENTE_INVESTIGACION",
//   "AYUDANTE_INVESTIGACION",
//   "TECNICO_INVESTIGACION"
// ];

// export default function DirAyudantes() {
//   const selected = getDirectorSelectedProject();

//   const [rows, setRows] = useState([]);
//   const [loading, setLoading] = useState(false);
//   const [toast, setToast] = useState({ msg: "", kind: "info" });

//   const [openReg, setOpenReg] = useState(false);
//   const [form, setForm] = useState({
//     nombres: "",
//     apellidos: "",
//     correoInstitucional: "",
//     facultad: "",
//     quintil: "",
//     tipoAyudante: "",
//     fechaInicioContrato: "",
//     fechaFinContrato: ""
//   });

//   async function load() {
//     if (!selected?.id) {
//       setToast({ msg: "Primero selecciona un proyecto en la pestaña Proyectos.", kind: "bad" });
//       return;
//     }

//     setLoading(true);
//     setToast({ msg: "", kind: "info" });
//     try {
//       const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/ayudantes`);
//       const arr = Array.isArray(res) ? res : (res?.items ?? []);
//       setRows(arr);
//     } catch (e) {
//       setToast({ msg: e.message, kind: "bad" });
//     } finally {
//       setLoading(false);
//     }
//   }

//   useEffect(() => { load(); }, [selected?.id]);

//   const columns = [
//     { key: "contratoId", label: "ContratoId" },
//     { key: "ayudanteId", label: "AyudanteId" },
//     { key: "correoInstitucional", label: "Correo" },
//     { key: "nombres", label: "Nombres" },
//     { key: "apellidos", label: "Apellidos" },
//     { key: "estado", label: "Estado" },
//     { key: "fechaInicio", label: "Inicio" },
//     { key: "fechaFin", label: "Fin" },
//     { key: "facultad", label: "Facultad" },
//     { key: "quintil", label: "Quintil" },
//     { key: "tipoAyudante", label: "TipoAyudante" },
//     {
//       key: "_actions",
//       label: "Acciones",
//       render: (r) => (
//         <button
//           className="rounded-xl px-3 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
//           onClick={() => finalizar(r.contratoId)}
//         >
//           Finalizar
//         </button>
//       )
//     }
//   ];

//   function abrirRegistrar() {
//     setForm({
//       nombres: "",
//       apellidos: "",
//       correoInstitucional: "",
//       facultad: "",
//       quintil: "",
//       tipoAyudante: "",
//       fechaInicioContrato: "",
//       fechaFinContrato: ""
//     });
//     setOpenReg(true);
//   }

//   async function registrar() {
//     if (!selected?.id) return;

//     const required = [
//       form.nombres, form.apellidos, form.correoInstitucional,
//       form.facultad, form.quintil, form.tipoAyudante,
//       form.fechaInicioContrato, form.fechaFinContrato
//     ];
//     if (required.some((x) => !String(x || "").trim())) {
//       setToast({ msg: "Todos los campos son requeridos", kind: "bad" });
//       return;
//     }

//     const q = Number(form.quintil);
//     if (isNaN(q) || q < 1 || q > 5) {
//       setToast({ msg: "El quintil debe estar entre 1 y 5", kind: "bad" });
//       return;
//     }

//     const body = {
//       nombres: form.nombres.trim(),
//       apellidos: form.apellidos.trim(),
//       correoInstitucional: form.correoInstitucional.trim().toLowerCase(),
//       facultad: form.facultad.trim(),
//       quintil: q,
//       tipoAyudante: form.tipoAyudante.trim(),
//       fechaInicioContrato: form.fechaInicioContrato.trim(),
//       fechaFinContrato: form.fechaFinContrato.trim()
//     };

//     try {
//       await apiPost(`/api/v1/director/proyectos/${selected.id}/ayudantes`, body);
//       setOpenReg(false);
//       setToast({ msg: "Ayudante registrado correctamente", kind: "ok" });
//       await load();
//     } catch (e) {
//       setToast({ msg: e.message, kind: "bad" });
//     }
//   }

//   async function finalizar(contratoId) {
//     if (!contratoId) return;

//     const motivo = prompt("Motivo (RENUNCIA / FIN_CONTRATO / DESPIDO):", "FIN_CONTRATO");
//     if (!motivo) return;

//     try {
//       await apiPost(`/api/v1/director/contratos/${contratoId}/finalizar`, { motivo });
//       setToast({ msg: "Contrato finalizado correctamente", kind: "ok" });
//       await load();
//     } catch (e) {
//       setToast({ msg: e.message, kind: "bad" });
//     }
//   }

//   const header = selected?.id
//     ? `Proyecto seleccionado: ${selected.codigo} - ${selected.nombre}`
//     : "No hay proyecto seleccionado";

//   return (
//     <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
//       <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
//         <div>
//           <div className="text-lg font-bold text-poli-ink">Ayudantes</div>
//           <div className="text-sm text-gray-500">{header}</div>
//         </div>

//         <div className="flex gap-2">
//           <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
//             Refrescar
//           </button>
//           <button onClick={abrirRegistrar} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
//             Registrar Ayudante
//           </button>
//         </div>
//       </div>

//       {loading ? <Loading /> : <Table columns={columns} rows={rows} />}

//       <Modal open={openReg} title="Registrar Ayudante" onClose={() => setOpenReg(false)}>
//         <div className="grid md:grid-cols-2 gap-3">
//           <div>
//             <div className="text-sm text-gray-600">Nombres</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.nombres} onChange={(e) => setForm({ ...form, nombres: e.target.value })} />
//           </div>
//           <div>
//             <div className="text-sm text-gray-600">Apellidos</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.apellidos} onChange={(e) => setForm({ ...form, apellidos: e.target.value })} />
//           </div>

//           <div className="md:col-span-2">
//             <div className="text-sm text-gray-600">Correo Institucional</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.correoInstitucional} onChange={(e) => setForm({ ...form, correoInstitucional: e.target.value })} />
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Facultad</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.facultad} onChange={(e) => setForm({ ...form, facultad: e.target.value })} />
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Quintil (1-5)</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.quintil} onChange={(e) => setForm({ ...form, quintil: e.target.value })} />
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Tipo Ayudante</div>
//             <select className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.tipoAyudante} onChange={(e) => setForm({ ...form, tipoAyudante: e.target.value })}>
//               <option value="">Seleccione...</option>
//               {TIPOS_AYUDANTE.map((t) => <option key={t} value={t}>{t}</option>)}
//             </select>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Fecha Inicio Contrato</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               placeholder="2026-01-01"
//               value={form.fechaInicioContrato} onChange={(e) => setForm({ ...form, fechaInicioContrato: e.target.value })} />
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Fecha Fin Contrato</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               placeholder="2026-03-31"
//               value={form.fechaFinContrato} onChange={(e) => setForm({ ...form, fechaFinContrato: e.target.value })} />
//           </div>
//         </div>

//         <div className="mt-4 flex justify-end gap-2">
//           <button onClick={() => setOpenReg(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
//             Cancelar
//           </button>
//           <button onClick={registrar} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
//             Registrar
//           </button>
//         </div>
//       </Modal>

//       <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
//     </div>
//   );
// }






import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Modal from "../../components/Modal.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Badge from "../../components/Badge.jsx";
import { getDirectorSelectedProject } from "../../lib/state";

const TIPOS_AYUDANTE = [
  "ASISTENTE_INVESTIGACION",
  "AYUDANTE_INVESTIGACION", 
  "TECNICO_INVESTIGACION"
];

export default function DirAyudantes() {
  const selected = getDirectorSelectedProject();

  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  // Modal para registrar ayudante
  const [openRegistro, setOpenRegistro] = useState(false);
  const [regForm, setRegForm] = useState({
    nombres: "",
    apellidos: "",
    correoInstitucional: "",
    facultad: "",
    quintil: "",
    tipoAyudante: "",
    fechaInicioContrato: "",
    fechaFinContrato: ""
  });

  // Modal para finalizar contrato
  const [openFinalizar, setOpenFinalizar] = useState(false);
  const [contratoAFinalizar, setContratoAFinalizar] = useState(null);
  const [motivoFinalizacion, setMotivoFinalizacion] = useState("RENUNCIA");

  async function load() {
    if (!selected?.id) {
      setToast({ msg: "⚠️ Primero selecciona un proyecto en la pestaña Proyectos", kind: "warn" });
      return;
    }

    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiGet(`/api/v1/director/proyectos/${selected.id}/ayudantes`);
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(arr);
      setToast({ msg: `✅ ${arr.length} contratos cargados`, kind: "ok" });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { 
    if (selected?.id) load(); 
  }, [selected?.id]);

  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return rows;
    return rows.filter((r) => {
      const txt = [
        r.contratoId, r.correoInstitucional, r.nombres, r.apellidos, 
        r.estado, r.tipoAyudante, r.facultad
      ].join(" ").toLowerCase();
      return txt.includes(s);
    });
  }, [rows, q]);

  const columns = [
    { key: "contratoId", label: "ContratoId" },
    { 
      key: "ayudante", 
      label: "Ayudante",
      render: (r) => (
        <div>
          <div className="font-semibold">{`${r.nombres || ''} ${r.apellidos || ''}`.trim()}</div>
          <div className="text-xs text-gray-500">{r.correoInstitucional}</div>
        </div>
      )
    },
    {
      key: "estado",
      label: "Estado",
      render: (r) => (
        r.estado === "ACTIVO" 
          ? <Badge kind="ok">ACTIVO</Badge> 
          : <Badge kind="bad">INACTIVO</Badge>
      )
    },
    { key: "tipoAyudante", label: "Tipo" },
    { key: "facultad", label: "Facultad" },
    { key: "quintil", label: "Quintil" },
    { 
      key: "periodo", 
      label: "Periodo",
      render: (r) => (
        <div className="text-sm">
          <div>📅 {r.fechaInicio}</div>
          <div>📅 {r.fechaFin}</div>
        </div>
      )
    },
    { key: "motivoInactivo", label: "Motivo Fin" },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        r.estado === "ACTIVO" && (
          <button
            className="rounded-xl px-3 py-2 bg-red-600 text-white hover:bg-red-700 font-bold text-sm"
            onClick={() => abrirFinalizar(r)}
          >
            Finalizar
          </button>
        )
      )
    }
  ];

  function abrirRegistro() {
    if (!selected?.id) {
      setToast({ msg: "⚠️ Primero selecciona un proyecto en Proyectos", kind: "bad" });
      return;
    }

    setRegForm({
      nombres: "",
      apellidos: "",
      correoInstitucional: "",
      facultad: "",
      quintil: "",
      tipoAyudante: "",
      fechaInicioContrato: "",
      fechaFinContrato: ""
    });
    setOpenRegistro(true);
  }

  async function registrarAyudante() {
    const { nombres, apellidos, correoInstitucional, facultad, quintil, tipoAyudante, fechaInicioContrato, fechaFinContrato } = regForm;

    if (!nombres.trim() || !apellidos.trim() || !correoInstitucional.trim() || 
        !facultad.trim() || !quintil || !tipoAyudante || 
        !fechaInicioContrato || !fechaFinContrato) {
      setToast({ msg: "⚠️ Todos los campos son requeridos", kind: "bad" });
      return;
    }

    const q = parseInt(quintil);
    if (isNaN(q) || q < 1 || q > 5) {
      setToast({ msg: "⚠️ El quintil debe estar entre 1 y 5", kind: "bad" });
      return;
    }

    try {
      await apiPost(`/api/v1/director/proyectos/${selected.id}/ayudantes`, {
        nombres: nombres.trim(),
        apellidos: apellidos.trim(),
        correoInstitucional: correoInstitucional.trim().toLowerCase(),
        facultad: facultad.trim(),
        quintil: q,
        tipoAyudante: tipoAyudante,
        fechaInicioContrato: fechaInicioContrato,
        fechaFinContrato: fechaFinContrato
      });

      setOpenRegistro(false);
      setToast({ msg: "✅ Ayudante registrado correctamente", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  function abrirFinalizar(contrato) {
    setContratoAFinalizar(contrato);
    setMotivoFinalizacion("RENUNCIA");
    setOpenFinalizar(true);
  }

  async function finalizarContrato() {
    if (!contratoAFinalizar) return;

    try {
      await apiPost(`/api/v1/director/contratos/${contratoAFinalizar.contratoId}/finalizar`, {
        motivo: motivoFinalizacion
      });

      setOpenFinalizar(false);
      setToast({ msg: "✅ Contrato finalizado correctamente", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  const header = selected?.id
    ? `📁 ${selected.codigo} - ${selected.nombre}`
    : "⚠️ No hay proyecto seleccionado";

  const showContent = Boolean(selected?.id);

  return (
    <div className="space-y-4">
      {/* Card principal */}
      <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
        <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
          <div>
            <div className="text-sm text-gray-500 mb-1">Proyecto actual</div>
            <div className="font-semibold text-poli-ink">{header}</div>
          </div>

          {showContent && (
            <div className="flex gap-2 flex-wrap">
              <input
                className="rounded-xl border px-3 py-2 w-64 outline-none focus:ring-2 focus:ring-poli-navy/30"
                placeholder="🔍 Buscar..."
                value={q}
                onChange={(e) => setQ(e.target.value)}
              />
              <button 
                onClick={load} 
                className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
              >
                🔄 Refrescar
              </button>
              <button 
                onClick={abrirRegistro}
                className="rounded-xl px-4 py-2 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold hover:shadow-lg"
              >
                ➕ Registrar Ayudante
              </button>
            </div>
          )}
        </div>

        {showContent && (
          <div className="mt-4">
            {loading ? <Loading /> : <Table columns={columns} rows={filtered} />}
          </div>
        )}

        {!showContent && (
          <div className="mt-4 p-8 text-center bg-amber-50 rounded-xl border border-amber-200">
            <div className="text-5xl mb-4">⚠️</div>
            <div className="text-lg font-bold text-amber-800">No hay proyecto seleccionado</div>
            <div className="text-amber-600 mt-2">
              Por favor, ve a la pestaña <strong>Proyectos</strong> y selecciona un proyecto primero.
            </div>
          </div>
        )}
      </div>

      {/* Modal: Registrar ayudante */}
      <Modal open={openRegistro} title="➕ Registrar Ayudante" onClose={() => setOpenRegistro(false)}>
        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="text-sm text-gray-600 font-semibold">Nombres *</label>
            <input 
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.nombres}
              onChange={(e) => setRegForm({ ...regForm, nombres: e.target.value })}
              placeholder="Juan Carlos"
            />
          </div>

          <div>
            <label className="text-sm text-gray-600 font-semibold">Apellidos *</label>
            <input 
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.apellidos}
              onChange={(e) => setRegForm({ ...regForm, apellidos: e.target.value })}
              placeholder="Pérez López"
            />
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Correo Institucional *</label>
            <input 
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.correoInstitucional}
              onChange={(e) => setRegForm({ ...regForm, correoInstitucional: e.target.value })}
              placeholder="juan.perez@epn.edu.ec"
            />
          </div>

          <div>
            <label className="text-sm text-gray-600 font-semibold">Facultad *</label>
            <input 
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.facultad}
              onChange={(e) => setRegForm({ ...regForm, facultad: e.target.value })}
              placeholder="FIS"
            />
          </div>

          <div>
            <label className="text-sm text-gray-600 font-semibold">Quintil (1-5) *</label>
            <select
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.quintil}
              onChange={(e) => setRegForm({ ...regForm, quintil: e.target.value })}
            >
              <option value="">Seleccione...</option>
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="3">3</option>
              <option value="4">4</option>
              <option value="5">5</option>
            </select>
          </div>

          <div className="md:col-span-2">
            <label className="text-sm text-gray-600 font-semibold">Tipo de Ayudante *</label>
            <select
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.tipoAyudante}
              onChange={(e) => setRegForm({ ...regForm, tipoAyudante: e.target.value })}
            >
              <option value="">Seleccione...</option>
              {TIPOS_AYUDANTE.map(tipo => (
                <option key={tipo} value={tipo}>{tipo}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha Inicio Contrato *</label>
            <input 
              type="date"
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.fechaInicioContrato}
              onChange={(e) => setRegForm({ ...regForm, fechaInicioContrato: e.target.value })}
            />
          </div>

          <div>
            <label className="text-sm text-gray-600 font-semibold">Fecha Fin Contrato *</label>
            <input 
              type="date"
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={regForm.fechaFinContrato}
              onChange={(e) => setRegForm({ ...regForm, fechaFinContrato: e.target.value })}
            />
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <button 
            onClick={() => setOpenRegistro(false)} 
            className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
          >
            Cancelar
          </button>
          <button 
            onClick={registrarAyudante}
            className="rounded-xl px-4 py-2 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold hover:shadow-lg"
          >
            ✅ Registrar
          </button>
        </div>
      </Modal>

      {/* Modal: Finalizar contrato */}
      <Modal open={openFinalizar} title="⚠️ Finalizar Contrato" onClose={() => setOpenFinalizar(false)}>
        {contratoAFinalizar && (
          <div>
            <div className="mb-4 p-4 bg-amber-50 rounded-xl border border-amber-200">
              <div className="font-semibold text-amber-800">
                {`${contratoAFinalizar.nombres} ${contratoAFinalizar.apellidos}`}
              </div>
              <div className="text-sm text-amber-600">
                {contratoAFinalizar.correoInstitucional}
              </div>
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold">Motivo de finalización</label>
              <select
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={motivoFinalizacion}
                onChange={(e) => setMotivoFinalizacion(e.target.value)}
              >
                <option value="RENUNCIA">RENUNCIA</option>
                <option value="FIN_CONTRATO">FIN_CONTRATO</option>
                <option value="DESPIDO">DESPIDO</option>
              </select>
            </div>

            <div className="mt-6 flex justify-end gap-2">
              <button 
                onClick={() => setOpenFinalizar(false)} 
                className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
              >
                Cancelar
              </button>
              <button 
                onClick={finalizarContrato}
                className="rounded-xl px-4 py-2 bg-red-600 text-white font-bold hover:bg-red-700"
              >
                ⚠️ Finalizar Contrato
              </button>
            </div>
          </div>
        )}
      </Modal>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}