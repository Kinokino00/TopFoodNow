/** @type {import('tailwindcss').Config} */

export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    screens: {
      sm: '640px',
      md: '720px',
      lg: '1024px',
      xl: '1280px',
      '2xl': '1536px'
    },
    fontSize: {
      xs: '0.75rem',
      sm: '0.875rem',
      base: '1rem',
      lg: '1.125rem',
      xl: '1.25rem',
      '2xl': '1.5rem'   /* 24px */,
      '3xl': '1.75rem'  /* 28px */,
      '4xl': '1.875rem' /* 30px */,
      '5xl': '2rem'     /* 32px */,
      '6xl': '2.25rem'  /* 36px */,
      '7xl': '2.5rem'   /* 40px */
    },
    colors: {
      primary: {
        100: '#FBEBDB',
        200: '#F7D7B7',
        300: '#F3C294',
        400: '#EFAE70',
        500: '#EB9A4C',
        600: '#EB8C31',
        700: '#EF8016',
        800: '#EE7603',
        900: '#DE6D00',
      },
      secondary: {
        100: '#DBE9FB',
        200: '#B7D3F7',
        300: '#94BDF3',
        400: '#82B2F1',
        500: '#4C91EB',
        600: '#368AF8',
        700: '#227CF2',
        800: '#0F6AE2',
        900: '#005ACF',
      },
      danger: {
        100: '#FBD9E1',
        200: '#F7B3C4',
        300: '#F48CA6',
        400: '#F06689',
        500: '#EC406B',
        600: '#BD3356',
        700: '#8E2640',
      },
      gray: {
        100: '#E6E6E6',
        200: '#cccccc',
        300: '#B3B3B3',
        400: '#999999',
        500: '#7F7F7F',
        600: '#666666',
        700: '#4D4D4D',
        800: '#333333',
        900: '#1A1A1A',
      },
      white: '#FFFFFF',
      black: '#000000',
      transparent: 'transparent'
    },
    extend: {
      fontFamily: {
        notoSansTC: ['"Noto Sans TC"', 'sans-serif'],
        fontAwesome: ['"Font Awesome 6 Free"', 'sans-serif'],
        // 在@apply是用font-fontAwesome
      },
      boxShadow: {
        primary: '0 0 4px 2px rgba(235, 155, 75, .25)',
        danger: '0 0 4px 2px rgba(219, 58, 51, .25)',
        secondary: '0 0 4px 2px rgba(66, 133, 247, .25)',
        gray: '0 0 4px 2px rgba(128, 128, 128, .25)'
      },
      contrast: {
        '60': '0.6',
      },
    }
  }
};
