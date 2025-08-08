import api from './api'
import type { Categories } from '@/types/categories'

export async function getCategories(): Promise<Categories[]> {
    try {
        const response = await api.get<Categories[]>('/categories')
        return response.data
    } catch (error) {
        console.error('Error fetching categories:', error)
        throw error
    }
}
