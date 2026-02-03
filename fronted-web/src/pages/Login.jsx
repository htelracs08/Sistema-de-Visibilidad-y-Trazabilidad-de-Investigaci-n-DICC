// import React, { useState } from "react";
// import { useNavigate } from "react-router-dom";
// import BrandHeader from "../components/BrandHeader.jsx";
// import RolePicker from "../components/RolePicker.jsx";
// import Toast from "../components/Toast.jsx";
// import Loading from "../components/Loading.jsx";
// import { apiGet } from "../lib/api";
// import { setAuth, clearAuth } from "../lib/auth";
// import { toUpperSafe } from "../lib/utils";

// export default function Login() {
//   const nav = useNavigate();

//   const [roleSelected, setRoleSelected] = useState("JEFATURA");
//   const [correo, setCorreo] = useState("");
//   const [password, setPassword] = useState("");

//   const [loading, setLoading] = useState(false);
//   const [toast, setToast] = useState({ msg: "", kind: "info" });

//   async function doLogin(e) {
//     e.preventDefault();
//     setToast({ msg: "", kind: "info" });

//     const c = correo.trim().toLowerCase();
//     const p = password;

//     if (!roleSelected) return setToast({ msg: "Selecciona un rol", kind: "bad" });
//     if (!c || !p) return setToast({ msg: "Correo y contraseña son requeridos", kind: "bad" });

//     try {
//       setLoading(true);

//       // Guardamos credenciales temporalmente para que api.js mande Basic Auth
//       setAuth({ correo: c, password: p, roleSelected });

//       const me = await apiGet("/api/v1/me");

//       // tu contrato dice: { ok:true, correo:"..", rol:"AYUDANTE" }
//       const ok = me?.ok === true;
//       const rolReal = toUpperSafe(me?.rol);

//       if (!ok) {
//         clearAuth();
//         return setToast({ msg: me?.msg || "Login falló", kind: "bad" });
//       }

//       if (rolReal !== toUpperSafe(roleSelected)) {
//         clearAuth();
//         return setToast({
//           msg: `Acceso denegado. Elegiste ${roleSelected} pero tu rol real es ${rolReal}`,
//           kind: "bad"
//         });
//       }

//       setAuth({ correo: c, password: p, roleSelected: rolReal });

//       if (rolReal === "JEFATURA") nav("/jefatura");
//       else if (rolReal === "DIRECTOR") nav("/director");
//       else nav("/ayudante");
//     } catch (err) {
//       clearAuth();
//       setToast({ msg: err.message || "Error de conexión", kind: "bad" });
//     } finally {
//       setLoading(false);
//     }
//   }

//   return (
//     <div className="min-h-screen bg-poli-gray">
//       <div className="max-w-5xl mx-auto p-4 md:p-8 space-y-4">
//         <BrandHeader subtitle="Un solo portal web para Jefatura, Director y Ayudante" />

//         <div className="grid lg:grid-cols-2 gap-4">
//           <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
//             <div className="text-sm font-semibold text-gray-500">Paso 1</div>
//             <div className="text-lg font-bold text-poli-ink">Elige el rol</div>
//             <div className="mt-3">
//               <RolePicker value={roleSelected} onChange={setRoleSelected} />
//             </div>
//           </div>

//           <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-5">
//             <div className="text-sm font-semibold text-gray-500">Paso 2</div>
//             <div className="text-lg font-bold text-poli-ink">Inicia sesión</div>

//             <form onSubmit={doLogin} className="mt-4 space-y-3">
//               <div>
//                 <label className="text-sm text-gray-600">Correo</label>
//                 <input
//                   className="mt-1 w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
//                   value={correo}
//                   onChange={(e) => setCorreo(e.target.value)}
//                   placeholder="usuario@epn.edu.ec"
//                 />
//               </div>

//               <div>
//                 <label className="text-sm text-gray-600">Password</label>
//                 <input
//                   type="password"
//                   className="mt-1 w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-poli-navy/30"
//                   value={password}
//                   onChange={(e) => setPassword(e.target.value)}
//                   placeholder="••••••••"
//                 />
//               </div>

//               <button
//                 disabled={loading}
//                 className="w-full rounded-xl bg-poli-navy text-white font-bold py-2 hover:opacity-95 disabled:opacity-60"
//               >
//                 {loading ? "Validando..." : `Ingresar como ${roleSelected}`}
//               </button>

//               {loading && <Loading text="Conectando con el backend..." />}
//               <div className="text-xs text-gray-500">
//                 Se valida contra <code>/api/v1/me</code> y se bloquea si el rol no coincide.
//               </div>
//             </form>
//           </div>
//         </div>
//       </div>

//       <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
//     </div>
//   );
// }



import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import BrandHeader from "../components/BrandHeader.jsx";
import RolePicker from "../components/RolePicker.jsx";
import Toast from "../components/Toast.jsx";
import Loading from "../components/Loading.jsx";
import { apiGet } from "../lib/api";
import { setAuth, clearAuth } from "../lib/auth";
import { toUpperSafe } from "../lib/utils";

export default function Login() {
  const nav = useNavigate();

  const [roleSelected, setRoleSelected] = useState("JEFATURA");
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });

  async function doLogin(e) {
    e.preventDefault();
    setToast({ msg: "", kind: "info" });

    const c = correo.trim().toLowerCase();
    const p = password;

    if (!roleSelected) return setToast({ msg: "⚠️ Selecciona un rol", kind: "bad" });
    if (!c || !p) return setToast({ msg: "⚠️ Correo y contraseña son requeridos", kind: "bad" });

    try {
      setLoading(true);

      // Guardamos credenciales temporalmente para que api.js mande Basic Auth
      setAuth({ correo: c, password: p, roleSelected });

      const me = await apiGet("/api/v1/me");

      const ok = me?.ok === true;
      const rolReal = toUpperSafe(me?.rol);

      if (!ok) {
        clearAuth();
        return setToast({ msg: me?.msg || "❌ Login falló", kind: "bad" });
      }

      if (rolReal !== toUpperSafe(roleSelected)) {
        clearAuth();
        return setToast({
          msg: `❌ Acceso denegado. Elegiste ${roleSelected} pero tu rol real es ${rolReal}`,
          kind: "bad"
        });
      }

      setAuth({ correo: c, password: p, roleSelected: rolReal });

      if (rolReal === "JEFATURA") nav("/jefatura");
      else if (rolReal === "DIRECTOR") nav("/director");
      else nav("/ayudante");
    } catch (err) {
      clearAuth();
      setToast({ msg: err.message || "❌ Error de conexión", kind: "bad" });
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-gray-100">
      <div className="max-w-6xl mx-auto p-4 md:p-8 space-y-6">
        <BrandHeader subtitle="Portal unificado para Jefatura, Director y Ayudante" />

        <div className="grid lg:grid-cols-2 gap-6">
          {/* Paso 1: Selección de rol */}
          <div className="rounded-2xl bg-white border border-gray-200 shadow-lg p-6">
            <div className="mb-2">
              <span className="inline-block px-3 py-1 rounded-full bg-blue-100 text-blue-800 text-xs font-bold">
                PASO 1
              </span>
            </div>
            <h2 className="text-2xl font-bold text-poli-ink mb-1">Elige tu rol</h2>
            <p className="text-sm text-gray-500 mb-6">Selecciona el rol con el que deseas ingresar</p>
            
            <RolePicker value={roleSelected} onChange={setRoleSelected} />
          </div>

          {/* Paso 2: Credenciales */}
          <div className="rounded-2xl bg-white border border-gray-200 shadow-lg p-6">
            <div className="mb-2">
              <span className="inline-block px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold">
                PASO 2
              </span>
            </div>
            <h2 className="text-2xl font-bold text-poli-ink mb-1">Inicia sesión</h2>
            <p className="text-sm text-gray-500 mb-6">Ingresa tus credenciales institucionales</p>

            <form onSubmit={doLogin} className="space-y-4">
              <div>
                <label className="text-sm font-semibold text-gray-700 flex items-center gap-2">
                  <span>📧</span>
                  <span>Correo institucional</span>
                </label>
                <input
                  className="mt-2 w-full rounded-xl border-2 border-gray-200 px-4 py-3 outline-none focus:border-poli-navy focus:ring-4 focus:ring-poli-navy/10 transition-all"
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  placeholder="usuario@epn.edu.ec"
                  disabled={loading}
                />
              </div>

              <div>
                <label className="text-sm font-semibold text-gray-700 flex items-center gap-2">
                  <span>🔒</span>
                  <span>Contraseña</span>
                </label>
                <input
                  type="password"
                  className="mt-2 w-full rounded-xl border-2 border-gray-200 px-4 py-3 outline-none focus:border-poli-navy focus:ring-4 focus:ring-poli-navy/10 transition-all"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  disabled={loading}
                />
              </div>

              <button
                disabled={loading}
                className="w-full rounded-xl bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold py-4 hover:shadow-xl transition-all duration-300 disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-3"
              >
                {loading ? (
                  <>
                    <div className="h-5 w-5 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                    <span>Validando...</span>
                  </>
                ) : (
                  <>
                    <span>✨</span>
                    <span>Ingresar como {roleSelected}</span>
                  </>
                )}
              </button>

              {loading && (
                <div className="flex items-center justify-center gap-2 text-sm text-gray-600">
                  <Loading text="Conectando con el servidor..." />
                </div>
              )}

              <div className="mt-4 p-3 bg-blue-50 rounded-xl border border-blue-200">
                <p className="text-xs text-blue-800">
                  <strong>ℹ️ Nota:</strong> El sistema valida contra <code className="bg-blue-200 px-2 py-0.5 rounded">/api/v1/me</code> y bloquea el acceso si el rol no coincide.
                </p>
              </div>
            </form>
          </div>
        </div>

        {/* Footer informativo */}
        <div className="rounded-2xl bg-white border border-gray-200 shadow-sm p-4 text-center">
          <p className="text-sm text-gray-600">
            Sistema DICC - Departamento de Investigación y Control de Calidad | 
            <span className="text-poli-navy font-semibold"> Escuela Politécnica Nacional</span>
          </p>
        </div>
      </div>

      <Toast msg={toast.msg} kind={toast.kind} onClose={() => setToast({ msg: "", kind: "info" })} />
    </div>
  );
}