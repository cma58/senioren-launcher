/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Kleuren geïnspireerd op Gent/Vlaanderen (leeuw) en warme Marokkaanse tinten
        gent: {
          50: '#eef6ff',
          100: '#d9ebff',
          200: '#bcdcff',
          300: '#8ec6ff',
          400: '#59a5ff',
          500: '#3282ff',
          600: '#1c62f5',
          700: '#154ce1',
          800: '#183fb6',
          900: '#1a398f',
        },
        saffraan: {
          50: '#fff8eb',
          100: '#feefc7',
          200: '#fddc8a',
          300: '#fcc44d',
          400: '#fbae24',
          500: '#f58e0b',
          600: '#d96c06',
          700: '#b44d09',
          800: '#923c0e',
          900: '#78320f',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'Segoe UI', 'Roboto', 'Arial', 'sans-serif'],
        arabic: ['"Noto Naskh Arabic"', '"Segoe UI"', 'serif'],
      },
    },
  },
  plugins: [],
}
