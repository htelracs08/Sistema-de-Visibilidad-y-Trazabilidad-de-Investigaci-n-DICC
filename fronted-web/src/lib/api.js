import { getAuth } from "./auth";

const BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function buildHeaders(extra = {}) {
  const auth = getAuth();
  const headers = {
    Accept: "application/json",
    ...extra
  };

  if (auth?.correo && auth?.password) {
    const token = btoa(`${auth.correo}:${auth.password}`);
    headers.Authorization = `Basic ${token}`;
    
    // 🔍 DEBUG: Log de autenticación
    console.log("🔐 Auth header generado para:", auth.correo);
  } else {
    console.warn("⚠️ No hay credenciales de autenticación disponibles");
  }

  return headers;
}

async function parseJsonSafe(res) {
  const text = await res.text();
  
  // 🔍 LOG CRÍTICO: Ver respuesta cruda del backend
  console.log(`📥 Backend response [${res.status}]:`, text.substring(0, 500));
  
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    console.warn("⚠️ Respuesta del backend no es JSON válido:", text);
    return { raw: text };
  }
}

export async function apiGet(path) {
  const url = `${BASE}${path}`;
  
  console.log("📤 GET", url);
  
  try {
    const res = await fetch(url, {
      method: "GET",
      headers: buildHeaders()
    });
    
    const data = await parseJsonSafe(res);
    
    if (!res.ok) {
      const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
      console.error(`❌ GET ${path} falló:`, msg);
      throw new Error(msg);
    }
    
    console.log(`✅ GET ${path} exitoso`);
    return data;
  } catch (e) {
    if (e.message === "Failed to fetch") {
      console.error("❌ No se puede conectar al backend. ¿Está corriendo en", BASE, "?");
      throw new Error("No se puede conectar al backend. Verifica que esté corriendo.");
    }
    throw e;
  }
}

export async function apiPost(path, bodyObj) {
  const url = `${BASE}${path}`;
  
  console.log("📤 POST", url);
  console.log("📤 Payload:", JSON.stringify(bodyObj, null, 2));
  
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: buildHeaders({ "Content-Type": "application/json" }),
      body: JSON.stringify(bodyObj ?? {})
    });
    
    const data = await parseJsonSafe(res);
    
    if (!res.ok) {
      const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
      console.error(`❌ POST ${path} falló:`, msg);
      console.error("   Status:", res.status);
      console.error("   Response:", data);
      throw new Error(msg);
    }
    
    console.log(`✅ POST ${path} exitoso`);
    return data;
  } catch (e) {
    if (e.message === "Failed to fetch") {
      console.error("❌ No se puede conectar al backend. ¿Está corriendo en", BASE, "?");
      throw new Error("No se puede conectar al backend. Verifica que esté corriendo.");
    }
    throw e;
  }
}

export async function apiPut(path, bodyObj) {
  const url = `${BASE}${path}`;
  
  console.log("📤 PUT", url);
  console.log("📤 Payload:", JSON.stringify(bodyObj, null, 2));
  
  try {
    const res = await fetch(url, {
      method: "PUT",
      headers: buildHeaders({ "Content-Type": "application/json" }),
      body: JSON.stringify(bodyObj ?? {})
    });
    
    const data = await parseJsonSafe(res);
    
    if (!res.ok) {
      const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
      console.error(`❌ PUT ${path} falló:`, msg);
      console.error("   Status:", res.status);
      console.error("   Response:", data);
      throw new Error(msg);
    }
    
    console.log(`✅ PUT ${path} exitoso`);
    return data;
  } catch (e) {
    if (e.message === "Failed to fetch") {
      console.error("❌ No se puede conectar al backend. ¿Está corriendo en", BASE, "?");
      throw new Error("No se puede conectar al backend. Verifica que esté corriendo.");
    }
    throw e;
  }
}