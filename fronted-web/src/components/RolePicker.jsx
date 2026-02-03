// import React from "react";

// const roles = [
//   { key: "JEFATURA", label: "Jefatura", desc: "Dashboard, Proyectos, Semáforo, Estadísticas" },
//   { key: "DIRECTOR", label: "Director", desc: "Proyectos, Ayudantes, Bitácoras" },
//   { key: "AYUDANTE", label: "Ayudante", desc: "Bitácora mensual, semanas, actividades, enviar" }
// ];

// export default function RolePicker({ value, onChange }) {
//   return (
//     <div className="grid md:grid-cols-3 gap-3">
//       {roles.map((r) => {
//         const active = value === r.key;
//         return (
//           <button
//             key={r.key}
//             type="button"
//             onClick={() => onChange(r.key)}
//             className={[
//               "text-left rounded-2xl border p-4 transition shadow-sm",
//               active
//                 ? "border-poli-red bg-poli-red/5"
//                 : "border-gray-200 bg-white hover:bg-poli-gray"
//             ].join(" ")}
//           >
//             <div className="flex items-center justify-between">
//               <div className="font-bold text-poli-ink">{r.label}</div>
//               <div
//                 className={[
//                   "h-2 w-2 rounded-full",
//                   active ? "bg-poli-red" : "bg-gray-300"
//                 ].join(" ")}
//               />
//             </div>
//             <div className="mt-1 text-sm text-gray-600">{r.desc}</div>
//           </button>
//         );
//       })}
//     </div>
//   );
// }
import React from "react";

const roles = [
  { 
    key: "JEFATURA", 
    label: "Jefatura",
    icon: "📊",
    color: "from-blue-600 to-blue-700"
  },
  { 
    key: "DIRECTOR", 
    label: "Director",
    icon: "👔",
    color: "from-emerald-600 to-emerald-700"
  },
  { 
    key: "AYUDANTE", 
    label: "Ayudante",
    icon: "🎓",
    color: "from-purple-600 to-purple-700"
  }
];

export default function RolePicker({ value, onChange }) {
  return (
    <div className="grid md:grid-cols-3 gap-4">
      {roles.map((r) => {
        const active = value === r.key;
        return (
          <button
            key={r.key}
            type="button"
            onClick={() => onChange(r.key)}
            className={[
              "relative text-center rounded-2xl border-2 p-6 transition-all duration-300 shadow-md hover:shadow-xl transform hover:-translate-y-1",
              active
                ? `border-poli-red bg-gradient-to-br ${r.color} text-white scale-105`
                : "border-gray-200 bg-white hover:border-poli-navy/30"
            ].join(" ")}
          >
            <div className="flex flex-col items-center gap-3">
              <div className={`text-5xl ${active ? 'animate-bounce' : ''}`}>
                {r.icon}
              </div>
              <div className={`font-bold text-xl ${active ? 'text-white' : 'text-poli-ink'}`}>
                {r.label}
              </div>
              
              {/* Indicador de selección */}
              {active && (
                <div className="absolute top-3 right-3">
                  <div className="h-4 w-4 rounded-full bg-white shadow-lg flex items-center justify-center">
                    <div className="h-2 w-2 rounded-full bg-poli-red"></div>
                  </div>
                </div>
              )}
            </div>
          </button>
        );
      })}
    </div>
  );
}