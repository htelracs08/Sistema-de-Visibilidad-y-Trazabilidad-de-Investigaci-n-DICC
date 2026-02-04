// import React, { useEffect, useMemo, useState } from "react";
// import { apiGet, apiPost } from "../../lib/api";
// import Table from "../../components/Table.jsx";
// import Modal from "../../components/Modal.jsx";
// import Loading from "../../components/Loading.jsx";
// import Badge from "../../components/Badge.jsx";

// export default function Proyectos() {
//   const [rows, setRows] = useState([]);
//   const [q, setQ] = useState("");
//   const [loading, setLoading] = useState(false);
//   const [err, setErr] = useState("");

//   const [openCreate, setOpenCreate] = useState(false);
//   const [profesores, setProfesores] = useState([]);
//   const [form, setForm] = useState({
//     codigo: "",
//     nombre: "",
//     correoDirector: "",
//     tipoProyecto: "",
//     subtipoProyecto: ""
//   });

//   const [openDetalle, setOpenDetalle] = useState(false);
//   const [detalleId, setDetalleId] = useState("");
//   const [detalleRows, setDetalleRows] = useState([]);
//   const [detalleLoading, setDetalleLoading] = useState(false);
//   const [detalleErr, setDetalleErr] = useState("");

//   async function load() {
//     setErr("");
//     setLoading(true);
//     try {
//       const res = await apiGet("/api/v1/jefatura/proyectos");
//       setRows(Array.isArray(res) ? res : []);
//     } catch (e) {
//       setErr(e.message);
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
//         r.codigo, r.nombre, r.correoDirector, r.tipo, r.subtipo
//       ].join(" ").toLowerCase();
//       return txt.includes(s);
//     });
//   }, [rows, q]);

//   const columns = [
//     { key: "id", label: "ID" },
//     { key: "codigo", label: "Código" },
//     { key: "nombre", label: "Nombre" },
//     { key: "correoDirector", label: "Director" },
//     { key: "tipo", label: "Tipo" },
//     { key: "subtipo", label: "Subtipo" },
//     {
//       key: "activo",
//       label: "Activo",
//       render: (r) => (r.activo ? <Badge kind="ok">ACTIVO</Badge> : <Badge kind="bad">INACTIVO</Badge>)
//     },
//     {
//       key: "_actions",
//       label: "Acciones",
//       render: (r) => (
//         <button
//           className="rounded-xl px-3 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
//           onClick={() => abrirDetalle(r.id)}
//         >
//           Ver detalle
//         </button>
//       )
//     }
//   ];

//   async function loadProfesores() {
//     const res = await apiGet("/api/v1/jefatura/profesores");
//     const arr = Array.isArray(res) ? res : [];
//     setProfesores(arr);
//   }

//   function openCrear() {
//     setForm({ codigo: "", nombre: "", correoDirector: "", tipoProyecto: "", subtipoProyecto: "" });
//     setOpenCreate(true);
//     loadProfesores().catch(() => {});
//   }

//   async function crear() {
//     if (!form.codigo.trim() || !form.nombre.trim() || !form.correoDirector.trim()) {
//       alert("Código, nombre y director son requeridos");
//       return;
//     }

//     // Importante: el backend “antiguo” a veces solo recibía codigo/nombre/correoDirector.
//     // Pero tú ya manejas tipo/subtipo en UI, aquí lo mandamos si existe.
//     const payload = {
//       codigo: form.codigo.trim(),
//       nombre: form.nombre.trim(),
//       correoDirector: form.correoDirector.trim(),
//       tipoProyecto: form.tipoProyecto ? form.tipoProyecto : null,
//       subtipoProyecto: form.tipoProyecto === "INVESTIGACION" ? (form.subtipoProyecto || null) : null
//     };

//     try {
//       await apiPost("/api/v1/jefatura/proyectos", payload);
//       setOpenCreate(false);
//       await load();
//       alert("Proyecto creado");
//     } catch (e) {
//       alert(e.message);
//     }
//   }

//   async function abrirDetalle(id) {
//     setDetalleId(id);
//     setOpenDetalle(true);
//     setDetalleErr("");
//     setDetalleLoading(true);
//     setDetalleRows([]);
//     try {
//       const res = await apiGet(`/api/v1/jefatura/proyectos/${id}/ayudantes`);
//       setDetalleRows(Array.isArray(res) ? res : []);
//     } catch (e) {
//       setDetalleErr(e.message);
//     } finally {
//       setDetalleLoading(false);
//     }
//   }

//   const detalleCols = [
//     { key: "contratoId", label: "ContratoId" },
//     { key: "estado", label: "Estado" },
//     { key: "motivoInactivo", label: "Motivo Inactivo" },
//     { key: "correoInstitucional", label: "Correo" },
//     { key: "tipoAyudante", label: "TipoAyudante" },
//     { key: "fechaInicio", label: "Inicio" },
//     { key: "fechaFin", label: "Fin" }
//   ];

//   return (
//     <div className="space-y-4">
//       <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
//         <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
//           <div>
//             <div className="text-lg font-bold text-poli-ink">Proyectos</div>
//             <div className="text-sm text-gray-500">GET /api/v1/jefatura/proyectos</div>
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
//             <button onClick={openCrear} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
//               Crear
//             </button>
//           </div>
//         </div>

//         <div className="mt-4">
//           {loading && <Loading />}
//           {err && <div className="text-red-600 font-semibold">Error: {err}</div>}
//           {!loading && !err && <Table columns={columns} rows={filtered} />}
//         </div>
//       </div>

//       <Modal open={openCreate} title="Crear proyecto" onClose={() => setOpenCreate(false)}>
//         <div className="grid md:grid-cols-2 gap-3">
//           <div>
//             <div className="text-sm text-gray-600">Código</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
//           </div>
//           <div>
//             <div className="text-sm text-gray-600">Nombre</div>
//             <input className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
//           </div>

//           <div className="md:col-span-2">
//             <div className="text-sm text-gray-600">Director</div>
//             <select
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.correoDirector}
//               onChange={(e) => setForm({ ...form, correoDirector: e.target.value })}
//             >
//               <option value="">Seleccione...</option>
//               {profesores.map((p) => (
//                 <option key={p.id || p.correo} value={p.correo}>
//                   {(p.nombres + " " + p.apellidos).trim()} ({p.correo})
//                 </option>
//               ))}
//             </select>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Tipo</div>
//             <select
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.tipoProyecto}
//               onChange={(e) => setForm({ ...form, tipoProyecto: e.target.value, subtipoProyecto: "" })}
//             >
//               <option value="">(opcional)</option>
//               <option value="INVESTIGACION">INVESTIGACION</option>
//               <option value="VINCULACION">VINCULACION</option>
//               <option value="TRANSFERENCIA_TECNOLOGICA">TRANSFERENCIA_TECNOLOGICA</option>
//             </select>
//           </div>

//           <div>
//             <div className="text-sm text-gray-600">Subtipo (solo INVESTIGACION)</div>
//             <select
//               className="mt-1 w-full rounded-xl border px-3 py-2"
//               value={form.subtipoProyecto}
//               disabled={form.tipoProyecto !== "INVESTIGACION"}
//               onChange={(e) => setForm({ ...form, subtipoProyecto: e.target.value })}
//             >
//               <option value="">(vacío)</option>
//               <option value="INTERNO">INTERNO</option>
//               <option value="SEMILLA">SEMILLA</option>
//               <option value="GRUPAL">GRUPAL</option>
//               <option value="MULTIDISCIPLINARIO">MULTIDISCIPLINARIO</option>
//             </select>
//           </div>
//         </div>

//         <div className="mt-4 flex justify-end gap-2">
//           <button onClick={() => setOpenCreate(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
//             Cancelar
//           </button>
//           <button onClick={crear} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
//             Crear
//           </button>
//         </div>
//       </Modal>

//       <Modal open={openDetalle} title={`Detalle Proyecto ${detalleId}`} onClose={() => setOpenDetalle(false)}>
//         {detalleLoading && <Loading />}
//         {detalleErr && <div className="text-red-600 font-semibold">Error: {detalleErr}</div>}
//         {!detalleLoading && !detalleErr && <Table columns={detalleCols} rows={detalleRows} />}
//       </Modal>
//     </div>
//   );
// }




import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Loading from "../../components/Loading.jsx";
import Modal from "../../components/Modal.jsx";
import Toast from "../../components/Toast.jsx";
import Badge from "../../components/Badge.jsx";

export default function JefProyectos() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  // ✨ BÚSQUEDA Y FILTROS AVANZADOS
  const [searchTerm, setSearchTerm] = useState("");
  const [filterActivo, setFilterActivo] = useState("TODOS"); // TODOS | SI | NO
  const [filterTipo, setFilterTipo] = useState("TODOS");

  // ✨ CREACIÓN DE PROYECTOS
  const [openCreate, setOpenCreate] = useState(false);
  const [profesores, setProfesores] = useState([]);
  const [loadingProfesores, setLoadingProfesores] = useState(false);
  const [form, setForm] = useState({
    codigo: "",
    nombre: "",
    correoDirector: "",
    tipoProyecto: "",
    subtipoProyecto: ""
  });

  // ✨ DETALLES DEL PROYECTO
  const [openDetails, setOpenDetails] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [ayudantesDelProyecto, setAyudantesDelProyecto] = useState([]);
  const [proyectoDetalle, setProyectoDetalle] = useState(null);

  // ========================================
  // CARGA INICIAL DE PROYECTOS
  // ========================================
  
  async function load() {
    setLoading(true);
    setToast({ msg: "", kind: "info" });
    try {
      const res = await apiGet("/api/v1/jefatura/proyectos");
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setRows(arr);
      setToast({ msg: `✅ ${arr.length} proyecto${arr.length !== 1 ? 's' : ''} cargado${arr.length !== 1 ? 's' : ''}`, kind: "ok" });
    } catch (e) {
      setToast({ msg: `❌ Error: ${e.message}`, kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  // ========================================
  // FILTRADO AVANZADO CON MÚLTIPLES CRITERIOS
  // ========================================
  
  const filteredRows = useMemo(() => {
    let filtered = rows;

    // Filtro por búsqueda de texto
    if (searchTerm.trim()) {
      const term = searchTerm.trim().toLowerCase();
      filtered = filtered.filter((r) => {
        const searchableText = [
          r.codigo,
          r.nombre,
          r.directorCorreo || r.correoDirector,
          r.tipo,
          r.subtipo
        ].join(" ").toLowerCase();
        return searchableText.includes(term);
      });
    }

    // Filtro por estado activo
    if (filterActivo !== "TODOS") {
      const isActivo = filterActivo === "SI";
      filtered = filtered.filter((r) => r.activo === isActivo);
    }

    // Filtro por tipo de proyecto
    if (filterTipo !== "TODOS") {
      filtered = filtered.filter((r) => r.tipo === filterTipo);
    }

    return filtered;
  }, [rows, searchTerm, filterActivo, filterTipo]);

  // ========================================
  // COLUMNAS DE LA TABLA PRINCIPAL
  // ========================================
  
  const columns = [
    { 
      key: "codigo", 
      label: "Código",
      render: (r) => <span className="font-mono font-semibold text-poli-navy">{r.codigo}</span>
    },
    { key: "nombre", label: "Nombre del Proyecto" },
    { 
      key: "directorCorreo", 
      label: "Director",
      render: (r) => (
        <span className="text-sm text-gray-600">
          {r.directorCorreo || r.correoDirector || "-"}
        </span>
      )
    },
    {
      key: "activo",
      label: "Estado",
      render: (r) => r.activo 
        ? <Badge kind="ok">✅ ACTIVO</Badge> 
        : <Badge kind="bad">❌ INACTIVO</Badge>
    },
    { 
      key: "tipo", 
      label: "Tipo",
      render: (r) => r.tipo ? (
        <span className="px-2 py-1 rounded-lg bg-blue-100 text-blue-800 text-xs font-semibold">
          {r.tipo}
        </span>
      ) : <span className="text-gray-400">-</span>
    },
    { 
      key: "subtipo", 
      label: "Subtipo",
      render: (r) => r.subtipo ? (
        <span className="text-xs text-gray-600">{r.subtipo}</span>
      ) : <span className="text-gray-400">-</span>
    },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        <button
          className="rounded-xl px-3 py-2 bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold text-sm hover:shadow-lg transition-all"
          onClick={() => verDetalles(r.id)}
        >
          👁️ Ver Ayudantes
        </button>
      )
    }
  ];

  // ========================================
  // CARGA DE PROFESORES PARA SELECTOR
  // ========================================
  
  async function loadProfesores() {
    setLoadingProfesores(true);
    try {
      const res = await apiGet("/api/v1/jefatura/profesores");
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setProfesores(arr);
    } catch (e) {
      setToast({ msg: `⚠️ No se pudieron cargar los profesores: ${e.message}`, kind: "bad" });
    } finally {
      setLoadingProfesores(false);
    }
  }

  // ========================================
  // ABRIR MODAL DE CREACIÓN
  // ========================================
  
  function abrirCrear() {
    setForm({
      codigo: "",
      nombre: "",
      correoDirector: "",
      tipoProyecto: "",
      subtipoProyecto: ""
    });
    setOpenCreate(true);
    loadProfesores();
  }

  // ========================================
  // CREAR NUEVO PROYECTO
  // ========================================
  
  async function crear() {
    const { codigo, nombre, correoDirector, tipoProyecto, subtipoProyecto } = form;

    // Validaciones
    if (!codigo.trim()) {
      setToast({ msg: "⚠️ El código del proyecto es requerido", kind: "bad" });
      return;
    }
    if (!nombre.trim()) {
      setToast({ msg: "⚠️ El nombre del proyecto es requerido", kind: "bad" });
      return;
    }
    if (!correoDirector.trim()) {
      setToast({ msg: "⚠️ Debe seleccionar un director", kind: "bad" });
      return;
    }

    const payload = {
      codigo: codigo.trim(),
      nombre: nombre.trim(),
      correoDirector: correoDirector.trim(),
      tipoProyecto: tipoProyecto || null,
      subtipoProyecto: tipoProyecto === "INVESTIGACION" ? (subtipoProyecto || null) : null
    };

    try {
      await apiPost("/api/v1/jefatura/proyectos", payload);
      setOpenCreate(false);
      setToast({ msg: "✅ Proyecto creado correctamente", kind: "ok" });
      await load();
    } catch (e) {
      setToast({ msg: `❌ Error al crear: ${e.message}`, kind: "bad" });
    }
  }

  // ========================================
  // VER DETALLES Y AYUDANTES DEL PROYECTO
  // ========================================
  
  async function verDetalles(proyectoId) {
    const proyecto = rows.find((p) => p.id === proyectoId);
    setProyectoDetalle(proyecto || null);
    setOpenDetails(true);
    setDetailsLoading(true);
    setAyudantesDelProyecto([]);

    try {
      const res = await apiGet(`/api/v1/jefatura/proyectos/${proyectoId}/ayudantes`);
      const arr = Array.isArray(res) ? res : (res?.items ?? []);
      setAyudantesDelProyecto(arr);
    } catch (e) {
      setToast({ msg: `❌ Error cargando ayudantes: ${e.message}`, kind: "bad" });
    } finally {
      setDetailsLoading(false);
    }
  }

  // ========================================
  // COLUMNAS DE AYUDANTES
  // ========================================
  
  const ayudantesColumns = [
    { 
      key: "contratoId", 
      label: "ID Contrato",
      render: (r) => <span className="font-mono text-sm">{r.contratoId}</span>
    },
    { key: "correoInstitucional", label: "Correo" },
    { key: "nombres", label: "Nombres" },
    { key: "apellidos", label: "Apellidos" },
    {
      key: "estado",
      label: "Estado",
      render: (r) => r.estado === "ACTIVO" 
        ? <Badge kind="ok">✅ ACTIVO</Badge> 
        : <Badge kind="bad">❌ INACTIVO</Badge>
    },
    { 
      key: "tipoAyudante", 
      label: "Tipo",
      render: (r) => (
        <span className="px-2 py-1 rounded-lg bg-purple-100 text-purple-800 text-xs font-semibold">
          {r.tipoAyudante || "-"}
        </span>
      )
    },
    { key: "facultad", label: "Facultad" },
    { 
      key: "quintil", 
      label: "Quintil",
      render: (r) => r.quintil ? (
        <span className="font-semibold text-poli-navy">{r.quintil}</span>
      ) : <span className="text-gray-400">-</span>
    }
  ];

  // ========================================
  // TIPOS ÚNICOS PARA FILTRO
  // ========================================
  
  const tiposUnicos = ["TODOS", ...new Set(rows.map(r => r.tipo).filter(Boolean))];

  // ========================================
  // RENDERIZADO
  // ========================================
  
  return (
    <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5 space-y-4">
      
      {/* ============ HEADER Y CONTROLES ============ */}
      <div className="flex flex-col gap-4">
        <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
          <div>
            <div className="text-2xl font-bold text-poli-ink flex items-center gap-2">
              📁 Gestión de Proyectos
            </div>
            <div className="text-sm text-gray-500 mt-1">
              Administra proyectos y sus ayudantes asociados
            </div>
          </div>
          
          <button 
            onClick={abrirCrear} 
            className="rounded-xl px-5 py-2.5 bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold hover:shadow-lg transition-all flex items-center gap-2 justify-center"
          >
            <span className="text-xl">➕</span>
            <span>Crear Proyecto</span>
          </button>
        </div>

        {/* ============ BARRA DE BÚSQUEDA Y FILTROS ============ */}
        <div className="grid md:grid-cols-4 gap-3">
          
          {/* Búsqueda general */}
          <div className="md:col-span-2">
            <div className="relative">
              <input
                type="text"
                placeholder="🔍 Buscar por código, nombre, director, tipo..."
                className="w-full rounded-xl border border-gray-300 px-4 py-2.5 pl-10 outline-none focus:ring-2 focus:ring-poli-navy/30 focus:border-poli-navy transition-all"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <span className="absolute left-3 top-3 text-gray-400">🔍</span>
            </div>
          </div>
          
          {/* Filtro por estado activo */}
          <div>
            <select
              className="w-full rounded-xl border border-gray-300 px-4 py-2.5 outline-none focus:ring-2 focus:ring-poli-navy/30 focus:border-poli-navy transition-all"
              value={filterActivo}
              onChange={(e) => setFilterActivo(e.target.value)}
            >
              <option value="TODOS">📋 Todos los estados</option>
              <option value="SI">✅ Solo activos</option>
              <option value="NO">❌ Solo inactivos</option>
            </select>
          </div>

          {/* Filtro por tipo */}
          <div>
            <select
              className="w-full rounded-xl border border-gray-300 px-4 py-2.5 outline-none focus:ring-2 focus:ring-poli-navy/30 focus:border-poli-navy transition-all"
              value={filterTipo}
              onChange={(e) => setFilterTipo(e.target.value)}
            >
              {tiposUnicos.map(tipo => (
                <option key={tipo} value={tipo}>
                  {tipo === "TODOS" ? "🏷️ Todos los tipos" : tipo}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* ============ RESUMEN DE FILTROS ============ */}
        {(searchTerm || filterActivo !== "TODOS" || filterTipo !== "TODOS") && (
          <div className="flex items-center justify-between px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl">
            <div className="flex items-center gap-2 text-sm">
              <span className="font-semibold text-blue-900">
                📊 Mostrando {filteredRows.length} de {rows.length} proyectos
              </span>
              {searchTerm && (
                <span className="px-2 py-1 bg-blue-200 text-blue-800 rounded-lg text-xs">
                  Búsqueda: "{searchTerm}"
                </span>
              )}
            </div>
            <button
              onClick={() => {
                setSearchTerm("");
                setFilterActivo("TODOS");
                setFilterTipo("TODOS");
              }}
              className="text-poli-red hover:underline text-sm font-semibold"
            >
              🗑️ Limpiar filtros
            </button>
          </div>
        )}
      </div>

      {/* ============ TABLA DE PROYECTOS ============ */}
      {loading ? <Loading /> : <Table columns={columns} rows={filteredRows} />}

      {/* ============ MODAL: CREAR PROYECTO ============ */}
      <Modal open={openCreate} title="➕ Crear Nuevo Proyecto" onClose={() => setOpenCreate(false)}>
        <div className="space-y-4">
          
          <div className="grid md:grid-cols-2 gap-4">
            
            {/* Código del proyecto */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                Código del Proyecto <span className="text-red-600">*</span>
              </label>
              <input 
                className="w-full rounded-xl border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
                value={form.codigo}
                onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                placeholder="PRJ-2025-001"
              />
            </div>

            {/* Director del proyecto */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                Director del Proyecto <span className="text-red-600">*</span>
              </label>
              {loadingProfesores ? (
                <div className="w-full rounded-xl border border-gray-300 px-3 py-2 bg-gray-50 text-gray-500 flex items-center gap-2">
                  <div className="animate-spin">⏳</div>
                  Cargando profesores...
                </div>
              ) : (
                <select
                  className="w-full rounded-xl border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
                  value={form.correoDirector}
                  onChange={(e) => setForm({ ...form, correoDirector: e.target.value })}
                >
                  <option value="">Seleccione un director...</option>
                  {profesores.map((p) => (
                    <option key={p.id || p.correo} value={p.correo}>
                      {p.nombres} {p.apellidos} ({p.correo})
                    </option>
                  ))}
                </select>
              )}
            </div>
          </div>

          {/* Nombre del proyecto */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Nombre del Proyecto <span className="text-red-600">*</span>
            </label>
            <input 
              className="w-full rounded-xl border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
              value={form.nombre}
              onChange={(e) => setForm({ ...form, nombre: e.target.value })}
              placeholder="Desarrollo de sistema de gestión académica..."
            />
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            
            {/* Tipo de proyecto */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                Tipo de Proyecto
              </label>
              <select
                className="w-full rounded-xl border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
                value={form.tipoProyecto}
                onChange={(e) => setForm({ ...form, tipoProyecto: e.target.value, subtipoProyecto: "" })}
              >
                <option value="">(Opcional)</option>
                <option value="INVESTIGACION">🔬 INVESTIGACION</option>
                <option value="VINCULACION">🤝 VINCULACION</option>
                <option value="TRANSFERENCIA_TECNOLOGICA">💻 TRANSFERENCIA TECNOLOGICA</option>
              </select>
            </div>

            {/* Subtipo (solo para INVESTIGACION) */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                Subtipo {form.tipoProyecto === "INVESTIGACION" && "(para Investigación)"}
              </label>
              <select
                className="w-full rounded-xl border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30 disabled:bg-gray-100 disabled:cursor-not-allowed"
                value={form.subtipoProyecto}
                disabled={form.tipoProyecto !== "INVESTIGACION"}
                onChange={(e) => setForm({ ...form, subtipoProyecto: e.target.value })}
              >
                <option value="">(Vacío)</option>
                <option value="INTERNO">INTERNO</option>
                <option value="SEMILLA">SEMILLA</option>
                <option value="GRUPAL">GRUPAL</option>
                <option value="MULTIDISCIPLINARIO">MULTIDISCIPLINARIO</option>
              </select>
              {form.tipoProyecto !== "INVESTIGACION" && (
                <p className="text-xs text-gray-500 mt-1">
                  Solo disponible para proyectos de INVESTIGACION
                </p>
              )}
            </div>
          </div>

          {/* Botones de acción */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button 
              onClick={() => setOpenCreate(false)} 
              className="rounded-xl px-5 py-2 bg-gray-200 hover:bg-gray-300 font-bold transition-all"
            >
              Cancelar
            </button>
            <button 
              onClick={crear}
              className="rounded-xl px-5 py-2 bg-gradient-to-r from-poli-red to-red-600 text-white font-bold hover:shadow-lg transition-all"
            >
              ✅ Crear Proyecto
            </button>
          </div>
        </div>
      </Modal>

      {/* ============ MODAL: VER AYUDANTES DEL PROYECTO ============ */}
      <Modal 
        open={openDetails} 
        title={`👥 Ayudantes del Proyecto`} 
        onClose={() => setOpenDetails(false)}
      >
        {proyectoDetalle && (
          <div className="mb-4 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border border-blue-200">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="font-bold text-blue-900 text-lg flex items-center gap-2">
                  <span>📁</span>
                  <span>{proyectoDetalle.codigo}</span>
                </div>
                <div className="text-sm text-blue-800 mt-1">
                  {proyectoDetalle.nombre}
                </div>
                <div className="text-xs text-blue-600 mt-2 flex items-center gap-1">
                  <span>👨‍🏫</span>
                  <span>Director: {proyectoDetalle.directorCorreo || proyectoDetalle.correoDirector}</span>
                </div>
              </div>
              {proyectoDetalle.activo ? (
                <Badge kind="ok">✅ ACTIVO</Badge>
              ) : (
                <Badge kind="bad">❌ INACTIVO</Badge>
              )}
            </div>
          </div>
        )}

        {detailsLoading && <Loading />}
        
        {!detailsLoading && ayudantesDelProyecto.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <div className="text-4xl mb-2">👥</div>
            <div className="font-semibold">No hay ayudantes asignados a este proyecto</div>
          </div>
        )}
        
        {!detailsLoading && ayudantesDelProyecto.length > 0 && (
          <>
            <div className="mb-3 text-sm text-gray-600">
              <span className="font-semibold">{ayudantesDelProyecto.length}</span> ayudante{ayudantesDelProyecto.length !== 1 ? 's' : ''} encontrado{ayudantesDelProyecto.length !== 1 ? 's' : ''}
            </div>
            <Table columns={ayudantesColumns} rows={ayudantesDelProyecto} />
          </>
        )}
      </Modal>

      {/* ============ TOAST DE NOTIFICACIONES ============ */}
      <Toast 
        msg={toast.msg} 
        kind={toast.kind} 
        onClose={() => setToast({ msg: "", kind: "info" })} 
      />
    </div>
  );
}