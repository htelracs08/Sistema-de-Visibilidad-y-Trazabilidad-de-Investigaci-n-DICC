export function toUpperSafe(v) {
  return (v ?? "").toString().trim().toUpperCase();
}

export function isNil(v) {
  return v === null || v === undefined;
}

export function fmtDateInputToIso(dateStr) {
  // ya viene yyyy-mm-dd desde input
  const v = (dateStr ?? "").trim();
  return v.length ? v : null;
}
