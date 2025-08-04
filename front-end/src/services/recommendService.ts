import axios from 'axios'
import type { RecommendItem, ApiResponse } from '@/types/recommend'
import { getToken } from '@/utils/auth' // 假設你有一個獲取 token 的函數

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// 創建一個 Axios 實例，方便集中管理配置和攔截器
const api = axios.create({
  baseURL: API_BASE_URL,
});

// 添加請求攔截器，自動附加認證 Token
// 根據您提供的 API 文件，/api/recommend/{userId}/{storeId} 是 "無認證" 的
// 所以這個攔截器只會影響需要認證的 API (例如 /api/recommend/all)
api.interceptors.request.use(
  (config) => {
    // 檢查請求的 URL 是否需要 Token。
    // 如果是 /api/recommend/{userId}/{storeId}，則不添加 Token。
    const isAuthRequired = !config.url?.match(/\/api\/recommend\/\d+\/\d+$/);

    if (isAuthRequired) {
      const token = getToken(); // 獲取 stored token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);


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
        const response = await api.get<ApiResponse<RecommendItem>>(`/api/recommend/all`, { // 使用 api 實例
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

// 新增：根據用戶 ID 和店家 ID 獲取單一推薦的函數
// API 規格： GET /api/recommend/{userId}/{storeId} 返回一個 RecommendItem 物件 (無認證)
export async function getRecommendationByUserIdAndStoreId(userId: number, storeId: number): Promise<RecommendItem> {
  try {
    const response = await api.get<RecommendItem>(`/api/recommend/${userId}/${storeId}`);
    return response.data; // 根據你提供的規格，直接返回 RecommendItem 物件
  } catch (error) {
    console.error(`Error fetching recommendation for userId ${userId} and storeId ${storeId}:`, error);
    throw error;
  }
}