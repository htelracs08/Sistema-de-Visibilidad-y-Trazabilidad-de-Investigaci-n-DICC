import React from "react";

export default function Modal({ open, title, children, onClose }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="w-full max-w-3xl rounded-2xl bg-white shadow-xl border border-gray-200">
        <div className="p-4 border-b flex items-center justify-between">
          <div className="font-bold text-poli-ink">{title}</div>
          <button onClick={onClose} className="px-3 py-1 rounded-lg bg-poli-gray hover:bg-gray-200">
            Cerrar
          </button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
}
