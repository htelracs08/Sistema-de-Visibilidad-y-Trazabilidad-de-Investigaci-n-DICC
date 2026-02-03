import { useEffect, useState } from 'react';
import { BarChart3, PieChart, TrendingUp } from 'lucide-react';
import Card from '../../components/common/Card';
import { jefaturaService } from '../../services/api';

export default function JefaturaEstadisticas() {
  const [estadisticas, setEstadisticas] = useState({
    proyectos: null,
    ayudantes: null,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarEstadisticas();
  }, []);

  const cargarEstadisticas = async () => {
    try {
      const [proyectos, ayudantes] = await Promise.all([
        jefaturaService.getEstadisticasProyectos(),
        jefaturaService.getEstadisticasAyudantes(),
      ]);
      setEstadisticas({ proyectos, ayudantes });
    } catch (error) {
      console.error('Error al cargar estadísticas:', error);
      alert('Error al cargar las estadísticas');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-epn-blue"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <BarChart3 className="w-8 h-8 text-epn-blue" />
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Estadísticas</h1>
          <p className="text-gray-600 mt-2">Análisis y reportes del sistema</p>
        </div>
      </div>

      {/* Estadísticas de Proyectos */}
      <Card title="Proyectos" subtitle="Distribución y estado de proyectos">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-blue-50 p-4 rounded-lg">
            <div className="flex items-center gap-3">
              <PieChart className="w-8 h-8 text-blue-600" />
              <div>
                <p className="text-sm text-gray-600">Total Proyectos</p>
                <p className="text-2xl font-bold text-gray-900">
                  {estadisticas.proyectos?.total || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-green-50 p-4 rounded-lg">
            <div className="flex items-center gap-3">
              <TrendingUp className="w-8 h-8 text-green-600" />
              <div>
                <p className="text-sm text-gray-600">Proyectos Activos</p>
                <p className="text-2xl font-bold text-gray-900">
                  {estadisticas.proyectos?.activos || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-gray-50 p-4 rounded-lg">
            <div className="flex items-center gap-3">
              <BarChart3 className="w-8 h-8 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Proyectos Inactivos</p>
                <p className="text-2xl font-bold text-gray-900">
                  {estadisticas.proyectos?.inactivos || 0}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Distribución por tipo */}
        <div className="mt-6">
          <h4 className="font-semibold text-gray-900 mb-4">Distribución por Tipo</h4>
          <div className="space-y-3">
            {estadisticas.proyectos?.porTipo?.map((tipo, index) => (
              <div key={index}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-700">{tipo.tipo}</span>
                  <span className="font-medium text-gray-900">{tipo.cantidad}</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-epn-blue h-2 rounded-full"
                    style={{
                      width: `${(tipo.cantidad / estadisticas.proyectos.total) * 100}%`,
                    }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Card>

      {/* Estadísticas de Ayudantes */}
      <Card title="Ayudantes" subtitle="Información sobre ayudantes de investigación">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-purple-50 p-4 rounded-lg">
            <div className="flex items-center gap-3">
              <TrendingUp className="w-8 h-8 text-purple-600" />
              <div>
                <p className="text-sm text-gray-600">Ayudantes Activos</p>
                <p className="text-2xl font-bold text-gray-900">
                  {estadisticas.ayudantes?.activos || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-orange-50 p-4 rounded-lg">
            <div className="flex items-center gap-3">
              <BarChart3 className="w-8 h-8 text-orange-600" />
              <div>
                <p className="text-sm text-gray-600">Bitácoras del Mes</p>
                <p className="text-2xl font-bold text-gray-900">
                  {estadisticas.ayudantes?.bitacorasMes || 0}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Distribución por tipo de ayudante */}
        {estadisticas.ayudantes?.porTipo && (
          <div className="mt-6">
            <h4 className="font-semibold text-gray-900 mb-4">Distribución por Tipo de Ayudante</h4>
            <div className="space-y-3">
              {estadisticas.ayudantes.porTipo.map((tipo, index) => (
                <div key={index}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-700">{tipo.tipo}</span>
                    <span className="font-medium text-gray-900">{tipo.cantidad}</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-purple-600 h-2 rounded-full"
                      style={{
                        width: `${(tipo.cantidad / estadisticas.ayudantes.activos) * 100}%`,
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}