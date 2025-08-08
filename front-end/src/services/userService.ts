import api from './api'

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