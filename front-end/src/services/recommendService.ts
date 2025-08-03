import axios from 'axios'
import type { RecommendItem, ApiResponse } from '@/types/recommend'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

interface GetAllRecommendParams {
    page?: number
    size?: number
    sortBy?: string
    sortOrder?: string
    searchTerm?: string
}

export async function getAllRecommendations(
    params: GetAllRecommendParams = {}
): Promise<ApiResponse<RecommendItem>> {
    try {
        const response = await axios.get<ApiResponse<RecommendItem>>(`${API_BASE_URL}/api/recommend/all`, {
        params: {
            page: params.page || 1,
            size: params.size || 10,
            sortBy: params.sortBy || 'id',
            sortOrder: params.sortOrder || 'asc',
            ...(params.searchTerm && { searchTerm: params.searchTerm }),
        }
        })
        return response.data
    } catch (error) {
        console.error('Error fetching recommendations:', error)
        throw error
    }
}