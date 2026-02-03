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
  }

  return headers;
}

async function parseJsonSafe(res) {
  const text = await res.text();
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return { raw: text };
  }
}

export async function apiGet(path) {
  const res = await fetch(`${BASE}${path}`, {
    method: "GET",
    headers: buildHeaders()
  });
  const data = await parseJsonSafe(res);
  if (!res.ok) {
    const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

export async function apiPost(path, bodyObj) {
  const res = await fetch(`${BASE}${path}`, {
    method: "POST",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(bodyObj ?? {})
  });
  const data = await parseJsonSafe(res);
  if (!res.ok) {
    const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

export async function apiPut(path, bodyObj) {
  const res = await fetch(`${BASE}${path}`, {
    method: "PUT",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(bodyObj ?? {})
  });
  const data = await parseJsonSafe(res);
  if (!res.ok) {
    const msg = data?.msg || data?.message || data?.raw || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}
