import { useEffect, useState } from 'react';
import { FolderKanban, Users, FileText, Clock } from 'lucide-react';
import Card from '../../components/common/Card';
import { directorService } from '../../services/api';

export default function DirectorDashboard() {
  const [stats, setStats] = useState({
    totalProyectos: 0,
    ayudantesActivos: 0,
    bitacorasPendientes: 0,
  });
  const [proyectos, setProyectos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      const proyectosData = await directorService.getMisProyectos();
      setProyectos(proyectosData);

      let totalAyudantes = 0;
      let totalPendientes = 0;

      for (const proyecto of proyectosData) {
        const ayudantes = await directorService.getAyudantesProyecto(proyecto.id);
        totalAyudantes += ayudantes.filter(a => a.estado === 'ACTIVO').length;

        const pendientes = await directorService.getBitacorasPendientes(proyecto.id);
        totalPendientes += pendientes.length;
      }

      setStats({
        totalProyectos: proyectosData.length,
        ayudantesActivos: totalAyudantes,
        bitacorasPendientes: totalPendientes,
      });
    } catch (error) {
      console.error('Error al cargar datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      title: 'Mis Proyectos',
      value: stats.totalProyectos,
      icon: FolderKanban,
      color: 'bg-blue-500',
    },
    {
      title: 'Ayudantes Activos',
      value: stats.ayudantesActivos,
      icon: Users,
      color: 'bg-purple-500',
    },
    {
      title: 'Bitácoras Pendientes',
      value: stats.bitacorasPendientes,
      icon: Clock,
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
        <p className="text-gray-600 mt-2">Resumen de tus proyectos</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
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
        <Card title="Mis Proyectos">
          <div className="space-y-3">
            {proyectos.length === 0 ? (
              <p className="text-gray-500 text-center py-4">No tienes proyectos asignados</p>
            ) : (
              proyectos.map((proyecto) => (
                <a
                  key={proyecto.id}
                  href={`/director/proyectos`}
                  className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <h4 className="font-semibold text-gray-900">{proyecto.nombre}</h4>
                  <p className="text-sm text-gray-600 mt-1">Código: {proyecto.codigo}</p>
                  <div className="flex items-center gap-2 mt-2">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      proyecto.activo ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                    }`}>
                      {proyecto.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </div>
                </a>
              ))
            )}
          </div>
        </Card>

        <Card title="Accesos Rápidos">
          <div className="space-y-3">
            <a
              href="/director/proyectos"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Gestionar Proyectos</h4>
              <p className="text-sm text-gray-600 mt-1">Ver y editar información de proyectos</p>
            </a>
            <a
              href="/director/ayudantes"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Administrar Ayudantes</h4>
              <p className="text-sm text-gray-600 mt-1">Registrar y gestionar ayudantes</p>
            </a>
            <a
              href="/director/bitacoras"
              className="block p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <h4 className="font-semibold text-gray-900">Revisar Bitácoras</h4>
              <p className="text-sm text-gray-600 mt-1">Aprobar o rechazar bitácoras pendientes</p>
            </a>
          </div>
        </Card>
      </div>
    </div>
  );
}