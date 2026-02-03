import axios from 'axios';

const API_BASE_URL = '/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar autenticación
api.interceptors.request.use((config) => {
  const auth = localStorage.getItem('auth');
  if (auth) {
    const { correo, password } = JSON.parse(auth);
    config.headers.Authorization = `Basic ${btoa(`${correo}:${password}`)}`;
  }
  return config;
});

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth');
      localStorage.removeItem('user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH ====================
export const authService = {
  login: async (correo, password) => {
    const response = await api.get('/me', {
      headers: {
        Authorization: `Basic ${btoa(`${correo}:${password}`)}`,
      },
    });
    return response.data;
  },
  
  cambiarPassword: async (nuevaPassword) => {
    const response = await api.post('/auth/cambiar-password', { nuevaPassword });
    return response.data;
  },
};

// ==================== JEFATURA ====================
export const jefaturaService = {
  getProfesores: async () => {
    const response = await api.get('/jefatura/profesores');
    return response.data;
  },
  
  crearProyecto: async (data) => {
    const response = await api.post('/jefatura/proyectos', data);
    return response.data;
  },
  
  getProyectos: async () => {
    const response = await api.get('/jefatura/proyectos');
    return response.data;
  },
  
  getResumenProyectos: async () => {
    const response = await api.get('/jefatura/proyectos/resumen');
    return response.data;
  },
  
  getAyudantesActivos: async () => {
    const response = await api.get('/jefatura/ayudantes/activos');
    return response.data;
  },
  
  getEstadisticasProyectos: async () => {
    const response = await api.get('/jefatura/proyectos/estadisticas');
    return response.data;
  },
  
  getEstadisticasAyudantes: async () => {
    const response = await api.get('/jefatura/ayudantes/estadisticas');
    return response.data;
  },
  
  getSemaforo: async () => {
    const response = await api.get('/jefatura/semaforo');
    return response.data;
  },
  
  getAyudantesProyecto: async (proyectoId) => {
    const response = await api.get(`/jefatura/proyectos/${proyectoId}/ayudantes`);
    return response.data;
  },
};

// ==================== DIRECTOR ====================
export const directorService = {
  getMisProyectos: async () => {
    const response = await api.get('/director/mis-proyectos');
    return response.data;
  },
  
  actualizarProyecto: async (proyectoId, data) => {
    const response = await api.put(`/director/proyectos/${proyectoId}`, data);
    return response.data;
  },
  
  getAyudantesProyecto: async (proyectoId) => {
    const response = await api.get(`/director/proyectos/${proyectoId}/ayudantes`);
    return response.data;
  },
  
  registrarAyudante: async (proyectoId, data) => {
    const response = await api.post(`/director/proyectos/${proyectoId}/ayudantes`, data);
    return response.data;
  },
  
  finalizarContrato: async (contratoId, motivo) => {
    const response = await api.post(`/director/contratos/${contratoId}/finalizar`, { motivo });
    return response.data;
  },
  
  getBitacorasPendientes: async (proyectoId) => {
    const response = await api.get(`/director/proyectos/${proyectoId}/bitacoras/pendientes`);
    return response.data;
  },
  
  getBitacora: async (bitacoraId) => {
    const response = await api.get(`/director/bitacoras/${bitacoraId}`);
    return response.data;
  },
  
  revisarBitacora: async (bitacoraId, decision, observacion) => {
    const response = await api.post(`/director/bitacoras/${bitacoraId}/revisar`, {
      decision,
      observacion,
    });
    return response.data;
  },
};

// ==================== AYUDANTE ====================
export const ayudanteService = {
  getBitacoraActual: async () => {
    const response = await api.post('/ayudante/bitacoras/actual');
    return response.data;
  },
  
  getBitacora: async (bitacoraId) => {
    const response = await api.get(`/ayudante/bitacoras/${bitacoraId}`);
    return response.data;
  },
  
  getBitacorasAprobadas: async () => {
    const response = await api.get('/ayudante/bitacoras/aprobadas');
    return response.data;
  },
  
  crearSemana: async (bitacoraId, data) => {
    const response = await api.post(`/ayudante/bitacoras/${bitacoraId}/semanas`, data);
    return response.data;
  },
  
  crearActividad: async (semanaId, data) => {
    const response = await api.post(`/ayudante/semanas/${semanaId}/actividades`, data);
    return response.data;
  },
  
  actualizarActividad: async (actividadId, data) => {
    const response = await api.put(`/ayudante/actividades/${actividadId}`, data);
    return response.data;
  },
  
  enviarBitacora: async (bitacoraId) => {
    const response = await api.post(`/ayudante/bitacoras/${bitacoraId}/enviar`);
    return response.data;
  },
  
  reabrirBitacora: async (bitacoraId) => {
    const response = await api.post(`/ayudante/bitacoras/${bitacoraId}/reabrir`);
    return response.data;
  },
};

export default api;