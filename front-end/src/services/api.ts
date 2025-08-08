import axios from 'axios'
import { getToken } from '@/utils/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

if (!API_BASE_URL) {
  console.warn('VITE_API_BASE_URL is not defined. Please check your .env files.')
}

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Cache-Control': 'no-cache',
    'Pragma': 'no-cache',
    'Expires': '0',
  },
})

api.interceptors.request.use(
  (config) => {
    const isAuthRequired = !(
      config.url?.includes('/auth/public/') ||
      config.url?.match(/\/recommend\/\d+\/\d+$/) ||
      config.url?.match(/\/recommend\/user\/\d+$/)
    )

    if (isAuthRequired) {
      const token = getToken()
      if (token) config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default api