// src/services/storeService.ts
import api from './api'
import type { Store, StoreDetails } from '@/types/store'

export async function getStoresRandomly(limit: number = 3): Promise<Store[]> {
    try {
        const response = await api.get<Store[]>('/store/random', {
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

// 新增函式：取得指定店家的詳細資訊
export async function getStoreDetails(storeId: number): Promise<StoreDetails> {
    try {
        const response = await api.get<StoreDetails>(`/store/${storeId}/details`)
        return response.data
    } catch (error) {
        console.error(`Error fetching store details for storeId ${storeId}:`, error)
        throw error
    }
}