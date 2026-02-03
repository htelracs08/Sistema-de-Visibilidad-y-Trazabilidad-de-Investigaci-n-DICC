const KEY = "dicc_director_selected_project_v1";

export function setDirectorSelectedProject(project) {
  // project: { id, codigo, nombre }
  localStorage.setItem(KEY, JSON.stringify(project));
}

export function getDirectorSelectedProject() {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function clearDirectorSelectedProject() {
  localStorage.removeItem(KEY);
}
