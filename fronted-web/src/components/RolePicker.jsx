import React from "react";

const roles = [
  { key: "JEFATURA", label: "Jefatura", desc: "Dashboard, Proyectos, Semáforo, Estadísticas" },
  { key: "DIRECTOR", label: "Director", desc: "Proyectos, Ayudantes, Bitácoras" },
  { key: "AYUDANTE", label: "Ayudante", desc: "Bitácora mensual, semanas, actividades, enviar" }
];

export default function RolePicker({ value, onChange }) {
  return (
    <div className="grid md:grid-cols-3 gap-3">
      {roles.map((r) => {
        const active = value === r.key;
        return (
          <button
            key={r.key}
            type="button"
            onClick={() => onChange(r.key)}
            className={[
              "text-left rounded-2xl border p-4 transition shadow-sm",
              active
                ? "border-poli-red bg-poli-red/5"
                : "border-gray-200 bg-white hover:bg-poli-gray"
            ].join(" ")}
          >
            <div className="flex items-center justify-between">
              <div className="font-bold text-poli-ink">{r.label}</div>
              <div
                className={[
                  "h-2 w-2 rounded-full",
                  active ? "bg-poli-red" : "bg-gray-300"
                ].join(" ")}
              />
            </div>
            <div className="mt-1 text-sm text-gray-600">{r.desc}</div>
          </button>
        );
      })}
    </div>
  );
}
