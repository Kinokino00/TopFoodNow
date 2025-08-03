import axios from 'axios'
import type { Store } from '@/types/store'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

if (!API_BASE_URL) {
    console.warn('VITE_API_BASE_URL is not defined. Please check your .env files.');
}

export async function getStoresRandomly(limit: number = 3): Promise<Store[]> {
    try {
        const response = await axios.get<Store[]>(`${API_BASE_URL}/api/store/random`, {
            params: {
                limit: limit
            }
        })
        return response.data
    } catch (error) {
        console.error('Error fetching stores:', error)
        throw error
    }
}