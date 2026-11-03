import react from '@vitejs/plugin-react'
import path from 'node:path'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, 'src'),
    },
  },
  server: {
    // The API runs on :8080 locally; the axios baseURL is "/api". API_PROXY overrides it when 8080 is taken.
    proxy: {
      '/api': process.env.API_PROXY ?? 'http://localhost:8080',
    },
  },
})
