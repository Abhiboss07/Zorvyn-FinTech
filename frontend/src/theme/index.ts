import { createTheme } from '@mui/material/styles';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#4ade80', // Vibrant green
      light: '#86efac',
      dark: '#166534',
      contrastText: '#1c1c1c',
    },
    secondary: {
      main: '#818cf8', // Indigo
      light: '#a5b4fc',
      dark: '#3730a3',
      contrastText: '#ffffff',
    },
    background: {
      default: '#0f172a', // Slate 900
      paper: '#1e293b',   // Slate 800
    },
    text: {
      primary: '#f8fafc', // Slate 50
      secondary: '#94a3b8', // Slate 400
    },
    error: {
      main: '#f87171',
    },
    warning: {
      main: '#fbbf24',
    },
    info: {
      main: '#38bdf8',
    },
    success: {
      main: '#34d399',
    },
  },
  shape: {
    borderRadius: 12,
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
      fontSize: '2.5rem',
      letterSpacing: '-0.02em',
    },
    h2: {
      fontWeight: 600,
      fontSize: '2rem',
      letterSpacing: '-0.01em',
    },
    h3: {
      fontWeight: 600,
      fontSize: '1.75rem',
    },
    h4: {
      fontWeight: 600,
      fontSize: '1.5rem',
    },
    h5: {
      fontWeight: 500,
      fontSize: '1.25rem',
    },
    h6: {
      fontWeight: 500,
      fontSize: '1rem',
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
          border: '1px solid #334155',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
          },
        },
      },
    },
  },
});

export default darkTheme;
