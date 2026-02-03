import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Eye, Check, X } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import { directorService } from '../../services/api';

export default function DirectorBitacoras() {
  const { proyectoId } = useParams();
  const [bitacoras, setBitacoras] = useState([]);
  const [bitacoraSeleccionada, setBitacoraSeleccionada] = useState(null);
  const [loading, setLoading] = useState(true);
  const [observacion, setObservacion] = useState('');

  useEffect(() => {
    if (proyectoId) {
      cargarBitacoras();
    }
  }, [proyectoId]);

  const cargarBitacoras = async () => {
    try {
      const data = await directorService.getBitacorasPendientes(proyectoId);
      setBitacoras(data);
    } catch (error) {
      console.error('Error al cargar bitácoras:', error);
      alert('Error al cargar las bitácoras');
    } finally {
      setLoading(false);
    }
  };

  const handleVerDetalle = async (bitacoraId) => {
    try {
      const detalle = await directorService.getBitacora(bitacoraId);
      setBitacoraSeleccionada(detalle);
    } catch (error) {
      console.error('Error al cargar detalle:', error);
      alert('Error al cargar el detalle de la bitácora');
    }
  };

  const handleRevisar = async (decision) => {
    if (!bitacoraSeleccionada) return;

    if (decision === 'RECHAZADA' && !observacion.trim()) {
      alert('Debe ingresar una observación al rechazar');
      return;
    }

    try {
      await directorService.revisarBitacora(
        bitacoraSeleccionada.id,
        decision,
        observacion
      );
      alert(`Bitácora ${decision === 'APROBADA' ? 'aprobada' : 'rechazada'} exitosamente`);
      setBitacoraSeleccionada(null);
      setObservacion('');
      cargarBitacoras();
    } catch (error) {
      console.error('Error al revisar bitácora:', error);
      alert('Error al revisar la bitácora');
    }
  };

  const columns = [
    {
      header: 'Mes/Año',
      render: (row) => `${row.mes}/${row.anio}`,
    },
    {
      header: 'Ayudante',
      render: (row) => `${row.ayudante?.nombres} ${row.ayudante?.apellidos}`,
    },
    {
      header: 'Fecha Envío',
      accessor: 'fechaEnvio',
    },
    {
      header: 'Estado',
      render: (row) => (
        <span className="px-3 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
          {row.estado}
        </span>
      ),
    },
    {
      header: 'Acciones',
      render: (row) => (
        <Button
          variant="outline"
          size="sm"
          icon={Eye}
          onClick={() => handleVerDetalle(row.id)}
        >
          Ver Detalle
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
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Bitácoras Pendientes</h1>
        <p className="text-gray-600 mt-2">Revisa y aprueba las bitácoras enviadas</p>
      </div>

      <Card>
        {bitacoras.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">No hay bitácoras pendientes de revisión</p>
          </div>
        ) : (
          <Table columns={columns} data={bitacoras} />
        )}
      </Card>

      {/* Modal de detalle */}
      {bitacoraSeleccionada && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-2xl font-bold text-gray-900">
                Bitácora {bitacoraSeleccionada.mes}/{bitacoraSeleccionada.anio}
              </h2>
              <p className="text-gray-600 mt-1">
                Ayudante: {bitacoraSeleccionada.ayudante?.nombres} {bitacoraSeleccionada.ayudante?.apellidos}
              </p>
            </div>

            <div className="p-6 space-y-6">
              {/* Semanas */}
              {bitacoraSeleccionada.semanas?.map((semana, index) => (
                <Card key={index} title={`Semana ${semana.numero}`}>
                  <div className="space-y-3">
                    {semana.informes?.map((informe, idx) => (
                      <div key={idx} className="p-3 bg-gray-50 rounded-lg">
                        <p className="text-sm font-medium text-gray-900">Informe {idx + 1}</p>
                        <p className="text-sm text-gray-700 mt-1">{informe.descripcion}</p>
                      </div>
                    ))}
                    {semana.actividades?.map((actividad, idx) => (
                      <div key={idx} className="p-3 bg-blue-50 rounded-lg">
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="text-sm font-medium text-gray-900">
                              {new Date(actividad.fecha).toLocaleDateString()}
                            </p>
                            <p className="text-sm text-gray-700 mt-1">{actividad.descripcion}</p>
                          </div>
                          <span className="text-xs font-medium text-blue-700">
                            {actividad.horasTrabajadas}h
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </Card>
              ))}

              {/* Observaciones */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Observaciones (opcional para aprobación, obligatorio para rechazo)
                </label>
                <textarea
                  value={observacion}
                  onChange={(e) => setObservacion(e.target.value)}
                  className="input-field"
                  rows="3"
                  placeholder="Ingrese sus observaciones..."
                />
              </div>

              {/* Acciones */}
              <div className="flex gap-3 justify-end pt-4 border-t border-gray-200">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setBitacoraSeleccionada(null);
                    setObservacion('');
                  }}
                >
                  Cancelar
                </Button>
                <Button
                  variant="danger"
                  icon={X}
                  onClick={() => handleRevisar('RECHAZADA')}
                >
                  Rechazar
                </Button>
                <Button
                  variant="success"
                  icon={Check}
                  onClick={() => handleRevisar('APROBADA')}
                >
                  Aprobar
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}