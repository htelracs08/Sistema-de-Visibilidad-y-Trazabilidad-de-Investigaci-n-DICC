import React from "react";

/**
 * Componente para confirmaciones antes de acciones destructivas
 * Uso:
 * <ConfirmDialog
 *   open={showConfirm}
 *   title="¿Confirmar eliminación?"
 *   message="Esta acción no se puede deshacer."
 *   confirmText="Eliminar"
 *   cancelText="Cancelar"
 *   onConfirm={handleDelete}
 *   onCancel={() => setShowConfirm(false)}
 *   type="danger"
 * />
 */
export default function ConfirmDialog({
  open,
  title = "¿Estás seguro?",
  message = "Esta acción no se puede deshacer.",
  confirmText = "Confirmar",
  cancelText = "Cancelar",
  onConfirm,
  onCancel,
  type = "danger" // "danger" | "warning" | "info"
}) {
  if (!open) return null;

  const typeStyles = {
    danger: {
      icon: "⚠️",
      iconBg: "bg-red-100",
      iconColor: "text-red-600",
      confirmBtn: "bg-red-600 hover:bg-red-700",
      border: "border-red-200"
    },
    warning: {
      icon: "⚡",
      iconBg: "bg-amber-100",
      iconColor: "text-amber-600",
      confirmBtn: "bg-amber-600 hover:bg-amber-700",
      border: "border-amber-200"
    },
    info: {
      icon: "ℹ️",
      iconBg: "bg-blue-100",
      iconColor: "text-blue-600",
      confirmBtn: "bg-blue-600 hover:bg-blue-700",
      border: "border-blue-200"
    }
  };

  const style = typeStyles[type] || typeStyles.danger;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fadeIn">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-2xl border-2 ${style.border} animate-scaleIn">
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center gap-4">
            <div className={`h-12 w-12 rounded-full ${style.iconBg} flex items-center justify-center text-2xl`}>
              {style.icon}
            </div>
            <div>
              <h3 className="text-lg font-bold text-poli-ink">{title}</h3>
            </div>
          </div>
        </div>

        {/* Body */}
        <div className="p-6">
          <p className="text-gray-700">{message}</p>
        </div>

        {/* Footer */}
        <div className="p-6 bg-gray-50 rounded-b-2xl flex gap-3 justify-end">
          <button
            onClick={onCancel}
            className="px-4 py-2 rounded-xl bg-white border-2 border-gray-300 text-gray-700 font-semibold hover:bg-gray-100 transition-all"
          >
            {cancelText}
          </button>
          <button
            onClick={() => {
              onConfirm();
              onCancel(); // Cerrar el dialog automáticamente
            }}
            className={`px-4 py-2 rounded-xl text-white font-semibold transition-all shadow-lg ${style.confirmBtn}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}