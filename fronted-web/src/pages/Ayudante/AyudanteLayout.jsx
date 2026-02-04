import React, { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import BrandHeader from "../../components/BrandHeader.jsx";
import ChangePasswordModal from "../../components/ChangePasswordModal.jsx";
import { clearAuth } from "../../lib/auth";

const menuItems = [
  { path: "/ayudante/bitacora-actual", label: "Bitácora Actual", icon: "📝" },
  { path: "/ayudante/historial", label: "Historial", icon: "📚" }
];

export default function AyudanteLayout() {
  const nav = useNavigate();
  const [showChangePassword, setShowChangePassword] = useState(false);
  
  function logout() {
    clearAuth();
    nav("/login");
  }

  const linkCls = ({ isActive }) =>
    "flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 " +
    (isActive
      ? "bg-gradient-to-r from-poli-red to-red-600 text-white font-bold shadow-lg"
      : "hover:bg-poli-gray text-poli-ink hover:translate-x-1");

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-purple-50 to-gray-100">
      <div className="max-w-7xl mx-auto p-4 md:p-8 space-y-4">
        <BrandHeader subtitle="Panel Ayudante de Investigación" />

        <div className="grid md:grid-cols-[280px_1fr] gap-4">
          <aside className="rounded-2xl bg-white border border-gray-200 shadow-lg p-5">
            <div className="flex items-center gap-2 mb-4 pb-4 border-b border-gray-200">
              <span className="text-2xl">🎓</span>
              <div>
                <div className="font-bold text-poli-ink">Ayudante</div>
                <div className="text-xs text-gray-500">Panel de bitácoras</div>
              </div>
            </div>

            <nav className="space-y-2">
              {menuItems.map((item) => (
                <NavLink key={item.path} className={linkCls} to={item.path}>
                  <span className="text-xl">{item.icon}</span>
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </nav>

            {/* 🔐 NUEVO: Botón de Cambiar Contraseña */}
            <div className="mt-6 pt-6 border-t border-gray-200">
              <button
                onClick={() => setShowChangePassword(true)}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 text-white py-3 font-bold hover:shadow-lg transition-all duration-200 hover:scale-105 mb-3"
              >
                <span>🔐</span>
                <span>Cambiar Contraseña</span>
              </button>

              <button
                onClick={logout}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-poli-navy to-blue-900 text-white py-3 font-bold hover:shadow-lg transition-all duration-200 hover:scale-105"
              >
                <span>🚪</span>
                <span>Cerrar sesión</span>
              </button>
            </div>
          </aside>

          <main className="space-y-4">
            <Outlet />
          </main>
        </div>
      </div>

      {/* 🔐 Modal de Cambio de Contraseña */}
      <ChangePasswordModal 
        open={showChangePassword} 
        onClose={() => setShowChangePassword(false)} 
      />
    </div>
  );
}