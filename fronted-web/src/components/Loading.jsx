import React from "react";

export default function Loading({ text = "Cargando..." }) {
  return (
    <div className="flex items-center gap-3 text-gray-600">
      <div className="h-4 w-4 rounded-full border-2 border-gray-300 border-t-poli-navy animate-spin" />
      <span className="text-sm">{text}</span>
    </div>
  );
}
