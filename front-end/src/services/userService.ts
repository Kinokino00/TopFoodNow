import api from './api'
import { useUserStore } from '@/stores/user'

export interface UserPublicProfile {
  id: number
  email: string
  name: string
  ytUrl?: string | null
  igUrl?: string | null
  profilePictureUrl?: string | null
}

export async function getUserPublicProfile(userId: number): Promise<UserPublicProfile> {
  try {
    const response = await api.get<UserPublicProfile>(`/auth/public/${userId}`)
    return response.data
  } catch (error) {
    console.error(`Error fetching public profile for user ${userId}:`, error)
    throw error
  }
}

export async function updateUserProfile(data: FormData): Promise<UserPublicProfile> {
  const userStore = useUserStore()
  const token = userStore.token
  if (!token) throw new Error('未經認證，請重新登入')
  try {
    const response = await api.put('/auth/profile', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })
    return response.data
  } catch (error) {
    console.error('更新用戶資料失敗:', error)
    throw error
  }
}
