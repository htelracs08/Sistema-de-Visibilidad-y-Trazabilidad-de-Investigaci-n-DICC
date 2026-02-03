import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  FolderKanban, 
  Users, 
  FileText, 
  BarChart3,
  AlertCircle,
  BookOpen,
  History
} from 'lucide-react';
import { getUser } from '../../utils/auth';

export default function Sidebar() {
  const user = getUser();

  const menuItems = {
    JEFATURA: [
      { path: '/jefatura/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
      { path: '/jefatura/proyectos', icon: FolderKanban, label: 'Proyectos' },
      { path: '/jefatura/semaforo', icon: AlertCircle, label: 'Semáforo' },
      { path: '/jefatura/estadisticas', icon: BarChart3, label: 'Estadísticas' },
    ],
    DIRECTOR: [
      { path: '/director/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
      { path: '/director/proyectos', icon: FolderKanban, label: 'Mis Proyectos' },
      { path: '/director/ayudantes', icon: Users, label: 'Ayudantes' },
      { path: '/director/bitacoras', icon: FileText, label: 'Bitácoras' },
    ],
    AYUDANTE: [
      { path: '/ayudante/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
      { path: '/ayudante/bitacora', icon: BookOpen, label: 'Mi Bitácora' },
      { path: '/ayudante/historial', icon: History, label: 'Historial' },
    ],
  };

  const items = menuItems[user?.rol] || [];

  return (
    <aside className="fixed left-0 top-16 w-64 h-[calc(100vh-4rem)] bg-white border-r border-gray-200 overflow-y-auto">
      <nav className="p-4 space-y-1">
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                isActive
                  ? 'bg-epn-blue text-white'
                  : 'text-gray-700 hover:bg-gray-100'
              }`
            }
          >
            <item.icon className="w-5 h-5" />
            <span className="font-medium">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}