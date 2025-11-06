/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // Crumble Cookie inspired colors
        cookie: {
          50: '#FFF5F7',
          100: '#FFE8ED',
          200: '#FFD1DB',
          300: '#FFB6C1',
          400: '#FF8FA3',
          500: '#FF6B9D',
          600: '#FF4A7C',
          700: '#E6395C',
          800: '#CC2E4D',
          900: '#B3243E',
        },
        brown: {
          50: '#FDF8F6',
          100: '#F2E8E5',
          200: '#EADDD8',
          300: '#E0CEC7',
          400: '#D2B48C',
          500: '#C19A6B',
          600: '#A67C52',
          700: '#8B4513',
          800: '#6B3410',
          900: '#4A2C0B',
        },
        beige: {
          50: '#FEFCFB',
          100: '#FDF8F6',
          200: '#FAF0E6',
          300: '#F5E6D3',
          400: '#EED9C4',
          500: '#E6D5B8',
          600: '#D4C4A8',
          700: '#B8A082',
        },
        // Keep lime for backward compatibility but add cookie as primary
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
        'float': 'float 8s ease-in-out infinite',
        'float-slow': 'floatSlow 15s ease-in-out infinite',
        'float-heart': 'floatHeart 6s ease-in-out infinite',
        'fade-in': 'fadeIn 0.8s ease-out',
        'fade-in-delay': 'fadeIn 0.8s ease-out 0.3s both',
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
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px) translateX(0px) rotate(0deg)' },
          '25%': { transform: 'translateY(-20px) translateX(10px) rotate(5deg)' },
          '50%': { transform: 'translateY(-10px) translateX(-10px) rotate(-5deg)' },
          '75%': { transform: 'translateY(-30px) translateX(5px) rotate(3deg)' },
        },
        floatSlow: {
          '0%, 100%': { transform: 'translateY(0px) translateX(0px)' },
          '50%': { transform: 'translateY(-30px) translateX(20px)' },
        },
        floatHeart: {
          '0%, 100%': { transform: 'translateY(0px) scale(1)', opacity: '0.2' },
          '50%': { transform: 'translateY(-25px) scale(1.2)', opacity: '0.4' },
        },
        fadeIn: {
          'from': { opacity: '0', transform: 'translateY(20px)' },
          'to': { opacity: '1', transform: 'translateY(0)' },
        },
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