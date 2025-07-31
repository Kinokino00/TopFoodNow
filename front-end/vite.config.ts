import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
        // 如果你的後端路徑本身就帶 /api，且你希望前端也帶 /api
        // rewrite: (path) => path.replace(/^\/api/, '/api')
        // 如果你希望前端發送 /api/xxxx，但後端接收 /xxxx (移除 /api)
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})
