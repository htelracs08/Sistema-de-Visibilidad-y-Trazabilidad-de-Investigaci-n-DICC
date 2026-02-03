import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogIn, AlertCircle } from 'lucide-react';
import { authService } from '../services/api';
import { setAuth, setUser } from '../utils/auth';

export default function Login() {
    const navigate = useNavigate();
    const [correo, setCorreo] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const user = await authService.login(correo, password);

            if (!user.ok) {
                throw new Error(user.msg || 'Error de autenticación');
            }

            // Guardar credenciales y usuario
            setAuth(correo, password);
            setUser(user);

            // Redirigir según rol
            switch (user.rol) {
                case 'JEFATURA':
                    navigate('/jefatura/dashboard');
                    break;
                case 'DIRECTOR':
                    navigate('/director/dashboard');
                    break;
                case 'AYUDANTE':
                    navigate('/ayudante/dashboard');
                    break;
                default:
                    throw new Error('Rol no reconocido');
            }
        } catch (err) {
            console.error('Error de login:', err);
            setError(err.response?.data?.msg || err.message || 'Error al iniciar sesión');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-epn-blue via-blue-900 to-epn-blue flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                {/* Logo y título */}
                <div className="text-center mb-8">
                    <div className="w-24 h-24 bg-white rounded-full mx-auto mb-4 flex items-center justify-center">
                        <span className="text-epn-blue text-4xl font-bold">EPN</span>
                    </div>



                    <h1 className="text-3xl font-bold text-white mb-2">Sistema DICC</h1>
                    <p className="text-blue-200">Escuela Politécnica Nacional</p>
                </div>

                {/* Formulario */}
                <div className="bg-white rounded-2xl shadow-2xl p-8">
                    <div className="mb-6">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-2">Iniciar Sesión</h2>
                        <p className="text-gray-600 text-sm">Ingresa tus credenciales institucionales</p>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
                            <div className="text-sm text-red-800">{error}</div>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Correo Institucional
                            </label>
                            <input
                                type="email"
                                value={correo}
                                onChange={(e) => setCorreo(e.target.value)}
                                placeholder="ejemplo@epn.edu.ec"
                                className="input-field"
                                required
                                disabled={loading}
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Contraseña
                            </label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className="input-field"
                                required
                                disabled={loading}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full btn-primary py-3 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <>
                                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    Validando...
                                </>
                            ) : (
                                <>
                                    <LogIn className="w-5 h-5" />
                                    Ingresar
                                </>
                            )}
                        </button>
                    </form>

                    <div className="mt-6 pt-6 border-t border-gray-200">
                        <p className="text-xs text-gray-500 text-center">
                            ¿Problemas para ingresar? Contacta con el administrador del sistema
                        </p>
                    </div>
                </div>

                <div className="mt-6 text-center text-sm text-blue-100">
                    © 2026 Escuela Politécnica Nacional - Todos los derechos reservados
                </div>
            </div>
        </div>
    );
}