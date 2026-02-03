import { useEffect, useState } from 'react';
import { BookOpen, Clock, CheckCircle, AlertCircle } from 'lucide-react';
import Card from '../../components/common/Card';
import { ayudanteService } from '../../services/api';
import { getUser } from '../../utils/auth';

export default function AyudanteDashboard() {
  const user = getUser();
  const [bitacoraActual, setBitacoraActual] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarBitacoraActual();
  }, []);

  const cargarBitacoraActual = async () => {
    try {
      const data = await ayudanteService.getBitacoraActual();
      setBitacoraActual(data);
    } catch (error) {
      console.error('Error al cargar bitácora:', error);
    } finally {
      setLoading(false);
    }
  };

  const getEstadoInfo = () => {
    if (!bitacoraActual) {
      return {
        color: 'bg-gray-100 text-gray-800',
        icon: AlertCircle,
        texto: 'Sin bitácora activa',
      };
    }

    switch (bitacoraActual.estado) {
      case 'BORRADOR':
        return {
          color: 'bg-blue-100 text-blue-800',
          icon: BookOpen,
          texto: 'En edición',
        };
      case 'ENVIADA':
        return {
          color: 'bg-yellow-100 text-yellow-800',
          icon: Clock,
          texto: 'Pendiente de revisión',
        };
      case 'APROBADA':
        return {
          color: 'bg-green-100 text-green-800',
          icon: CheckCircle,
          texto: 'Aprobada',
        };
      case 'RECHAZADA':
        return {
          color: 'bg-red-100 text-red-800',
          icon: AlertCircle,
          texto: 'Rechazada',
        };
      default:
        return {
          color: 'bg-gray-100 text-gray-800',
          icon: AlertCircle,
          texto: 'Desconocido',
        };
    }
  };

  const estadoInfo = getEstadoInfo();
  const Icon = estadoInfo.icon;

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
        <h1 className="text-3xl font-bold text-gray-900">Bienvenido, {user?.nombres}</h1>
        <p className="text-gray-600 mt-2">Gestiona tu bitácora mensual</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card title="Bitácora Actual">
          <div className="space-y-4">
            {bitacoraActual ? (
              <>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Período:</span>
                  <span className="font-semibold">
                    {bitacoraActual.mes}/{bitacoraActual.anio}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Estado:</span>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${estadoInfo.color} flex items-center gap-2`}>
                    <Icon className="w-4 h-4" />
                    {estadoInfo.texto}
                  </span>
                </div>
                {bitacoraActual.observacion && (
                  <div className="p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                    <p className="text-sm font-medium text-yellow-800">Observaciones del Director:</p>
                    <p className="text-sm text-yellow-700 mt-1">{bitacoraActual.observacion}</p>
                  </div>
                )}
                <a
                  href="/ayudante/bitacora"
                  className="block w-full btn-primary text-center"
                >
                  Ir a Mi Bitácora
                </a>
              </>
            ) : (
              <div className="text-center py-8">
                <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                <p className="text-gray-600">No tienes una bitácora activa</p>
                <p className="text-sm text-gray-500 mt-1">
                  Se creará automáticamente cuando sea necesario
                </p>
              </div>
            )}
          </div>
        </Card>

        <Card title="Accesos Rápidos">
          <div className="space-y-3">
            <a
              href="/ayudante/bitacora"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900 flex items-center gap-2">
                <BookOpen className="w-5 h-5 text-epn-blue" />
                Mi Bitácora
              </h4>
              <p className="text-sm text-gray-600 mt-1">
                Registra tus actividades y avances del mes
              </p>
            </a>
            <a
              href="/ayudante/historial"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                Historial
              </h4>
              <p className="text-sm text-gray-600 mt-1">
                Ver bitácoras aprobadas anteriores
              </p>
            </a>
          </div>
        </Card>
      </div>

      <Card title="Información Importante">
        <div className="space-y-3 text-sm text-gray-700">
          <div className="flex items-start gap-2">
            <div className="w-2 h-2 bg-epn-blue rounded-full mt-1.5"></div>
            <p>Debes registrar tus actividades semanalmente en la bitácora</p>
          </div>
          <div className="flex items-start gap-2">
            <div className="w-2 h-2 bg-epn-blue rounded-full mt-1.5"></div>
            <p>Al finalizar el mes, envía tu bitácora para revisión del director</p>
          </div>
          <div className="flex items-start gap-2">
            <div className="w-2 h-2 bg-epn-blue rounded-full mt-1.5"></div>
            <p>Si tu bitácora es rechazada, podrás reabrirla y hacer las correcciones necesarias</p>
          </div>
        </div>
      </Card>
    </div>
  );
}