// import React from "react";
// import { NavLink, Outlet, useNavigate } from "react-router-dom";
// import BrandHeader from "../../components/BrandHeader.jsx";
// import { clearAuth } from "../../lib/auth";

// export default function JefaturaLayout() {
//   const nav = useNavigate();
//   function logout() {
//     clearAuth();
//     nav("/login");
//   }

//   const linkCls = ({ isActive }) =>
//     "block px-4 py-2 rounded-xl " + (isActive ? "bg-poli-red text-white font-bold" : "hover:bg-white/60 text-poli-ink");

//   return (
//     <div className="min-h-screen bg-poli-gray">
//       <div className="max-w-6xl mx-auto p-4 md:p-8 space-y-4">
//         <BrandHeader subtitle="Panel Jefatura" />

//         <div className="grid md:grid-cols-[260px_1fr] gap-4">
//           <aside className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4">
//             <div className="font-bold text-poli-ink mb-3">Menú</div>
//             <nav className="space-y-2">
//               <NavLink className={linkCls} to="/jefatura/dashboard">Dashboard</NavLink>
//               <NavLink className={linkCls} to="/jefatura/proyectos">Proyectos</NavLink>
//               <NavLink className={linkCls} to="/jefatura/semaforo">Semáforo</NavLink>
//               <NavLink className={linkCls} to="/jefatura/estadisticas">Estadísticas</NavLink>
//             </nav>

//             <button onClick={logout} className="mt-4 w-full rounded-xl bg-poli-navy text-white py-2 font-bold">
//               Cerrar sesión
//             </button>
//           </aside>

//           <main className="space-y-4">
//             <Outlet />
//           </main>
//         </div>
//       </div>
//     </div>
//   );
// }
import React, { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import BrandHeader from "../../components/BrandHeader.jsx";
import ChangePasswordModal from "../../components/ChangePasswordModal.jsx";
import { clearAuth } from "../../lib/auth";

const menuItems = [
  { path: "/jefatura/dashboard", label: "Dashboard", icon: "📊" },
  { path: "/jefatura/proyectos", label: "Proyectos", icon: "📁" },
  { path: "/jefatura/semaforo", label: "Semáforo", icon: "🚦" },
  { path: "/jefatura/estadisticas", label: "Estadísticas", icon: "📈" }
];

export default function JefaturaLayout() {
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
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100">
      <div className="max-w-7xl mx-auto p-4 md:p-8 space-y-4">
        <BrandHeader subtitle="Panel Jefatura DICC" />

        <div className="grid md:grid-cols-[280px_1fr] gap-4">
          <aside className="rounded-2xl bg-white border border-gray-200 shadow-lg p-5">
            <div className="flex items-center gap-2 mb-4 pb-4 border-b border-gray-200">
              <span className="text-2xl">📊</span>
              <div>
                <div className="font-bold text-poli-ink">Jefatura</div>
                <div className="text-xs text-gray-500">Panel de control</div>
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

            {/* 🔐 BOTONES DE ACCIÓN */}
            <div className="mt-6 pt-6 border-t border-gray-200 space-y-2">
              <button
                onClick={() => setShowChangePassword(true)}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 text-white py-3 font-bold hover:shadow-lg transition-all duration-200 hover:scale-105"
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