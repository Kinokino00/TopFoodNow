import axios from 'axios'
import { useUserStore } from '@/stores/user'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
if (!API_BASE_URL) console.warn('VITE_API_BASE_URL is not defined')

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 不需要認證的 API
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