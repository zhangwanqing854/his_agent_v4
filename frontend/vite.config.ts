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
    strictPort: true,
    host: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.headers['content-type']?.includes('multipart/form-data')) {
              proxyReq.setHeader('Content-Type', req.headers['content-type'] as string)
            }
          })
        }
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
