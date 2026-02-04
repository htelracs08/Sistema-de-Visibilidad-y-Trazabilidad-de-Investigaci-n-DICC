// import React, { useEffect, useMemo, useState } from "react";
// import { apiGet, apiPut } from "../../lib/api";
// import Table from "../../components/Table.jsx";
// import Loading from "../../components/Loading.jsx";
// import Toast from "../../components/Toast.jsx";
// import Badge from "../../components/Badge.jsx";
// import { setDirectorSelectedProject, getDirectorSelectedProject } from "../../lib/state";

// export default function DirProyectos() {
//   const [rows, setRows] = useState([]);
//   const [q, setQ] = useState("");
//   const [loading, setLoading] = useState(false);

//   const [selectedId, setSelectedId] = useState(getDirectorSelectedProject()?.id || "");
//   const [toast, setToast] = useState({ msg: "", kind: "info" });

//   const [edit, setEdit] = useState({
//     fechaInicio: "",
//     fechaFin: "",
//     tipo: "",
//     subtipo: "",
//     maxAyudantes: "",
//     maxArticulos: "",
//     sinFechaInicio: false,
//     sinFechaFin: false
//   });

//   async function load() {
//     setToast({ msg: "", kind: "info" });
//     setLoading(true);
//     try {
//       const res = await apiGet("/api/v1/director/mis-proyectos");
//       const arr = Array.isArray(res) ? res : (res?.items ?? []);
//       setRows(arr);

//       // si no hay selección, selecciona el primero
//       if (!selectedId && arr.length) {
//         const p = arr[0];
//         setSelectedId(p.id);
//         setDirectorSelectedProject({ id: p.id, codigo: p.codigo, nombre: p.nombre });
//       }
//     } catch (e) {
//       setToast({ msg: e.message, kind: "bad" });
//     } finally {
//       setLoading(false);
//     }
//   }

//   useEffect(() => { load(); }, []);

//   const filtered = useMemo(() => {
//     const s = q.trim().toLowerCase();
//     if (!s) return rows;
//     return rows.filter((r) => {
//       const txt = [
//         r.id, r.codigo, r.nombre, r.directorCorreo, r.tipo, r.subtipo
//       ].join(" ").toLowerCase();
//       return txt.includes(s);
//     });
//   }, [rows, q]);

//   const columns = [
//     {
//       key: "_sel",
//       label: "Sel",
//       render: (r) => (
//         <input
//           type="radio"
//           name="selProy"
//           checked={selectedId === r.id}
//           onChange={() => seleccionar(r)}
//         />
//       )
//     },
//     { key: "id", label: "ID" },
//     { key: "codigo", label: "Código" },
//     { key: "nombre", label: "Nombre" },
//     { key: "directorCorreo", label: "Director" },
//     {
//       key: "activo",
//       label: "Activo",
//       render: (r) => (r.activo ? <Badge kind="ok">SÍ</Badge> : <Badge kind="bad">NO</Badge>)
//     },
//     { key: "tipo", label: "Tipo" },
//     { key: "subtipo", label: "Subtipo" },
//     { key: "fechaInicio", label: "Inicio" },
//     { key: "fechaFin", label: "Fin" },
//     { key: "maxAyudantes", label: "Max Ayudantes" },
//     { key: "maxArticulos", label: "Max Artículos" }
//   ];

//   function seleccionar(p) {
//     setSelectedId(p.id);
//     setDirectorSelectedProject({ id: p.id, codigo: p.codigo, nombre: p.nombre });

//     // precargar en formulario de edición
//     setEdit({
//       fechaInicio: p.fechaInicio || "",
//       fechaFin: p.fechaFin || "",
//       tipo: p.tipo || "",
//       subtipo: p.subtipo || "",
//       maxAyudantes: p.maxAyudantes || "",
//       maxArticulos: p.maxArticulos || "",
//       sinFechaInicio: !p.fechaInicio,
//       sinFechaFin: !p.fechaFin
//     });

//     setToast({ msg: `Proyecto seleccionado: ${p.codigo} - ${p.nombre}`, kind: "ok" });
//   }

//   async function actualizar() {
//     if (!selectedId) {
//       setToast({ msg: "Selecciona un proyecto primero", kind: "bad" });
//       return;
//     }

//     if (!edit.tipo.trim()) {
//       setToast({ msg: "El tipo es requerido", kind: "bad" });
//       return;
//     }

//     const ma = String(edit.maxAyudantes).trim();
//     const mar = String(edit.maxArticulos).trim();

//     if (ma && isNaN(Number(ma))) return setToast({ msg: "maxAyudantes debe ser número", kind: "bad" });
//     if (mar && isNaN(Number(mar))) return setToast({ msg: "maxArticulos debe ser número", kind: "bad" });

//     const body = {
//       fechaInicio: edit.sinFechaInicio || !edit.fechaInicio ? null : edit.fechaInicio,
//       fechaFin: edit.sinFechaFin || !edit.fechaFin ? null : edit.fechaFin,
//       tipo: edit.tipo.trim(),
//       subtipo:
//         edit.tipo.trim().toUpperCase() !== "INVESTIGACION"
//           ? null
//           : (edit.subtipo?.trim() ? edit.subtipo.trim() : null),
//       maxAyudantes: Number(ma || 0),
//       maxArticulos: Number(mar || 0)
//     };

//     try {
//       await apiPut(`/api/v1/director/proyectos/${selectedId}`, body);
//       setToast({ msg: "Proyecto actualizado correctamente", kind: "ok" });
//       await load();
//     } catch (e) {
//       setToast({ msg: e.message, kind: "bad" });
//     }
//   }

//   return (
//     <div className="space-y-4">
//       <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
//         <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
//           <div>
//             <div className="text-lg font-bold text-poli-ink">Mis Proyectos</div>
//             <div className="text-sm text-gray-500">GET /api/v1/director/mis-proyectos</div>
//           </div>

//           <div className="flex gap-2">
//             <input
//               className="rounded-xl border px-3 py-2 w-64 outline-none focus:ring-2 focus:ring-poli-navy/30"
//               placeholder="Buscar..."
//               value={q}
//               onChange={(e) => setQ(e.target.value)}
//             />
//             <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
//               Refrescar
//             </button>
//           </div>
//         </div>

//         <div className="mt-4">
//           {loading ? <Loading /> : <Table columns={columns} rows={filtered} />}
//         </div>

//         <div className="mt-3 text-sm text-gray-600">
//           ✅ Selecciona un proyecto (radio) para usarlo en <b>Ayudantes</b> y <b>Bitácoras</b>.
//         </div>
//       </div>

//       <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
//         <div className="text-lg font-bold text-poli-ink">Actualizar detalles del proyecto</div>
//         <div className="text-sm text-gray-500">PUT /api/v1/director/proyectos/{`{proyectoId}`}</div>

//         <div className="mt-4 grid md:grid-cols-2 gap-3">
//           <div>
//             <div className="text-sm text-gray-600">fechaInicio (YYYY-MM-DD)</div>
//             <input
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.fechaInicio}
//               disabled={edit.sinFechaInicio}
//               onChange={(e) => setEdit({ ...edit, fechaInicio: e.target.value })}
//               placeholder="2026-01-01"
//             />
//             <label className="mt-2 flex items-center gap-2 text-sm">
//               <input
//                 type="checkbox"
//                 checked={edit.sinFechaInicio}
//                 onChange={(e) => setEdit({ ...edit, sinFechaInicio: e.target.checked, fechaInicio: "" })}
//               />
//               Sin fecha inicio
//             </label>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">fechaFin (YYYY-MM-DD)</div>
//             <input
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.fechaFin}
//               disabled={edit.sinFechaFin}
//               onChange={(e) => setEdit({ ...edit, fechaFin: e.target.value })}
//               placeholder="2026-12-31"
//             />
//             <label className="mt-2 flex items-center gap-2 text-sm">
//               <input
//                 type="checkbox"
//                 checked={edit.sinFechaFin}
//                 onChange={(e) => setEdit({ ...edit, sinFechaFin: e.target.checked, fechaFin: "" })}
//               />
//               Sin fecha fin
//             </label>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">tipo *</div>
//             <select
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.tipo}
//               onChange={(e) => {
//                 const v = e.target.value;
//                 setEdit({ ...edit, tipo: v, subtipo: v.toUpperCase() === "INVESTIGACION" ? edit.subtipo : "" });
//               }}
//             >
//               <option value="">Seleccione...</option>
//               <option value="INVESTIGACION">INVESTIGACION</option>
//               <option value="VINCULACION">VINCULACION</option>
//               <option value="TRANSFERENCIA_TECNOLOGICA">TRANSFERENCIA_TECNOLOGICA</option>
//             </select>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">subtipo (solo INVESTIGACION)</div>
//             <select
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.subtipo}
//               disabled={edit.tipo.toUpperCase() !== "INVESTIGACION"}
//               onChange={(e) => setEdit({ ...edit, subtipo: e.target.value })}
//             >
//               <option value="">(vacío)</option>
//               <option value="INTERNO">INTERNO</option>
//               <option value="SEMILLA">SEMILLA</option>
//               <option value="GRUPAL">GRUPAL</option>
//               <option value="MULTIDISCIPLINARIO">MULTIDISCIPLINARIO</option>
//             </select>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">maxAyudantes</div>
//             <input
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.maxAyudantes}
//               onChange={(e) => setEdit({ ...edit, maxAyudantes: e.target.value })}
//               placeholder="2"
//             />
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">maxArticulos</div>
//             <input
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={edit.maxArticulos}
//               onChange={(e) => setEdit({ ...edit, maxArticulos: e.target.value })}
//               placeholder="3"
//             />
//           </div>
//         </div>

//         <div className="mt-4 flex justify-end gap-2">
//           <button
//             onClick={actualizar}
//             className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold"
//           >
//             Guardar cambios
//           </button>
//         </div>
//       </div>

//       <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
//     </div>
//   );
// }




import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPut } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Toast from "../../components/Toast.jsx";
import Badge from "../../components/Badge.jsx";
import { setDirectorSelectedProject, getDirectorSelectedProject } from "../../lib/state";

export default function DirProyectos() {
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);

  const [selectedId, setSelectedId] = useState(getDirectorSelectedProject()?.id || "");
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  
  //const [showEditForm, setShowEditForm] = useState(false);
  const [edit, setEdit] = useState({
    fechaInicio: "",
    fechaFin: "",
    tipo: "",
    subtipo: "",
    maxAyudantes: "",
    maxArticulos: "",
    sinFechaInicio: false,
    sinFechaFin: false
  });

  async function load() {
    setToast({ msg: "", kind: "info" });
    setLoading(true);
    try {
      const res = await apiGet("/api/v1/director/mis-proyectos");
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(arr);

      // Si no hay selección, selecciona el primero
      if (!selectedId && arr.length) {
        const p = arr[0];
        setSelectedId(p.id);
        setDirectorSelectedProject({ id: p.id, codigo: p.codigo, nombre: p.nombre });
        precargarFormulario(p);
      } else if (selectedId) {
        // Recargar el formulario con los datos actualizados
        const p = arr.find(x => x.id === selectedId);
        if (p) precargarFormulario(p);
      }

      setToast({ msg: `✅ ${arr.length} proyectos cargados`, kind: "ok" });
    } catch (e) {
      setToast({ msg: e.message, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return rows;
    return rows.filter((r) => {
      const txt = [
        r.id, r.codigo, r.nombre, r.directorCorreo, r.tipo, r.subtipo
      ].join(" ").toLowerCase();
      return txt.includes(s);
    });
  }, [rows, q]);

  const columns = [
    {
      key: "_sel",
      label: "Sel",
      render: (r) => (
        <input
          type="radio"
          name="selProy"
          checked={selectedId === r.id}
          onChange={() => seleccionar(r)}
          className="w-5 h-5 cursor-pointer"
        />
      )
    },
    { key: "codigo", label: "Código" },
    { key: "nombre", label: "Nombre" },
    { key: "directorCorreo", label: "Director" },
    {
      key: "activo",
      label: "Activo",
      render: (r) => (r.activo ? <Badge kind="ok">SÍ</Badge> : <Badge kind="bad">NO</Badge>)
    },
    { 
      key: "tipo", 
      label: "Tipo",
      render: (r) => r.tipo || <span className="text-gray-400">-</span>
    },
    { 
      key: "subtipo", 
      label: "Subtipo",
      render: (r) => r.subtipo || <span className="text-gray-400">-</span>
    },
    { 
      key: "fechaInicio", 
      label: "Inicio",
      render: (r) => r.fechaInicio || <span className="text-gray-400">-</span>
    },
    { 
      key: "fechaFin", 
      label: "Fin",
      render: (r) => r.fechaFin || <span className="text-gray-400">-</span>
    },
    { key: "maxAyudantes", label: "Max Ayud." },
    { key: "maxArticulos", label: "Max Art." }
  ];

  function precargarFormulario(p) {
    setEdit({
      fechaInicio: p.fechaInicio || "",
      fechaFin: p.fechaFin || "",
      tipo: p.tipo || "",
      subtipo: p.subtipo || "",
      maxAyudantes: p.maxAyudantes || "",
      maxArticulos: p.maxArticulos || "",
      sinFechaInicio: !p.fechaInicio,
      sinFechaFin: !p.fechaFin
    });
  }

  function seleccionar(p) {
    setSelectedId(p.id);
    setDirectorSelectedProject({ id: p.id, codigo: p.codigo, nombre: p.nombre });
    precargarFormulario(p);

    //setShowEditForm(true);
    setToast({ msg: `📁 Proyecto seleccionado: ${p.codigo}`, kind: "ok" });
  }

  async function actualizar() {
    if (!selectedId) {
      setToast({ msg: "⚠️ Selecciona un proyecto primero", kind: "bad" });
      return;
    }

    if (!edit.tipo.trim()) {
      setToast({ msg: "⚠️ El tipo es requerido", kind: "bad" });
      return;
    }

    const ma = String(edit.maxAyudantes).trim();
    const mar = String(edit.maxArticulos).trim();

    if (ma && isNaN(Number(ma))) return setToast({ msg: "⚠️ maxAyudantes debe ser número", kind: "bad" });
    if (mar && isNaN(Number(mar))) return setToast({ msg: "⚠️ maxArticulos debe ser número", kind: "bad" });

    const body = {
      fechaInicio: edit.sinFechaInicio || !edit.fechaInicio ? null : edit.fechaInicio,
      fechaFin: edit.sinFechaFin || !edit.fechaFin ? null : edit.fechaFin,
      tipo: edit.tipo.trim(),
      subtipo:
        edit.tipo.trim().toUpperCase() !== "INVESTIGACION"
          ? null
          : (edit.subtipo?.trim() ? edit.subtipo.trim() : null),
      maxAyudantes: Number(ma || 0),
      maxArticulos: Number(mar || 0)
    };

    try {
      await apiPut(`/api/v1/director/proyectos/${selectedId}`, body);
      setToast({ msg: "✅ Proyecto actualizado correctamente", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ ${e.message}`, kind: "bad" });
    }
  }

  return (
    <div className="space-y-4">
      {/* Lista de proyectos */}
      <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
        <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between mb-4">
          <div>
            <div className="text-sm text-gray-500">Selecciona un proyecto</div>
            <div className="text-xs text-gray-400 mt-1">
              ℹ️ El proyecto seleccionado se usará en <strong>Ayudantes</strong> y <strong>Bitácoras</strong>
            </div>
          </div>

          <div className="flex gap-2">
            <input
              className="rounded-xl border px-3 py-2 w-64 outline-none focus:ring-2 focus:ring-poli-navy/30"
              placeholder="🔍 Buscar..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
            <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
              🔄 Refrescar
            </button>
          </div>
        </div>

        {loading ? <Loading /> : <Table columns={columns} rows={filtered} />}
      </div>

      {/* Formulario de edición (solo visible si hay proyecto seleccionado) */}
      
      {/* {showEditForm && selectedId && (
        <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <div className="text-lg font-bold text-poli-ink">⚙️ Actualizar detalles del proyecto</div>
              <div className="text-sm text-gray-500">
                Editando: <strong>{rows.find(r => r.id === selectedId)?.codigo}</strong>
              </div>
            </div>
            <button
              onClick={() => setShowEditForm(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div> */}

        {selectedId && (
        <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
          <div className="mb-4">
            <div className="text-lg font-bold text-poli-ink">⚙️ Actualizar detalles del proyecto</div>
            <div className="text-sm text-gray-500">
              Editando: <strong>{rows.find(r => r.id === selectedId)?.codigo}</strong>
            </div>
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm text-gray-600 font-semibold flex items-center gap-2">
                📅 Fecha Inicio
              </label>
              <input
                type="date"
                className="mt-1 w-full rounded-xl border px-3 py-2 disabled:bg-gray-100"
                value={edit.fechaInicio}
                disabled={edit.sinFechaInicio}
                onChange={(e) => setEdit({ ...edit, fechaInicio: e.target.value })}
              />
              <label className="mt-2 flex items-center gap-2 text-sm text-gray-600">
                <input
                  type="checkbox"
                  checked={edit.sinFechaInicio}
                  onChange={(e) => setEdit({ ...edit, sinFechaInicio: e.target.checked, fechaInicio: "" })}
                  className="rounded"
                />
                Sin fecha inicio
              </label>
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold flex items-center gap-2">
                📅 Fecha Fin
              </label>
              <input
                type="date"
                className="mt-1 w-full rounded-xl border px-3 py-2 disabled:bg-gray-100"
                value={edit.fechaFin}
                disabled={edit.sinFechaFin}
                onChange={(e) => setEdit({ ...edit, fechaFin: e.target.value })}
              />
              <label className="mt-2 flex items-center gap-2 text-sm text-gray-600">
                <input
                  type="checkbox"
                  checked={edit.sinFechaFin}
                  onChange={(e) => setEdit({ ...edit, sinFechaFin: e.target.checked, fechaFin: "" })}
                  className="rounded"
                />
                Sin fecha fin
              </label>
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold">🏷️ Tipo *</label>
              <select
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={edit.tipo}
                onChange={(e) => {
                  const v = e.target.value;
                  setEdit({ ...edit, tipo: v, subtipo: v.toUpperCase() === "INVESTIGACION" ? edit.subtipo : "" });
                }}
              >
                <option value="">Seleccione...</option>
                <option value="INVESTIGACION">🔬 INVESTIGACION</option>
                <option value="VINCULACION">🤝 VINCULACION</option>
                <option value="TRANSFERENCIA_TECNOLOGICA">💡 TRANSFERENCIA TECNOLOGICA</option>
              </select>
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold">🏷️ Subtipo (solo INVESTIGACION)</label>
              <select
                className="mt-1 w-full rounded-xl border px-3 py-2 disabled:bg-gray-100"
                value={edit.subtipo}
                disabled={edit.tipo.toUpperCase() !== "INVESTIGACION"}
                onChange={(e) => setEdit({ ...edit, subtipo: e.target.value })}
              >
                <option value="">(vacío)</option>
                <option value="INTERNO">INTERNO</option>
                <option value="SEMILLA">SEMILLA</option>
                <option value="GRUPAL">GRUPAL</option>
                <option value="MULTIDISCIPLINARIO">MULTIDISCIPLINARIO</option>
              </select>
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold">👥 Max Ayudantes</label>
              <input
                type="number"
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={edit.maxAyudantes}
                onChange={(e) => setEdit({ ...edit, maxAyudantes: e.target.value })}
                placeholder="2"
                min="0"
              />
            </div>

            <div>
              <label className="text-sm text-gray-600 font-semibold">📄 Max Artículos</label>
              <input
                type="number"
                className="mt-1 w-full rounded-xl border px-3 py-2"
                value={edit.maxArticulos}
                onChange={(e) => setEdit({ ...edit, maxArticulos: e.target.value })}
                placeholder="3"
                min="0"
              />
            </div>
          </div>

          <div className="mt-6 flex justify-end gap-2">
            <button
              onClick={() => setShowEditForm(false)}
              className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
            >
              Cancelar
            </button>
            <button
              onClick={actualizar}
              className="rounded-xl px-4 py-2 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold hover:shadow-lg"
            >
              💾 Guardar cambios
            </button>
          </div>
        </div>
      )}

      {/* Mensaje si no hay proyecto seleccionado */}
      {!selectedId && !loading && (
        <div className="rounded-2xl bg-blue-50 border border-blue-200 p-6 text-center">
          <div className="text-4xl mb-3">📁</div>
          <div className="font-bold text-blue-800">Selecciona un proyecto</div>
          <div className="text-sm text-blue-600 mt-2">
            Haz clic en el radio button de la tabla para seleccionar un proyecto y editarlo
          </div>
        </div>
      )}

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}