import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Plus, UserX } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import { directorService } from '../../services/api';

export default function DirectorAyudantes() {
  const { proyectoId } = useParams();
  const [ayudantes, setAyudantes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    nombres: '',
    apellidos: '',
    correo: '',
    cedula: '',
    tipo: 'ASISTENTE_INVESTIGACION',
    fechaInicio: '',
    horasMensuales: '',
  });

  useEffect(() => {
    if (proyectoId) {
      cargarAyudantes();
    }
  }, [proyectoId]);

  const cargarAyudantes = async () => {
    try {
      const data = await directorService.getAyudantesProyecto(proyectoId);
      setAyudantes(data);
    } catch (error) {
      console.error('Error al cargar ayudantes:', error);
      alert('Error al cargar los ayudantes');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await directorService.registrarAyudante(proyectoId, formData);
      alert('Ayudante registrado exitosamente');
      setShowModal(false);
      setFormData({
        nombres: '',
        apellidos: '',
        correo: '',
        cedula: '',
        tipo: 'ASISTENTE_INVESTIGACION',
        fechaInicio: '',
        horasMensuales: '',
      });
      cargarAyudantes();
    } catch (error) {
      console.error('Error al registrar ayudante:', error);
      alert(error.response?.data?.msg || 'Error al registrar el ayudante');
    }
  };

  const handleFinalizarContrato = async (contratoId) => {
    const motivo = prompt('Ingrese el motivo de finalización:');
    if (!motivo) return;

    try {
      await directorService.finalizarContrato(contratoId, motivo);
      alert('Contrato finalizado');
      cargarAyudantes();
    } catch (error) {
      console.error('Error al finalizar contrato:', error);
      alert('Error al finalizar el contrato');
    }
  };

  const columns = [
    {
      header: 'Nombre',
      render: (row) => `${row.nombres} ${row.apellidos}`,
    },
    {
      header: 'Correo',
      accessor: 'correo',
    },
    {
      header: 'Tipo',
      accessor: 'tipo',
    },
    {
      header: 'Horas Mensuales',
      accessor: 'horasMensuales',
    },
    {
      header: 'Estado',
      render: (row) => (
        <span
          className={`px-3 py-1 rounded-full text-xs font-medium ${
            row.estado === 'ACTIVO'
              ? 'bg-green-100 text-green-800'
              : 'bg-gray-100 text-gray-800'
          }`}
        >
          {row.estado}
        </span>
      ),
    },
    {
      header: 'Acciones',
      render: (row) =>
        row.estado === 'ACTIVO' && (
          <Button
            variant="danger"
            size="sm"
            icon={UserX}
            onClick={() => handleFinalizarContrato(row.contratoId)}
          >
            Finalizar
          </Button>
        ),
    },
  ];

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
          <h1 className="text-3xl font-bold text-gray-900">Ayudantes del Proyecto</h1>
          <p className="text-gray-600 mt-2">Gestiona los ayudantes asignados</p>
        </div>
        <Button icon={Plus} onClick={() => setShowModal(true)}>
          Registrar Ayudante
        </Button>
      </div>

      <Card>
        <Table columns={columns} data={ayudantes} />
      </Card>

      {/* Modal Registrar Ayudante */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-2xl font-bold text-gray-900">Registrar Ayudante</h2>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombres *
                  </label>
                  <input
                    type="text"
                    value={formData.nombres}
                    onChange={(e) => setFormData({ ...formData, nombres: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Apellidos *
                  </label>
                  <input
                    type="text"
                    value={formData.apellidos}
                    onChange={(e) => setFormData({ ...formData, apellidos: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Correo *
                  </label>
                  <input
                    type="email"
                    value={formData.correo}
                    onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Cédula *
                  </label>
                  <input
                    type="text"
                    value={formData.cedula}
                    onChange={(e) => setFormData({ ...formData, cedula: e.target.value })}
                    className="input-field"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo de Ayudante *
                  </label>
                  <select
                    value={formData.tipo}
                    onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                    className="input-field"
                    required
                  >
                    <option value="ASISTENTE_INVESTIGACION">Asistente de Investigación</option>
                    <option value="AYUDANTE_INVESTIGACION">Ayudante de Investigación</option>
                    <option value="TECNICO_INVESTIGACION">Técnico de Investigación</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Horas Mensuales *
                  </label>
                  <input
                    type="number"
                    value={formData.horasMensuales}
                    onChange={(e) => setFormData({ ...formData, horasMensuales: e.target.value })}
                    className="input-field"
                    required
                    min="1"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Fecha de Inicio *
                </label>
                <input
                  type="date"
                  value={formData.fechaInicio}
                  onChange={(e) => setFormData({ ...formData, fechaInicio: e.target.value })}
                  className="input-field"
                  required
                />
              </div>

              <div className="flex gap-3 justify-end pt-4">
                <Button variant="secondary" onClick={() => setShowModal(false)} type="button">
                  Cancelar
                </Button>
                <Button type="submit">Registrar</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}