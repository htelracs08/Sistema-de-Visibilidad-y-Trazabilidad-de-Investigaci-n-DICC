export const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export const setUser = (user) => {
  localStorage.setItem('user', JSON.stringify(user));
};

export const getAuth = () => {
  const auth = localStorage.getItem('auth');
  return auth ? JSON.parse(auth) : null;
};

export const setAuth = (correo, password) => {
  localStorage.setItem('auth', JSON.stringify({ correo, password }));
};

export const logout = () => {
  localStorage.removeItem('user');
  localStorage.removeItem('auth');
  window.location.href = '/';
};

export const isAuthenticated = () => {
  return getUser() !== null && getAuth() !== null;
};

export const hasRole = (role) => {
  const user = getUser();
  return user && user.rol === role;
};