import { defineStore } from 'pinia'
import { login, logout, type LoginPayload } from '@/services/authService'
import type { UserPublicProfile } from '@/services/userService' 

export interface UserState {
  token: string | null
  user: {
    id: number
    email: string
    name: string
  } | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('authToken') || null,
    user: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    /**
     * 登入
     * @param payload 包含 email 和 password 的物件
     * @returns 登入成功的用戶 ID，如果登入失敗則拋出錯誤
     */
    async handleLogin(payload: LoginPayload): Promise<number> {
      try {
        const { token, user } = await login(payload)

        this.token = token
        localStorage.setItem('authToken', token)

        this.user = user
        localStorage.setItem('userData', JSON.stringify(user))

        const userId = this.user?.id
        if (userId) {
          return userId
        } else {
          throw new Error('User ID not found in login response.')
        }
      } catch (error) {
        this.token = null
        this.user = null
        localStorage.removeItem('authToken')
        localStorage.removeItem('userData')
        console.error('Login failed in Pinia store:', error)
        throw error
      }
    },

    // 登出
    async handleLogout() {
      const token = this.token
      try {
        if (token) {
          await logout(token)
          this.token = null
          this.user = null
          localStorage.removeItem('authToken')
          localStorage.removeItem('userData')
        }
      } catch (error) {
        console.error('Logout failed:', error)
        this.token = null
        this.user = null
        localStorage.removeItem('authToken')
        localStorage.removeItem('userData')
      }
    },

    // 從 localStorage 載入用戶資料
    loadUserData() {
      const storedToken = localStorage.getItem('authToken')
      const storedUser = localStorage.getItem('userData')
      if (storedToken && storedUser) {
        this.token = storedToken
        this.user = JSON.parse(storedUser)
      }
    },

    /**
     * 更新用戶資料
     * @param updatedUser 更新後的用戶資料物件
     */
    updateUser(updatedUser: UserPublicProfile) {
      if (this.user) {
        this.user = { ...this.user, ...updatedUser }
        localStorage.setItem('userData', JSON.stringify(this.user))
      }
    }
  }
})