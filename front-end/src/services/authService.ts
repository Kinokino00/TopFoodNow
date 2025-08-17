import api from './api'

// 登入請求的資料型別
export interface LoginPayload {
    email: string
    password: string
}

export interface ApiLoginResponse {
    success: boolean
    message: string
    token: string
    user: {
        id: number
        role?: {
            id: number
            name: string
        }
        email: string
        password?: string
        name: string
        enabled?: boolean
        verificationCode?: string
        resetPasswordToken?: string
        resetPasswordExpiryDate?: string
        ytUrl?: string
        igUrl?: string
        profilePictureUrl?: string
        resetPasswordTokenExpired?: boolean
    }
}

// Pinia Store 期望的回應型別
export interface LoginResponse {
    token: string
    user: {
        id: number
        email: string
        name: string
    }
}

export interface RegisterPayload {
    email: string
    password: string
    name: string
    ytUrl?: string
    igUrl?: string
    profilePictureFile?: File | null
}

/**
 * 處理用戶登入
 * @param payload 包含 email 和 password 的物件
 * @returns Promise<LoginResponse>
 */
export async function login(payload: LoginPayload): Promise<LoginResponse> {
    try {
        const response = await api.post<ApiLoginResponse>('/auth/login', payload)

        if (response.data.success) {
            const { token, user } = response.data

            const transformedResponse: LoginResponse = {
                token: token,
                user: {
                    id: user.id,
                    email: user.email,
                    name: user.name
                }
            }
            return transformedResponse
        } else {
            throw new Error(response.data.message || '登入失敗')
        }
    } catch (error) {
        console.error('Login failed:', error)
        throw error
    }
}

/**
 * 處理用戶登出
 * @param token JWT Token
 */
export async function logout(token: string) {
    try {
        await api.post('/auth/logout', token, {
            headers: {
                'Content-Type': 'text/plain'
            }
        });
    } catch (error) {
        console.error('Logout failed:', error)
        throw error
    }
}

/**
 * 處理用戶註冊
 * @param payload 包含註冊資訊的物件
 * @returns Promise<void> (註冊成功不回傳特定資料，狀態碼 201)
 */
export async function register(payload: RegisterPayload): Promise<void> {
    try {
        const formData = new FormData()
        formData.append('email', payload.email)
        formData.append('password', payload.password)
        formData.append('name', payload.name)

        if (payload.ytUrl) formData.append('ytUrl', payload.ytUrl)
        if (payload.igUrl) formData.append('igUrl', payload.igUrl)
        if (payload.profilePictureFile) formData.append('profilePictureFile', payload.profilePictureFile)

        await api.post('/auth/register', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    } catch (error) {
        console.error('Registration failed:', error)
        throw error
    }
}

/**
 * 驗證用戶帳戶
 * @param verificationCode 從郵件連結中取得的驗證碼
 * @returns Promise<void>
 */
export async function verifyAccount(verificationCode: string): Promise<void> {
    try {
        await api.get(`/auth/verify?code=${verificationCode}`)
    } catch (error) {
        console.error('Account verification failed:', error)
        throw error
    }
}

/**
 * 處理忘記密碼請求
 * @param email 用戶的電子信箱
 */
export async function forgotPassword(email: string): Promise<void> {
    try {
        await api.post('/auth/forgot-password', { email })
    } catch (error) {
        console.error('Forgot password request failed:', error)
        throw error
    }
}
