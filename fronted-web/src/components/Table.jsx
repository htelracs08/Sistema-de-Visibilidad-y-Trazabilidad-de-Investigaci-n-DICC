import React from "react";

export default function Table({ columns, rows }) {
  return (
    <div className="w-full overflow-auto rounded-2xl border border-gray-200 bg-white">
      <table className="min-w-full text-sm">
        <thead className="bg-poli-gray">
          <tr>
            {columns.map((c) => (
              <th key={c.key} className="text-left px-4 py-3 font-bold text-poli-ink whitespace-nowrap">
                {c.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td className="px-4 py-4 text-gray-500" colSpan={columns.length}>
                Sin datos
              </td>
            </tr>
          ) : (
            rows.map((r, idx) => (
              <tr key={idx} className="border-t">
                {columns.map((c) => (
                  <td key={c.key} className="px-4 py-3 whitespace-nowrap">
                    {c.render ? c.render(r) : r[c.key]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
