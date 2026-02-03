import React, { useEffect, useMemo, useState } from "react";
import { apiGet, apiPost } from "../../lib/api";
import Table from "../../components/Table.jsx";
import Modal from "../../components/Modal.jsx";
import Loading from "../../components/Loading.jsx";
import Badge from "../../components/Badge.jsx";

export default function Proyectos() {
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const [openCreate, setOpenCreate] = useState(false);
  const [profesores, setProfesores] = useState([]);
  const [form, setForm] = useState({
    codigo: "",
    nombre: "",
    correoDirector: "",
    tipoProyecto: "",
    subtipoProyecto: ""
  });

  const [openDetalle, setOpenDetalle] = useState(false);
  const [detalleId, setDetalleId] = useState("");
  const [detalleRows, setDetalleRows] = useState([]);
  const [detalleLoading, setDetalleLoading] = useState(false);
  const [detalleErr, setDetalleErr] = useState("");

  async function load() {
    setErr("");
    setLoading(true);
    try {
      const res = await apiGet("/api/v1/jefatura/proyectos");
      setRows(Array.isArray(res) ? res : []);
    } catch (e) {
      setErr(e.message);
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
        r.codigo, r.nombre, r.correoDirector, r.tipo, r.subtipo
      ].join(" ").toLowerCase();
      return txt.includes(s);
    });
  }, [rows, q]);

  const columns = [
    { key: "id", label: "ID" },
    { key: "codigo", label: "Código" },
    { key: "nombre", label: "Nombre" },
    { key: "correoDirector", label: "Director" },
    { key: "tipo", label: "Tipo" },
    { key: "subtipo", label: "Subtipo" },
    {
      key: "activo",
      label: "Activo",
      render: (r) => (r.activo ? <Badge kind="ok">ACTIVO</Badge> : <Badge kind="bad">INACTIVO</Badge>)
    },
    {
      key: "_actions",
      label: "Acciones",
      render: (r) => (
        <button
          className="rounded-xl px-3 py-2 bg-poli-gray hover:bg-gray-200 font-bold"
          onClick={() => abrirDetalle(r.id)}
        >
          Ver detalle
        </button>
      )
    }
  ];

  async function loadProfesores() {
    const res = await apiGet("/api/v1/jefatura/profesores");
    const arr = Array.isArray(res) ? res : [];
    setProfesores(arr);
  }

  function openCrear() {
    setForm({ codigo: "", nombre: "", correoDirector: "", tipoProyecto: "", subtipoProyecto: "" });
    setOpenCreate(true);
    loadProfesores().catch(() => {});
  }

  async function crear() {
    if (!form.codigo.trim() || !form.nombre.trim() || !form.correoDirector.trim()) {
      alert("Código, nombre y director son requeridos");
      return;
    }

    // Importante: el backend “antiguo” a veces solo recibía codigo/nombre/correoDirector.
    // Pero tú ya manejas tipo/subtipo en UI, aquí lo mandamos si existe.
    const payload = {
      codigo: form.codigo.trim(),
      nombre: form.nombre.trim(),
      correoDirector: form.correoDirector.trim(),
      tipoProyecto: form.tipoProyecto ? form.tipoProyecto : null,
      subtipoProyecto: form.tipoProyecto === "INVESTIGACION" ? (form.subtipoProyecto || null) : null
    };

    try {
      await apiPost("/api/v1/jefatura/proyectos", payload);
      setOpenCreate(false);
      await load();
      alert("Proyecto creado");
    } catch (e) {
      alert(e.message);
    }
  }

  async function abrirDetalle(id) {
    setDetalleId(id);
    setOpenDetalle(true);
    setDetalleErr("");
    setDetalleLoading(true);
    setDetalleRows([]);
    try {
      const res = await apiGet(`/api/v1/jefatura/proyectos/${id}/ayudantes`);
      setDetalleRows(Array.isArray(res) ? res : []);
    } catch (e) {
      setDetalleErr(e.message);
    } finally {
      setDetalleLoading(false);
    }
  }

  const detalleCols = [
    { key: "contratoId", label: "ContratoId" },
    { key: "estado", label: "Estado" },
    { key: "motivoInactivo", label: "Motivo Inactivo" },
    { key: "correoInstitucional", label: "Correo" },
    { key: "tipoAyudante", label: "TipoAyudante" },
    { key: "fechaInicio", label: "Inicio" },
    { key: "fechaFin", label: "Fin" }
  ];

  return (
    <div className="space-y-4">
      <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
        <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
          <div>
            <div className="text-lg font-bold text-poli-ink">Proyectos</div>
            <div className="text-sm text-gray-500">GET /api/v1/jefatura/proyectos</div>
          </div>

          <div className="flex gap-2">
            <input
              className="rounded-xl border px-3 py-2 w-64 outline-none focus:ring-2 focus:ring-poli-navy/30"
              placeholder="Buscar..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
            />
            <button onClick={load} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
              Refrescar
            </button>
            <button onClick={openCrear} className="rounded-xl px-4 py-2 bg-poli-navy text-white font-bold">
              Crear
            </button>
          </div>
        </div>

        <div className="mt-4">
          {loading && <Loading />}
          {err && <div className="text-red-600 font-semibold">Error: {err}</div>}
          {!loading && !err && <Table columns={columns} rows={filtered} />}
        </div>
      </div>

      <Modal open={openCreate} title="Crear proyecto" onClose={() => setOpenCreate(false)}>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <div className="text-sm text-gray-600">Código</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
          </div>
          <div>
            <div className="text-sm text-gray-600">Nombre</div>
            <input className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
          </div>

          <div className="md:col-span-2">
            <div className="text-sm text-gray-600">Director</div>
            <select
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.correoDirector}
              onChange={(e) => setForm({ ...form, correoDirector: e.target.value })}
            >
              <option value="">Seleccione...</option>
              {profesores.map((p) => (
                <option key={p.id || p.correo} value={p.correo}>
                  {(p.nombres + " " + p.apellidos).trim()} ({p.correo})
                </option>
              ))}
            </select>
          </div>

          <div>
            <div className="text-sm text-gray-600">Tipo</div>
            <select
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.tipoProyecto}
              onChange={(e) => setForm({ ...form, tipoProyecto: e.target.value, subtipoProyecto: "" })}
            >
              <option value="">(opcional)</option>
              <option value="INVESTIGACION">INVESTIGACION</option>
              <option value="VINCULACION">VINCULACION</option>
              <option value="TRANSFERENCIA_TECNOLOGICA">TRANSFERENCIA_TECNOLOGICA</option>
            </select>
          </div>

          <div>
            <div className="text-sm text-gray-600">Subtipo (solo INVESTIGACION)</div>
            <select
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.subtipoProyecto}
              disabled={form.tipoProyecto !== "INVESTIGACION"}
              onChange={(e) => setForm({ ...form, subtipoProyecto: e.target.value })}
            >
              <option value="">(vacío)</option>
              <option value="INTERNO">INTERNO</option>
              <option value="SEMILLA">SEMILLA</option>
              <option value="GRUPAL">GRUPAL</option>
              <option value="MULTIDISCIPLINARIO">MULTIDISCIPLINARIO</option>
            </select>
          </div>
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <button onClick={() => setOpenCreate(false)} className="rounded-xl px-4 py-2 bg-poli-gray hover:bg-gray-200 font-bold">
            Cancelar
          </button>
          <button onClick={crear} className="rounded-xl px-4 py-2 bg-poli-red text-white font-bold">
            Crear
          </button>
        </div>
      </Modal>

      <Modal open={openDetalle} title={`Detalle Proyecto ${detalleId}`} onClose={() => setOpenDetalle(false)}>
        {detalleLoading && <Loading />}
        {detalleErr && <div className="text-red-600 font-semibold">Error: {detalleErr}</div>}
        {!detalleLoading && !detalleErr && <Table columns={detalleCols} rows={detalleRows} />}
      </Modal>
    </div>
  );
}
