/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        poli: {
          navy: "#0B2A4A",
          red: "#E63946",
          gold: "#C9A961",
          gray: "#F5F5F5",
          ink: "#111827"
        }
      }
    }
  },
  plugins: []
};




// /** @type {import('tailwindcss').Config} */
// export default {
//   content: [
//     "./index.html",
//     "./src/**/*.{js,ts,jsx,tsx}",
//   ],
//   theme: {
//     extend: {
//       colors: {
//         epn: {
//           blue: "#003366",
//           red: "#E63946",
//           gold: "#C9A961",
//           gray: "#F5F5F5",
//         },
//       },
//     },
//   },
//   plugins: [],
// };
