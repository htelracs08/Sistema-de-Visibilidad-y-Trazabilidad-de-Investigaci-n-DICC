import React from "react";
import { Navigate } from "react-router-dom";
import { getAuth } from "../lib/auth";

export default function ProtectedRoute({ children, role }) {
  const auth = getAuth();
  if (!auth?.correo || !auth?.password) return <Navigate to="/login" replace />;
  if (role && auth?.roleSelected !== role) return <Navigate to="/login" replace />;
  return children;
}
