import type { RouteRecordRaw } from 'vue-router'

const auth: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/auth/LoginPage.vue'),
        meta: {
            title: '登入',
            requiresAuth: false
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/auth/RegisterPage.vue'),
        meta: {
            title: '註冊',
            requiresAuth: false
        }
    },
    {
        path: '/verify',
        name: 'VerifyAccount',
        component: () => import('@/views/auth/VerifyAccountPage.vue'),
        meta: {
            title: '驗證帳戶',
            requiresAuth: false
        }
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPassword.vue'),
        meta: {
            title: '忘記密碼',
            requiresAuth: false
        }
    }
]

export default auth