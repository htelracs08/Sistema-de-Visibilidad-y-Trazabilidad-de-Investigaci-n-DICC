// import React from "react";
// import { NavLink, Outlet, useNavigate } from "react-router-dom";
// import BrandHeader from "../../components/BrandHeader.jsx";
// import { clearAuth } from "../../lib/auth";

// export default function DirectorLayout() {
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
//         <BrandHeader subtitle="Panel Director" />

//         <div className="grid md:grid-cols-[260px_1fr] gap-4">
//           <aside className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4">
//             <div className="font-bold text-poli-ink mb-3">Menú</div>
//             <nav className="space-y-2">
//               <NavLink className={linkCls} to="/director/proyectos">Proyectos</NavLink>
//               <NavLink className={linkCls} to="/director/ayudantes">Ayudantes</NavLink>
//               <NavLink className={linkCls} to="/director/bitacoras">Bitácoras</NavLink>
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
import React, { useMemo } from "react";
import { NavLink, Outlet, useNavigate, useLocation } from "react-router-dom";
import BrandHeader from "../../components/BrandHeader.jsx";
import { clearAuth } from "../../lib/auth";
import { getDirectorSelectedProject } from "../../lib/state";

const menuItems = [
  { 
    path: "/director/proyectos", 
    label: "Proyectos", 
    icon: "📁",
    title: "Gestión de Proyectos",
    description: "Administra tus proyectos de investigación"
  },
  { 
    path: "/director/ayudantes", 
    label: "Ayudantes", 
    icon: "👥",
    title: "Gestión de Ayudantes",
    description: "Registra y administra ayudantes del proyecto"
  },
  { 
    path: "/director/bitacoras", 
    label: "Bitácoras", 
    icon: "📋",
    title: "Revisión de Bitácoras",
    description: "Revisa y aprueba las bitácoras mensuales"
  },
    { 
    path: "/director/Historial", 
    label: "Historial", 
    icon: "📋",
    title: "Ver Historial de Bitácoras",
    description: "Consulta el historial de bitácoras enviadas"
  }
];

export default function DirectorLayout() {
  const nav = useNavigate();
  const location = useLocation();
  const selectedProject = getDirectorSelectedProject();

  const currentPage = useMemo(() => {
    return menuItems.find(item => location.pathname.startsWith(item.path)) || menuItems[0];
  }, [location.pathname]);

  function logout() {
    clearAuth();
    nav("/login");
  }

  const linkCls = ({ isActive }) =>
    "flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 " + 
    (isActive 
      ? "bg-gradient-to-r from-poli-red to-red-600 text-white font-bold shadow-lg" 
      : "hover:bg-poli-gray text-poli-ink hover:translate-x-1"
    );

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="max-w-7xl mx-auto p-4 md:p-8 space-y-4">
        <BrandHeader subtitle="Panel Director" />

        {/* Proyecto seleccionado banner */}
        {selectedProject && (
          <div className="rounded-2xl bg-gradient-to-r from-poli-navy to-blue-900 text-white shadow-lg p-4 border border-blue-800">
            <div className="flex items-center gap-3">
              <div className="text-2xl">🎯</div>
              <div>
                <div className="text-sm opacity-90">Proyecto activo:</div>
                <div className="font-bold">{selectedProject.codigo} - {selectedProject.nombre}</div>
              </div>
            </div>
          </div>
        )}

        <div className="grid md:grid-cols-[280px_1fr] gap-4">
          {/* Sidebar */}
          <aside className="rounded-2xl bg-white border border-gray-200 shadow-lg p-5">
            <div className="flex items-center gap-2 mb-4 pb-4 border-b border-gray-200">
              <span className="text-2xl">👔</span>
              <div>
                <div className="font-bold text-poli-ink">Director</div>
                <div className="text-xs text-gray-500">Panel de control</div>
              </div>
            </div>

            <nav className="space-y-2">
              {menuItems.map((item) => (
                <NavLink 
                  key={item.path} 
                  className={linkCls} 
                  to={item.path}
                >
                  <span className="text-xl">{item.icon}</span>
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </nav>

            <div className="mt-6 pt-6 border-t border-gray-200">
              <button 
                onClick={logout} 
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-poli-navy to-blue-900 text-white py-3 font-bold hover:shadow-lg transition-all duration-200 hover:scale-105"
              >
                <span>🚪</span>
                <span>Cerrar sesión</span>
              </button>
            </div>
          </aside>

          {/* Main content */}
          <main className="space-y-4">
            {/* Header dinámico de la página actual */}
            <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
              <div className="flex items-start gap-4">
                <div className="text-4xl">{currentPage.icon}</div>
                <div className="flex-1">
                  <h1 className="text-2xl font-bold text-poli-ink">
                    {currentPage.title}
                  </h1>
                  <p className="text-gray-600 mt-1">
                    {currentPage.description}
                  </p>
                </div>
              </div>
            </div>

            {/* Contenido de la ruta */}
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}