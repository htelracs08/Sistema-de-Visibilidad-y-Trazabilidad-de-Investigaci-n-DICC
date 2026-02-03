// import { useState } from 'react'
// import reactLogo from './assets/react.svg'
// import viteLogo from '/vite.svg'
// import './App.css'

// function App() {
//   const [count, setCount] = useState(0)

//   return (
//     <>
//       <div>
//         <a href="https://vite.dev" target="_blank">
//           <img src={viteLogo} className="logo" alt="Vite logo" />
//         </a>
//         <a href="https://react.dev" target="_blank">
//           <img src={reactLogo} className="logo react" alt="React logo" />
//         </a>
//       </div>
//       <h1>Vite + React</h1>
//       <div className="card">
//         <button onClick={() => setCount((count) => count + 1)}>
//           count is {count}
//         </button>
//         <p>
//           Edit <code>src/App.jsx</code> and save to test HMR
//         </p>
//       </div>
//       <p className="read-the-docs">
//         Click on the Vite and React logos to learn more
//       </p>
//     </>
//   )
// }

// export default App
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import JefaturaDashboard from './pages/jefatura/Dashboard';
import JefaturaProyectos from './pages/jefatura/Proyectos';
import JefaturaSemaforo from './pages/jefatura/Semaforo';
import JefaturaEstadisticas from './pages/jefatura/Estadisticas';
import DirectorDashboard from './pages/director/Dashboard';
import DirectorProyectos from './pages/director/MisProyectos';
import DirectorAyudantes from './pages/director/Ayudantes';
import DirectorBitacoras from './pages/director/Bitacoras';
import AyudanteDashboard from './pages/ayudante/Dashboard';
import AyudanteBitacora from './pages/ayudante/MiBitacora';
import AyudanteHistorial from './pages/ayudante/Historial';
import Layout from './components/layout/Layout';
import PrivateRoute from './components/auth/PrivateRoute';
import { isAuthenticated, getUser } from './utils/auth';

function App() {
  console.log("APP RENDER OK");

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        
        {/* Rutas de Jefatura */}
        <Route path="/jefatura" element={<PrivateRoute role="JEFATURA"><Layout /></PrivateRoute>}>
          <Route path="dashboard" element={<JefaturaDashboard />} />
          <Route path="proyectos" element={<JefaturaProyectos />} />
          <Route path="semaforo" element={<JefaturaSemaforo />} />
          <Route path="estadisticas" element={<JefaturaEstadisticas />} />
        </Route>
        
        {/* Rutas de Director */}
        <Route path="/director" element={<PrivateRoute role="DIRECTOR"><Layout /></PrivateRoute>}>
          <Route path="dashboard" element={<DirectorDashboard />} />
          <Route path="proyectos" element={<DirectorProyectos />} />
          <Route path="ayudantes/:proyectoId" element={<DirectorAyudantes />} />
          <Route path="bitacoras/:proyectoId" element={<DirectorBitacoras />} />
        </Route>
        
        {/* Rutas de Ayudante */}
        <Route path="/ayudante" element={<PrivateRoute role="AYUDANTE"><Layout /></PrivateRoute>}>
          <Route path="dashboard" element={<AyudanteDashboard />} />
          <Route path="bitacora" element={<AyudanteBitacora />} />
          <Route path="historial" element={<AyudanteHistorial />} />
        </Route>

        {/* Ruta por defecto */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;