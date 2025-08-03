import type { RouteRecordRaw } from 'vue-router'

const auth: RouteRecordRaw[] = [
    {
        path: '/search',
        name: 'searchList',
        component: () => import('@/views/search/SearchList.vue'),
        meta: {
            title: '推薦餐廳搜尋列表',
            requiresAuth: false
        }
    },
]

export default auth