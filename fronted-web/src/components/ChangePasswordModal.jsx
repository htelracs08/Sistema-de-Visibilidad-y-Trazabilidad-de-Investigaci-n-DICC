import React, { useState } from "react";
import Modal from "./Modal.jsx";
import Toast from "./Toast.jsx";
import { apiPost } from "../lib/api";

/**
 * 🔐 COMPONENTE DE CAMBIO DE CONTRASEÑA
 * ✅ CORREGIDO: Usa el payload correcto que espera el backend
 * 
 * Uso:
 * import ChangePasswordModal from "../components/ChangePasswordModal.jsx";
 * 
 * const [showChangePassword, setShowChangePassword] = useState(false);
 * 
 * <ChangePasswordModal 
 *   open={showChangePassword} 
 *   onClose={() => setShowChangePassword(false)} 
 * />
 */
export default function ChangePasswordModal({ open, onClose }) {
  const [form, setForm] = useState({
    passwordActual: "",
    passwordNueva: "",
    passwordConfirm: ""
  });
  
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ msg: "", kind: "info" });
  const [showPasswords, setShowPasswords] = useState({
    actual: false,
    nueva: false,
    confirm: false
  });

  function handleClose() {
    setForm({
      passwordActual: "",
      passwordNueva: "",
      passwordConfirm: ""
    });
    setShowPasswords({
      actual: false,
      nueva: false,
      confirm: false
    });
    setToast({ msg: "", kind: "info" });
    onClose();
  }

  async function handleSubmit() {
    // Validaciones
    if (!form.passwordActual.trim()) {
      setToast({ msg: "⚠️ Ingresa tu contraseña actual", kind: "bad" });
      return;
    }

    if (!form.passwordNueva.trim()) {
      setToast({ msg: "⚠️ Ingresa una nueva contraseña", kind: "bad" });
      return;
    }

    if (form.passwordNueva.length < 6) {
      setToast({ msg: "⚠️ La nueva contraseña debe tener al menos 6 caracteres", kind: "bad" });
      return;
    }

    if (form.passwordNueva !== form.passwordConfirm) {
      setToast({ msg: "⚠️ Las contraseñas nuevas no coinciden", kind: "bad" });
      return;
    }

    if (form.passwordActual === form.passwordNueva) {
      setToast({ msg: "⚠️ La nueva contraseña debe ser diferente a la actual", kind: "bad" });
      return;
    }

    setLoading(true);
    
    try {
      console.log("📤 Intentando cambiar contraseña...");
      
      // ✅ PAYLOAD CORRECTO: Backend espera "nuevaPassword" (no "passwordNueva")
      const payload = {
        nuevaPassword: form.passwordNueva
      };

      // ✅ ENDPOINT CORRECTO según AuthController.java
      const response = await apiPost("/api/v1/auth/cambiar-password", payload);
      
      if (response && response.ok === true) {
        console.log("✅ Contraseña cambiada exitosamente");
        setToast({ msg: "✅ Contraseña actualizada correctamente", kind: "ok" });
        
        // Esperar 2 segundos y cerrar
        setTimeout(() => {
          handleClose();
        }, 2000);
      } else {
        throw new Error(response?.msg || "No se pudo cambiar la contraseña");
      }
      
    } catch (e) {
      console.error("❌ Error cambiando contraseña:", e);
      setToast({ 
        msg: `❌ ${e.message || 'Error al cambiar contraseña. Verifica tu contraseña actual.'}`, 
        kind: "bad" 
      });
    } finally {
      setLoading(false);
    }
  }

  if (!open) return null;

  return (
    <>
      <Modal open={open} title="🔐 Cambiar Contraseña" onClose={handleClose}>
        <div className="space-y-4">
          
          {/* Información */}
          <div className="p-3 bg-blue-50 rounded-xl border border-blue-200">
            <p className="text-sm text-blue-800">
              <strong>ℹ️ Requisitos:</strong> La nueva contraseña debe tener al menos 6 caracteres.
            </p>
          </div>

          {/* Contraseña Actual */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Contraseña Actual <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <input
                type={showPasswords.actual ? "text" : "password"}
                className="w-full rounded-xl border-2 border-gray-200 px-4 py-3 pr-12 outline-none focus:border-poli-navy focus:ring-4 focus:ring-poli-navy/10 transition-all"
                value={form.passwordActual}
                onChange={(e) => setForm({ ...form, passwordActual: e.target.value })}
                placeholder="••••••••"
                disabled={loading}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, actual: !showPasswords.actual })}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                tabIndex={-1}
              >
                {showPasswords.actual ? "👁️" : "👁️‍🗨️"}
              </button>
            </div>
          </div>

          {/* Nueva Contraseña */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Nueva Contraseña <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <input
                type={showPasswords.nueva ? "text" : "password"}
                className="w-full rounded-xl border-2 border-gray-200 px-4 py-3 pr-12 outline-none focus:border-poli-navy focus:ring-4 focus:ring-poli-navy/10 transition-all"
                value={form.passwordNueva}
                onChange={(e) => setForm({ ...form, passwordNueva: e.target.value })}
                placeholder="••••••••"
                disabled={loading}
                autoComplete="new-password"
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, nueva: !showPasswords.nueva })}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                tabIndex={-1}
              >
                {showPasswords.nueva ? "👁️" : "👁️‍🗨️"}
              </button>
            </div>
            {form.passwordNueva && form.passwordNueva.length < 6 && (
              <p className="text-xs text-red-600 mt-1">
                ⚠️ Mínimo 6 caracteres
              </p>
            )}
          </div>

          {/* Confirmar Nueva Contraseña */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Confirmar Nueva Contraseña <span className="text-red-600">*</span>
            </label>
            <div className="relative">
              <input
                type={showPasswords.confirm ? "text" : "password"}
                className="w-full rounded-xl border-2 border-gray-200 px-4 py-3 pr-12 outline-none focus:border-poli-navy focus:ring-4 focus:ring-poli-navy/10 transition-all"
                value={form.passwordConfirm}
                onChange={(e) => setForm({ ...form, passwordConfirm: e.target.value })}
                placeholder="••••••••"
                disabled={loading}
                autoComplete="new-password"
              />
              <button
                type="button"
                onClick={() => setShowPasswords({ ...showPasswords, confirm: !showPasswords.confirm })}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                tabIndex={-1}
              >
                {showPasswords.confirm ? "👁️" : "👁️‍🗨️"}
              </button>
            </div>
            {form.passwordConfirm && form.passwordNueva !== form.passwordConfirm && (
              <p className="text-xs text-red-600 mt-1">
                ⚠️ Las contraseñas no coinciden
              </p>
            )}
          </div>

          {/* Botones */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button
              onClick={handleClose}
              disabled={loading}
              className="rounded-xl px-5 py-2.5 bg-gray-200 hover:bg-gray-300 font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Cancelar
            </button>
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="rounded-xl px-5 py-2.5 bg-gradient-to-r from-poli-navy to-blue-900 text-white font-bold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {loading ? (
                <>
                  <div className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                  <span>Procesando...</span>
                </>
              ) : (
                <>
                  <span>🔐</span>
                  <span>Cambiar Contraseña</span>
                </>
              )}
            </button>
          </div>
        </div>
      </Modal>

      <Toast 
        msg={toast.msg} 
        kind={toast.kind} 
        onClose={() => setToast({ msg: "", kind: "info" })} 
      />
    </>
  );
}