import React from "react";

export default function Toast({ msg, kind = "info", onClose }) {
  if (!msg) return null;
  const cls =
    kind === "ok"
      ? "bg-emerald-600"
      : kind === "bad"
      ? "bg-red-600"
      : "bg-poli-navy";
  return (
    <div className="fixed bottom-4 right-4 z-50">
      <div className={`rounded-2xl px-4 py-3 text-white shadow-lg ${cls} flex items-center gap-3`}>
        <div className="text-sm">{msg}</div>
        <button className="px-2 py-1 bg-white/15 rounded-lg" onClick={onClose}>
          X
        </button>
      </div>
    </div>
  );
}
