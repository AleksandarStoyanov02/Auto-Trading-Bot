import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],

  server: {
    // Proxy setup to redirect all API calls to the Spring Boot backend
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // The address where Spring Boot is running
        changeOrigin: true, // Needed for hostname replacement
        secure: false,      // Use false for local http connections
      }
    }
  }
})