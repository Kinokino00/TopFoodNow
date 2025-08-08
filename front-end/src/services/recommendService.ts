import api from './api'
import type { RecommendItem, ApiResponse } from '@/types/recommend'

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
    const response = await api.get<ApiResponse<RecommendItem>>(`/recommend/all`, {
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

export async function getRecommendationByUserIdAndStoreId(userId: number, storeId: number): Promise<RecommendItem> {
  try {
    const response = await api.get<RecommendItem>(`/recommend/${userId}/${storeId}`)
    return response.data
  } catch (error) {
    console.error(`Error fetching recommendation for userId ${userId} and storeId ${storeId}:`, error)
    throw error
  }
}

export async function getRecommendationsByUserId(userId: number): Promise<RecommendItem[]> {
  try {
    const response = await api.get<RecommendItem[]>(`/recommend/user/${userId}`)
    return response.data
  } catch (error) {
    console.error(`Error fetching recommendations for userId ${userId}:`, error)
    throw error
  }
}

/**
 * 取得指定店家的所有推薦
 * @param storeId 店家 ID
 * @returns Promise<RecommendItem[]>
 */
export async function getRecommendationsByStoreId(storeId: number): Promise<RecommendItem[]> {
  try {
    const response = await api.get<RecommendItem[]>(`/recommend/store/${storeId}`)
    return response.data
  } catch (error) {
    console.error(`Error fetching recommendations for storeId ${storeId}:`, error)
    throw error
  }
}
