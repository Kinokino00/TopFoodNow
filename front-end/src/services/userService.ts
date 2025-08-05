import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

const api = axios.create({
  baseURL: API_BASE_URL,
})

// 定義用戶公共資料的類型
export interface UserPublicProfile {
  id: number
  email: string
  name: string
  ytUrl?: string | null
  igUrl?: string | null
  profilePictureUrl?: string | null
}

// 取得指定用戶的公共資料 (無認證)
export async function getUserPublicProfile(userId: number): Promise<UserPublicProfile> {
  try {
    const response = await api.get<UserPublicProfile>(`/api/auth/public/${userId}`)
    return response.data
  } catch (error) {
    console.error(`Error fetching public profile for user ${userId}:`, error)
    throw error
  }
}