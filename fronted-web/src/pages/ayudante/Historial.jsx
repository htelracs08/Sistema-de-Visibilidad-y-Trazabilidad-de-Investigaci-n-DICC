import { useEffect, useState } from 'react';
import { Eye } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import { ayudanteService } from '../../services/api';

export default function AyudanteHistorial() {
  const [bitacoras, setBitacoras] = useState([]);
  const [bitacoraSeleccionada, setBitacoraSeleccionada] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarHistorial();
  }, []);

  const cargarHistorial = async () => {
    try {
      const data = await ayudanteService.getBitacorasAprobadas();
      setBitacoras(data);
    } catch (error) {
      console.error('Error al cargar historial:', error);
      alert('Error al cargar el historial');
    } finally {
      setLoading(false);
    }
  };

  const handleVerDetalle = async (bitacoraId) => {
    try {
      const detalle = await ayudanteService.getBitacora(bitacoraId);
      setBitacoraSeleccionada(detalle);
    } catch (error) {
      console.error('Error al cargar detalle:', error);
      alert('Error al cargar el detalle');
    }
  };

  const columns = [
    {
      header: 'Período',
      render: (row) => `${row.mes}/${row.anio}`,
    },
    {
      header: 'Fecha Aprobación',
      accessor: 'fechaAprobacion',
    },
    {
      header: 'Semanas',
      render: (row) => row.semanas?.length || 0,
    },
    {
      header: 'Estado',
      render: (row) => (
        <span className="px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
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
        <h1 className="text-3xl font-bold text-gray-900">Historial de Bitácoras</h1>
        <p className="text-gray-600 mt-2">Consulta tus bitácoras aprobadas</p>
      </div>

      <Card>
        {bitacoras.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">No tienes bitácoras aprobadas aún</p>
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
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">
                    Bitácora {bitacoraSeleccionada.mes}/{bitacoraSeleccionada.anio}
                  </h2>
                  <p className="text-sm text-gray-600 mt-1">
                    Aprobada el {bitacoraSeleccionada.fechaAprobacion}
                  </p>
                </div>
                <Button
                  variant="secondary"
                  onClick={() => setBitacoraSeleccionada(null)}
                >
                  Cerrar
                </Button>
              </div>
            </div>

            <div className="p-6 space-y-6">
              {bitacoraSeleccionada.observacion && (
                <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                  <p className="text-sm font-medium text-green-800">Observaciones del Director:</p>
                  <p className="text-sm text-green-700 mt-1">{bitacoraSeleccionada.observacion}</p>
                </div>
              )}

              {/* Semanas */}
              {bitacoraSeleccionada.semanas?.map((semana, index) => (
                <Card key={index} title={`Semana ${semana.numero}`}>
                  <div className="space-y-4">
                    <div>
                      <p className="text-sm text-gray-600">
                        {new Date(semana.fechaInicio).toLocaleDateString()} -{' '}
                        {new Date(semana.fechaFin).toLocaleDateString()}
                      </p>
                    </div>

                    {/* Informes */}
                    {semana.informes?.length > 0 && (
                      <div>
                        <h4 className="font-semibold text-gray-900 mb-2">Informes:</h4>
                        {semana.informes.map((informe, idx) => (
                          <div key={idx} className="p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-700">{informe.descripcion}</p>
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Actividades */}
                    {semana.actividades?.length > 0 && (
                      <div>
                        <h4 className="font-semibold text-gray-900 mb-2">Actividades:</h4>
                        <div className="space-y-2">
                          {semana.actividades.map((actividad, idx) => (
                            <div
                              key={idx}
                              className="p-3 bg-blue-50 rounded-lg flex justify-between items-start"
                            >
                              <div>
                                <p className="text-sm font-medium text-gray-900">
                                  {new Date(actividad.fecha).toLocaleDateString()}
                                </p>
                                <p className="text-sm text-gray-700 mt-1">{actividad.descripcion}</p>
                              </div>
                              <span className="text-xs font-semibold text-blue-700 bg-blue-200 px-2 py-1 rounded">
                                {actividad.horasTrabajadas}h
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}