import { useEffect, useState } from 'react';
import { Plus, Search, Eye } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import { jefaturaService } from '../../services/api';

export default function JefaturaProyectos() {
  const [proyectos, setProyectos] = useState([]);
  const [profesores, setProfesores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [busqueda, setBusqueda] = useState('');
  const [formData, setFormData] = useState({
    nombre: '',
    codigo: '',
    tipo: 'INVESTIGACION',
    subtipo: 'INTERNO',
    descripcion: '',
    profesorId: '',
  });

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      const [proyectosData, profesoresData] = await Promise.all([
        jefaturaService.getProyectos(),
        jefaturaService.getProfesores(),
      ]);
      setProyectos(proyectosData);
      setProfesores(profesoresData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      alert('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await jefaturaService.crearProyecto(formData);
      alert('Proyecto creado exitosamente');
      setShowModal(false);
      setFormData({
        nombre: '',
        codigo: '',
        tipo: 'INVESTIGACION',
        subtipo: 'INTERNO',
        descripcion: '',
        profesorId: '',
      });
      cargarDatos();
    } catch (error) {
      console.error('Error al crear proyecto:', error);
      alert('Error al crear el proyecto');
    }
  };

  const columns = [
    {
      header: 'Código',
      accessor: 'codigo',
    },
    {
      header: 'Nombre',
      accessor: 'nombre',
    },
    {
      header: 'Tipo',
      accessor: 'tipo',
    },
    {
      header: 'Director',
      render: (row) => `${row.director?.nombres} ${row.director?.apellidos}`,
    },
    {
      header: 'Estado',
      render: (row) => (
        <span
          className={`px-3 py-1 rounded-full text-xs font-medium ${
            row.activo
              ? 'bg-green-100 text-green-800'
              : 'bg-gray-100 text-gray-800'
          }`}
        >
          {row.activo ? 'Activo' : 'Inactivo'}
        </span>
      ),
    },
    {
      header: 'Acciones',
      render: (row) => (
        <Button variant="outline" size="sm" icon={Eye}>
          Ver
        </Button>
      ),
    },
  ];

  const proyectosFiltrados = proyectos.filter((p) =>
    p.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    p.codigo.toLowerCase().includes(busqueda.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-epn-blue"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Proyectos</h1>
          <p className="text-gray-600 mt-2">Gestiona los proyectos de investigación</p>
        </div>
        <Button icon={Plus} onClick={() => setShowModal(true)}>
          Nuevo Proyecto
        </Button>
      </div>

      <Card>
        <div className="mb-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Buscar por nombre o código..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="input-field pl-10"
            />
          </div>
        </div>

        <Table columns={columns} data={proyectosFiltrados} />
      </Card>

      {/* Modal Crear Proyecto */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-2xl font-bold text-gray-900">Nuevo Proyecto</h2>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Código *
                  </label>
                  <input
                    type="text"
                    value={formData.codigo}
                    onChange={(e) => setFormData({ ...formData, codigo: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre *
                  </label>
                  <input
                    type="text"
                    value={formData.nombre}
                    onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo *
                  </label>
                  <select
                    value={formData.tipo}
                    onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                    className="input-field"
                    required
                  >
                    <option value="INVESTIGACION">Investigación</option>
                    <option value="VINCULACION">Vinculación</option>
                    <option value="TRANSFERENCIA_TECNOLOGICA">Transferencia Tecnológica</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Subtipo *
                  </label>
                  <select
                    value={formData.subtipo}
                    onChange={(e) => setFormData({ ...formData, subtipo: e.target.value })}
                    className="input-field"
                    required
                  >
                    <option value="INTERNO">Interno</option>
                    <option value="SEMILLA">Semilla</option>
                    <option value="GRUPAL">Grupal</option>
                    <option value="MULTIDISCIPLINARIO">Multidisciplinario</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Director *
                </label>
                <select
                  value={formData.profesorId}
                  onChange={(e) => setFormData({ ...formData, profesorId: e.target.value })}
                  className="input-field"
                  required
                >
                  <option value="">Seleccione un director</option>
                  {profesores.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nombres} {p.apellidos}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Descripción
                </label>
                <textarea
                  value={formData.descripcion}
                  onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                  className="input-field"
                  rows="4"
                />
              </div>

              <div className="flex gap-3 justify-end pt-4">
                <Button variant="secondary" onClick={() => setShowModal(false)} type="button">
                  Cancelar
                </Button>
                <Button type="submit">Crear Proyecto</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}