import React from "react";
import logo from "../assets/logo-poli.png";

export default function BrandHeader({ subtitle }) {
  return (
    <div className="w-full rounded-2xl bg-white shadow-sm border border-gray-200 p-5 flex items-center justify-between">
      <div className="flex items-center gap-4">
        <div className="h-12 w-12 rounded-xl bg-white border border-gray-200 overflow-hidden flex items-center justify-center">
          <img src={logo} alt="POLI" className="h-10 w-10 object-contain" />
        </div>

        <div>
          <div className="text-lg font-bold text-poli-ink">Sistema DICC</div>
          <div className="text-sm text-gray-500">{subtitle}</div>
        </div>
      </div>

      <div className="hidden sm:flex items-center gap-2">
        <span className="px-3 py-1 rounded-full bg-poli-gray text-poli-ink text-xs font-semibold border border-gray-200">
          POLI
        </span>
        <span className="px-3 py-1 rounded-full bg-poli-red/10 text-poli-red text-xs font-semibold border border-poli-red/30">
          Web
        </span>
      </div>
    </div>
  );
}
