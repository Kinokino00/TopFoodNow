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
    }
]

export default auth