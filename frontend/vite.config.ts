import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      },
      '/api/soap-proxy': {
        target: 'http://10.208.104.58:8089',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            const soapUrl = req.headers['x-soap-url'] as string
            if (soapUrl) {
              const url = new URL(soapUrl)
              proxyReq.setHeader('host', url.host)
              proxyReq.path = url.pathname + url.search
            }
          })
        }
      }
    }
  }
})
