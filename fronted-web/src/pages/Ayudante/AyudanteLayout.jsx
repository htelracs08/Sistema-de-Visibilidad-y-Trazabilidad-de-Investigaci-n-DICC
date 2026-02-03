// import React from "react";
// import { NavLink, Outlet, useNavigate } from "react-router-dom";
// import BrandHeader from "../../components/BrandHeader.jsx";
// import { clearAuth } from "../../lib/auth";

// export default function AyudanteLayout() {
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
//         <BrandHeader subtitle="Panel Ayudante" />

//         <div className="grid md:grid-cols-[260px_1fr] gap-4">
//           <aside className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4">
//             <div className="font-bold text-poli-ink mb-3">Menú</div>
//             <nav className="space-y-2">
//               <NavLink className={linkCls} to="/ayudante/bitacora-actual">Bitácora actual</NavLink>
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
import React from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import BrandHeader from "../../components/BrandHeader.jsx";
import { clearAuth } from "../../lib/auth";

export default function AyudanteLayout() {
  const nav = useNavigate();
  
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
              <NavLink className={linkCls} to="/ayudante/bitacora-actual">
                <span className="text-xl">📝</span>
                <span>Bitácora Actual</span>
              </NavLink>
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

          <main className="space-y-4">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}