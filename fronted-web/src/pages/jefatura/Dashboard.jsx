import { useEffect, useState } from 'react';
import { FolderKanban, Users, FileCheck, TrendingUp } from 'lucide-react';
import Card from '../../components/common/Card';
import { jefaturaService } from '../../services/api';

export default function JefaturaDashboard() {
  const [stats, setStats] = useState({
    totalProyectos: 0,
    proyectosActivos: 0,
    totalAyudantes: 0,
    bitacorasPendientes: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarEstadisticas();
  }, []);

  const cargarEstadisticas = async () => {
    try {
      const [resumen, ayudantes] = await Promise.all([
        jefaturaService.getResumenProyectos(),
        jefaturaService.getAyudantesActivos(),
      ]);

      setStats({
        totalProyectos: resumen.total || 0,
        proyectosActivos: resumen.activos || 0,
        totalAyudantes: ayudantes.length || 0,
        bitacorasPendientes: resumen.bitacorasPendientes || 0,
      });
    } catch (error) {
      console.error('Error al cargar estadísticas:', error);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      title: 'Total Proyectos',
      value: stats.totalProyectos,
      icon: FolderKanban,
      color: 'bg-blue-500',
    },
    {
      title: 'Proyectos Activos',
      value: stats.proyectosActivos,
      icon: TrendingUp,
      color: 'bg-green-500',
    },
    {
      title: 'Ayudantes Activos',
      value: stats.totalAyudantes,
      icon: Users,
      color: 'bg-purple-500',
    },
    {
      title: 'Bitácoras Pendientes',
      value: stats.bitacorasPendientes,
      icon: FileCheck,
      color: 'bg-orange-500',
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
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-2">Resumen general del sistema</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <Card key={index} className="hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
              </div>
              <div className={`${stat.color} p-3 rounded-lg`}>
                <stat.icon className="w-8 h-8 text-white" />
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Accesos Rápidos">
          <div className="space-y-3">
            <a
              href="/jefatura/proyectos"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Gestionar Proyectos</h4>
              <p className="text-sm text-gray-600 mt-1">Crear y administrar proyectos de investigación</p>
            </a>
            <a
              href="/jefatura/semaforo"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Ver Semáforo</h4>
              <p className="text-sm text-gray-600 mt-1">Monitorear el estado de las bitácoras</p>
            </a>
            <a
              href="/jefatura/estadisticas"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Estadísticas</h4>
              <p className="text-sm text-gray-600 mt-1">Ver reportes y análisis detallados</p>
            </a>
          </div>
        </Card>

        <Card title="Actividad Reciente">
          <div className="space-y-3">
            <div className="flex items-start gap-3 p-3 bg-blue-50 rounded-lg">
              <div className="w-2 h-2 bg-blue-500 rounded-full mt-2"></div>
              <div>
                <p className="text-sm font-medium text-gray-900">Sistema actualizado</p>
                <p className="text-xs text-gray-600">Todos los módulos funcionando correctamente</p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}