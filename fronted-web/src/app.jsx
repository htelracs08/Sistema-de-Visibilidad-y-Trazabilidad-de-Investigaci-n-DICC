import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

import JefaturaLayout from "./pages/Jefatura/JefaturaLayout.jsx";
import JefDashboard from "./pages/Jefatura/Dashboard.jsx";
import JefProyectos from "./pages/Jefatura/Proyectos.jsx";
import JefSemaforo from "./pages/Jefatura/Semaforo.jsx";
import JefEstadisticas from "./pages/Jefatura/Estadisticas.jsx";

import DirectorLayout from "./pages/Director/DirectorLayout.jsx";
import DirProyectos from "./pages/Director/Proyectos.jsx";
import DirAyudantes from "./pages/Director/Ayudantes.jsx";
import DirBitacoras from "./pages/Director/Bitacoras.jsx";
import DirHistorial from "./pages/Director/Historial.jsx";

import AyudanteLayout from "./pages/Ayudante/AyudanteLayout.jsx";
import AyuBitacoraActual from "./pages/Ayudante/BitacoraActual.jsx";
import AyuBitacoraVer from "./pages/Ayudante/BitacoraVer.jsx";
import AyuHistorialBitacoras from "./pages/Ayudante/Historial.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        path="/jefatura"
        element={
          <ProtectedRoute role="JEFATURA">
            <JefaturaLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/jefatura/dashboard" replace />} />
        <Route path="dashboard" element={<JefDashboard />} />
        <Route path="proyectos" element={<JefProyectos />} />
        <Route path="semaforo" element={<JefSemaforo />} />
        <Route path="estadisticas" element={<JefEstadisticas />} />
      </Route>

      <Route
        path="/director"
        element={
          <ProtectedRoute role="DIRECTOR">
            <DirectorLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/director/proyectos" replace />} />
        <Route path="proyectos" element={<DirProyectos />} />
        <Route path="ayudantes" element={<DirAyudantes />} />
        <Route path="bitacoras" element={<DirBitacoras />} />
        <Route path="historial" element={<DirHistorial />} />
      </Route>

      <Route
        path="/ayudante"
        element={
          <ProtectedRoute role="AYUDANTE">
            <AyudanteLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/ayudante/bitacora-actual" replace />} />
        <Route path="bitacora-actual" element={<AyuBitacoraActual />} />
        <Route path="bitacora/:id" element={<AyuBitacoraVer />} />
        <Route path="historial" element={<AyuHistorialBitacoras />} />

      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
