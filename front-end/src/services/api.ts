import axios from 'axios'
import { useUserStore } from '@/stores/user'

declare global {
  interface Window {
    runtimeConfig?: {
      VITE_API_BASE_URL?: string
    }
  }
}

function getBaseUrl(): string {
  if (window.runtimeConfig?.VITE_API_BASE_URL) {
    return window.runtimeConfig.VITE_API_BASE_URL
  }
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  console.warn('VITE_API_BASE_URL is not defined')
  return ''
}

const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json'
  }
})

const publicApiEndpoints = ['/auth/login', '/auth/logout', '/auth/public/', '/categories', '/store/', '/recommend/']

api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token

    const isPublic = publicApiEndpoints.some(endpoint => config.url?.startsWith(endpoint))
    if (token && !isPublic) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

export default api
