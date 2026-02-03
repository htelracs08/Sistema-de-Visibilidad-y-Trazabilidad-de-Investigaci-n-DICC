import { LogOut, User, Settings } from 'lucide-react';
import { logout, getUser } from '../../utils/auth';
import { useState } from 'react';

export default function Navbar() {
  const user = getUser();
  const [showMenu, setShowMenu] = useState(false);

  const handleLogout = () => {
    if (window.confirm('¿Estás seguro de cerrar sesión?')) {
      logout();
    }
  };

  const getRoleName = (rol) => {
    switch (rol) {
      case 'JEFATURA': return 'Jefatura';
      case 'DIRECTOR': return 'Director';
      case 'AYUDANTE': return 'Ayudante';
      default: return rol;
    }
  };

  return (
    <nav className="bg-white border-b border-gray-200 fixed w-full z-30 top-0">
      <div className="px-6 py-3">
        <div className="flex items-center justify-between">
          {/* Logo y título */}
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-epn-blue rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">EPN</span>
            </div>
            <div>
              <h1 className="text-xl font-bold text-epn-blue">Sistema DICC</h1>
              <p className="text-xs text-gray-500">Escuela Politécnica Nacional</p>
            </div>
          </div>

          {/* Usuario */}
          <div className="relative">
            <button
              onClick={() => setShowMenu(!showMenu)}
              className="flex items-center gap-3 px-4 py-2 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <div className="text-right">
                <p className="text-sm font-medium text-gray-900">
                  {user?.nombres} {user?.apellidos}
                </p>
                <p className="text-xs text-gray-500">{getRoleName(user?.rol)}</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-epn-blue flex items-center justify-center">
                <User className="w-6 h-6 text-white" />
              </div>
            </button>

            {/* Menú desplegable */}
            {showMenu && (
              <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                <div className="px-4 py-3 border-b border-gray-100">
                  <p className="text-sm font-medium text-gray-900">{user?.correo}</p>
                </div>
                <button
                  onClick={handleLogout}
                  className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                >
                  <LogOut className="w-4 h-4" />
                  Cerrar sesión
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}