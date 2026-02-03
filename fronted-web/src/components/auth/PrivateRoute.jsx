import { Navigate } from 'react-router-dom';
import { isAuthenticated, getUser } from '../../utils/auth';

export default function PrivateRoute({ children, role }) {
  if (!isAuthenticated()) {
    return <Navigate to="/" replace />;
  }

  const user = getUser();
  
  if (role && user.rol !== role) {
    // Redirigir al dashboard correspondiente del usuario
    const redirectMap = {
      'JEFATURA': '/jefatura/dashboard',
      'DIRECTOR': '/director/dashboard',
      'AYUDANTE': '/ayudante/dashboard',
    };
    return <Navigate to={redirectMap[user.rol] || '/'} replace />;
  }

  return children;
}