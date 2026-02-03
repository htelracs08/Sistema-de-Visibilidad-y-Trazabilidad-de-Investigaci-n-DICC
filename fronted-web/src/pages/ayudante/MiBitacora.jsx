import { useEffect, useState } from 'react';
import { Plus, Send, Edit } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import { ayudanteService } from '../../services/api';

export default function AyudanteMiBitacora() {
  const [bitacora, setBitacora] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showSemanaModal, setShowSemanaModal] = useState(false);
  const [showActividadModal, setShowActividadModal] = useState(false);
  const [semanaSeleccionada, setSemanaSeleccionada] = useState(null);
  const [formSemana, setFormSemana] = useState({
    numero: '',
    fechaInicio: '',
    fechaFin: '',
    informe: '',
  });
  const [formActividad, setFormActividad] = useState({
    fecha: '',
    descripcion: '',
    horasTrabajadas: '',
  });

  useEffect(() => {
    cargarBitacora();
  }, []);

  const cargarBitacora = async () => {
    try {
      const data = await ayudanteService.getBitacoraActual();
      setBitacora(data);
    } catch (error) {
      console.error('Error al cargar bitácora:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCrearSemana = async (e) => {
    e.preventDefault();
    try {
      await ayudanteService.crearSemana(bitacora.id, {
        numero: parseInt(formSemana.numero),
        fechaInicio: formSemana.fechaInicio,
        fechaFin: formSemana.fechaFin,
        informes: formSemana.informe ? [{ descripcion: formSemana.informe }] : [],
      });
      alert('Semana creada exitosamente');
      setShowSemanaModal(false);
      setFormSemana({ numero: '', fechaInicio: '', fechaFin: '', informe: '' });
      cargarBitacora();
    } catch (error) {
      console.error('Error al crear semana:', error);
      alert('Error al crear la semana');
    }
  };

  const handleCrearActividad = async (e) => {
    e.preventDefault();
    try {
      await ayudanteService.crearActividad(semanaSeleccionada.id, formActividad);
      alert('Actividad registrada exitosamente');
      setShowActividadModal(false);
      setSemanaSeleccionada(null);
      setFormActividad({ fecha: '', descripcion: '', horasTrabajadas: '' });
      cargarBitacora();
    } catch (error) {
      console.error('Error al crear actividad:', error);
      alert('Error al crear la actividad');
    }
  };

  const handleEnviarBitacora = async () => {
    if (!window.confirm('¿Estás seguro de enviar la bitácora para revisión?')) return;

    try {
      await ayudanteService.enviarBitacora(bitacora.id);
      alert('Bitácora enviada exitosamente');
      cargarBitacora();
    } catch (error) {
      console.error('Error al enviar bitácora:', error);
      alert('Error al enviar la bitácora');
    }
  };

  const handleReabrirBitacora = async () => {
    if (!window.confirm('¿Deseas reabrir la bitácora para hacer correcciones?')) return;

    try {
      await ayudanteService.reabrirBitacora(bitacora.id);
      alert('Bitácora reabierta');
      cargarBitacora();
    } catch (error) {
      console.error('Error al reabrir bitácora:', error);
      alert('Error al reabrir la bitácora');
    }
  };

  const puedeEditar = bitacora?.estado === 'BORRADOR';
  const puedeEnviar = bitacora?.estado === 'BORRADOR' && bitacora?.semanas?.length > 0;
  const puedeReabrir = bitacora?.estado === 'RECHAZADA';

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-epn-blue"></div>
      </div>
    );
  }

  if (!bitacora) {
    return (
      <div className="flex items-center justify-center h-64">
        <Card>
          <p className="text-gray-500 text-center">No tienes una bitácora activa</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Bitácora {bitacora.mes}/{bitacora.anio}
          </h1>
          <p className="text-gray-600 mt-2">Registra tus actividades semanales</p>
        </div>
        <div className="flex gap-3">
          {puedeReabrir && (
            <Button icon={Edit} onClick={handleReabrirBitacora}>
              Reabrir
            </Button>
          )}
          {puedeEditar && (
            <Button icon={Plus} onClick={() => setShowSemanaModal(true)}>
              Nueva Semana
            </Button>
          )}
          {puedeEnviar && (
            <Button icon={Send} onClick={handleEnviarBitacora}>
              Enviar para Revisión
            </Button>
          )}
        </div>
      </div>

      {/* Estado */}
      <Card>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600">Estado actual:</p>
            <p className={`text-lg font-semibold mt-1 ${
              bitacora.estado === 'APROBADA' ? 'text-green-600' :
              bitacora.estado === 'RECHAZADA' ? 'text-red-600' :
              bitacora.estado === 'ENVIADA' ? 'text-yellow-600' :
              'text-blue-600'
            }`}>
              {bitacora.estado}
            </p>
          </div>
          {bitacora.observacion && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 max-w-md">
              <p className="text-sm font-medium text-yellow-800">Observaciones:</p>
              <p className="text-sm text-yellow-700 mt-1">{bitacora.observacion}</p>
            </div>
          )}
        </div>
      </Card>

      {/* Semanas */}
      {bitacora.semanas?.map((semana, index) => (
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
            <div>
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-semibold text-gray-900">Actividades:</h4>
                {puedeEditar && (
                  <Button
                    size="sm"
                    variant="outline"
                    icon={Plus}
                    onClick={() => {
                      setSemanaSeleccionada(semana);
                      setShowActividadModal(true);
                    }}
                  >
                    Agregar
                  </Button>
                )}
              </div>
              {semana.actividades?.length === 0 ? (
                <p className="text-sm text-gray-500 italic">No hay actividades registradas</p>
              ) : (
                <div className="space-y-2">
                  {semana.actividades?.map((actividad, idx) => (
                    <div key={idx} className="p-3 bg-blue-50 rounded-lg flex justify-between items-start">
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
              )}
            </div>
          </div>
        </Card>
      ))}

      {bitacora.semanas?.length === 0 && (
        <Card>
          <p className="text-center text-gray-500 py-8">
            No hay semanas registradas. Crea tu primera semana para comenzar.
          </p>
        </Card>
      )}

      {/* Modal Nueva Semana */}
      {showSemanaModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-bold">Nueva Semana</h2>
            </div>
            <form onSubmit={handleCrearSemana} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Número de Semana *
                </label>
                <input
                  type="number"
                  value={formSemana.numero}
                  onChange={(e) => setFormSemana({ ...formSemana, numero: e.target.value })}
                  className="input-field"
                  required
                  min="1"
                  max="4"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Fecha Inicio *
                </label>
                <input
                  type="date"
                  value={formSemana.fechaInicio}
                  onChange={(e) => setFormSemana({ ...formSemana, fechaInicio: e.target.value })}
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Fecha Fin *
                </label>
                <input
                  type="date"
                  value={formSemana.fechaFin}
                  onChange={(e) => setFormSemana({ ...formSemana, fechaFin: e.target.value })}
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Informe (opcional)
                </label>
                <textarea
                  value={formSemana.informe}
                  onChange={(e) => setFormSemana({ ...formSemana, informe: e.target.value })}
                  className="input-field"
                  rows="3"
                />
              </div>
              <div className="flex gap-3 justify-end">
                <Button variant="secondary" onClick={() => setShowSemanaModal(false)} type="button">
                  Cancelar
                </Button>
                <Button type="submit">Crear</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Nueva Actividad */}
      {showActividadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-bold">Nueva Actividad</h2>
            </div>
            <form onSubmit={handleCrearActividad} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Fecha *
                </label>
                <input
                  type="date"
                  value={formActividad.fecha}
                  onChange={(e) => setFormActividad({ ...formActividad, fecha: e.target.value })}
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Descripción *
                </label>
                <textarea
                  value={formActividad.descripcion}
                  onChange={(e) => setFormActividad({ ...formActividad, descripcion: e.target.value })}
                  className="input-field"
                  rows="3"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Horas Trabajadas *
                </label>
                <input
                  type="number"
                  value={formActividad.horasTrabajadas}
                  onChange={(e) => setFormActividad({ ...formActividad, horasTrabajadas: e.target.value })}
                  className="input-field"
                  required
                  min="0.5"
                  step="0.5"
                />
              </div>
              <div className="flex gap-3 justify-end">
                <Button
                  variant="secondary"
                  onClick={() => {
                    setShowActividadModal(false);
                    setSemanaSeleccionada(null);
                  }}
                  type="button"
                >
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