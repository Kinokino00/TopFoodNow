import type { RouteRecordRaw } from 'vue-router'

const search: RouteRecordRaw[] = [
    {
        path: '/search',
        name: 'searchList',
        component: () => import('@/views/search/SearchList.vue'),
        meta: {
            title: '推薦餐廳搜尋列表',
            requiresAuth: false
        }
    },
    {
        path: '/recommendations/:userId/:storeId',
        name: 'recommendationDetail',
        component: () => import('@/views/search/RecommendationDetail.vue'),
        props: true,
        meta: {
            title: '推薦詳細資訊',
            requiresAuth: false
        }
    },
    {
        path: '/user/:userId/recommendations',
        name: 'userRecommendations',
        component: () => import('@/views/search/UserRecommendations.vue'),
        props: true,
        meta: {
            title: '用戶的推薦列表',
            requiresAuth: false
        },
    },
    {
        path: '/store/:storeId',
        name: 'storeRecommendations',
        component: () => import('@/views/search/StoreRecommendations.vue'),
        props: true,
        meta: {
            title: '店家推薦列表',
            requiresAuth: false,
        },
    },
]

export default search