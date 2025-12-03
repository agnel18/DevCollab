/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        trello: {
          blue: '#0079bf',
          darkBlue: '#026aa7',
          lightBlue: '#61bdff',
          green: '#61bd4f',
          yellow: '#f2d600',
          orange: '#ff9f1a',
          red: '#eb5a46',
          purple: '#c377e0',
          pink: '#ff78cb',
          darkGray: '#172b4d',
          mediumGray: '#5e6c84',
          lightGray: '#091e4221',
        }
      },
      backgroundImage: {
        'sunset': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }
    },
  },
  plugins: [],
}
