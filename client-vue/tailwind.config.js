/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        lime: {
          500: '#84cc16',
          600: '#65a30d',
          700: '#4d7c0f',
        },
        indigo: {
          500: '#6366f1',
          600: '#4f46e5',
        },
      },
      transitionProperty: {
        'transform-shadow': 'transform, box-shadow',
      },
      transitionDuration: {
        '400': '400ms',
      },
      animation: {
        'hover-scale': 'hoverScale 0.3s ease forwards',
        'modal-fade': 'modalFade 0.3s ease',
        'wiggle': 'wiggle 0.5s ease-in-out',
        'bounce': 'bounce 1s infinite',
        'logo-pulse': 'pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'logo-bounce': 'bounce 1s infinite',
      },
      keyframes: {
        hoverScale: {
          '0%': { transform: 'translateY(0)', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' },
          '100%': { transform: 'translateY(-3px)', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)' },
        },
        modalFade: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        wiggle: {
          '0%, 100%': { transform: 'rotate(0deg) scale(1.1)' },
          '25%': { transform: 'rotate(-5deg) scale(1.2)' },
          '50%': { transform: 'rotate(5deg) scale(1.2)' },
          '75%': { transform: 'rotate(-5deg) scale(1.2)' },
        },
        pulse: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.5' },
        },
        bounce: {
          '0%, 100%': { 
            transform: 'translateY(0)' 
          },
          '50%': { 
            transform: 'translateY(-5px)' 
          },
        }
      },
      boxShadow: {
        'button': '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
        'button-hover': '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
      },
      scrollBehavior: {
        smooth: 'smooth',
      },
    },
  },
  plugins: [],
};