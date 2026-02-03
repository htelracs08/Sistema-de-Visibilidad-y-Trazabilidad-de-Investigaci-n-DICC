import React from "react";

export default function Badge({ kind, children }) {
  const cls =
    kind === "ok"
      ? "bg-emerald-600"
      : kind === "warn"
      ? "bg-amber-500 text-black"
      : kind === "bad"
      ? "bg-red-600"
      : "bg-gray-500";
  return (
    <span className={`px-3 py-1 rounded-full text-xs font-bold text-white ${cls}`}>
      {children}
    </span>
  );
}
