import React from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import BrandHeader from "../../components/BrandHeader.jsx";
import { clearAuth } from "../../lib/auth";

export default function DirectorLayout() {
  const nav = useNavigate();
  function logout() {
    clearAuth();
    nav("/login");
  }

  const linkCls = ({ isActive }) =>
    "block px-4 py-2 rounded-xl " + (isActive ? "bg-poli-red text-white font-bold" : "hover:bg-white/60 text-poli-ink");

  return (
    <div className="min-h-screen bg-poli-gray">
      <div className="max-w-6xl mx-auto p-4 md:p-8 space-y-4">
        <BrandHeader subtitle="Panel Director" />

        <div className="grid md:grid-cols-[260px_1fr] gap-4">
          <aside className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4">
            <div className="font-bold text-poli-ink mb-3">Menú</div>
            <nav className="space-y-2">
              <NavLink className={linkCls} to="/director/proyectos">Proyectos</NavLink>
              <NavLink className={linkCls} to="/director/ayudantes">Ayudantes</NavLink>
              <NavLink className={linkCls} to="/director/bitacoras">Bitácoras</NavLink>
            </nav>

            <button onClick={logout} className="mt-4 w-full rounded-xl bg-poli-navy text-white py-2 font-bold">
              Cerrar sesión
            </button>
          </aside>

          <main className="space-y-4">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}
