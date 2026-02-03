import { useEffect, useState } from 'react';
import { AlertCircle } from 'lucide-react';
import Card from '../../components/common/Card';
import { jefaturaService } from '../../services/api';
import { COLORES_SEMAFORO } from '../../utils/constants';

export default function JefaturaSemaforo() {
  const [semaforo, setSemaforo] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarSemaforo();
  }, []);

  const cargarSemaforo = async () => {
    try {
      const data = await jefaturaService.getSemaforo();
      setSemaforo(data);
    } catch (error) {
      console.error('Error al cargar semáforo:', error);
      alert('Error al cargar el semáforo');
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
        <AlertCircle className="w-8 h-8 text-epn-blue" />
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Semáforo de Bitácoras</h1>
          <p className="text-gray-600 mt-2">Monitoreo del estado de entrega de bitácoras mensuales</p>
        </div>
      </div>

      {/* Leyenda */}
      <Card>
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-green-500 rounded-full"></div>
            <span className="text-sm font-medium">Al día (Verde)</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-yellow-500 rounded-full"></div>
            <span className="text-sm font-medium">Próximo a vencer (Amarillo)</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-red-500 rounded-full"></div>
            <span className="text-sm font-medium">Atrasado (Rojo)</span>
          </div>
        </div>
      </Card>

      {/* Tabla de semáforo */}
      <div className="grid gap-4">
        {semaforo.length === 0 ? (
          <Card>
            <p className="text-center text-gray-500 py-8">No hay datos de semáforo disponibles</p>
          </Card>
        ) : (
          semaforo.map((proyecto) => (
            <Card key={proyecto.proyectoId}>
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">{proyecto.proyectoNombre}</h3>
                  <p className="text-sm text-gray-600">Código: {proyecto.proyectoCodigo}</p>
                </div>

                <div className="overflow-x-auto">
                  <table className="min-w-full">
                    <thead>
                      <tr className="border-b border-gray-200">
                        <th className="text-left py-2 px-4 text-sm font-medium text-gray-700">Ayudante</th>
                        <th className="text-left py-2 px-4 text-sm font-medium text-gray-700">Última Bitácora</th>
                        <th className="text-left py-2 px-4 text-sm font-medium text-gray-700">Estado</th>
                        <th className="text-left py-2 px-4 text-sm font-medium text-gray-700">Días</th>
                      </tr>
                    </thead>
                    <tbody>
                      {proyecto.ayudantes?.map((ayudante, index) => {
                        const color = ayudante.color || 'VERDE';
                        const colorStyles = COLORES_SEMAFORO[color];
                        
                        return (
                          <tr key={index} className="border-b border-gray-100 last:border-0">
                            <td className="py-3 px-4 text-sm text-gray-900">
                              {ayudante.nombres} {ayudante.apellidos}
                            </td>
                            <td className="py-3 px-4 text-sm text-gray-600">
                              {ayudante.ultimaBitacora || 'Sin bitácoras'}
                            </td>
                            <td className="py-3 px-4">
                              <span className={`px-3 py-1 rounded-full text-xs font-medium ${colorStyles.bg} ${colorStyles.text} border ${colorStyles.border}`}>
                                {ayudante.estadoTexto || color}
                              </span>
                            </td>
                            <td className="py-3 px-4 text-sm text-gray-600">
                              {ayudante.diasDesdeUltima !== undefined ? `${ayudante.diasDesdeUltima} días` : '-'}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}