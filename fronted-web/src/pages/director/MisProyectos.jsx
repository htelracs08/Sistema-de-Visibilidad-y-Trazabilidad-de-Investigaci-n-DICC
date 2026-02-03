import { useEffect, useState } from 'react';
import { Edit, Users, FileText } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Table from '../../components/common/Table';
import { directorService } from '../../services/api';

export default function DirectorMisProyectos() {
  const navigate = useNavigate();
  const [proyectos, setProyectos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarProyectos();
  }, []);

  const cargarProyectos = async () => {
    try {
      const data = await directorService.getMisProyectos();
      setProyectos(data);
    } catch (error) {
      console.error('Error al cargar proyectos:', error);
      alert('Error al cargar los proyectos');
    } finally {
      setLoading(false);
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
      header: 'Subtipo',
      accessor: 'subtipo',
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
        <div className="flex gap-2">
          <Button 
            variant="outline" 
            size="sm" 
            icon={Users}
            onClick={() => navigate(`/director/ayudantes/${row.id}`)}
          >
            Ayudantes
          </Button>
          <Button 
            variant="outline" 
            size="sm" 
            icon={FileText}
            onClick={() => navigate(`/director/bitacoras/${row.id}`)}
          >
            Bitácoras
          </Button>
        </div>
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
        <h1 className="text-3xl font-bold text-gray-900">Mis Proyectos</h1>
        <p className="text-gray-600 mt-2">Administra los proyectos que diriges</p>
      </div>

      <Card>
        {proyectos.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500">No tienes proyectos asignados</p>
          </div>
        ) : (
          <Table columns={columns} data={proyectos} />
        )}
      </Card>
    </div>
  );
}