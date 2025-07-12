import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/DashboardPage.vue'),
      meta: {
        title: 'Dashboard',
        requiresAuth: true
      },
    },
    {
      path: '/cc',
      name: 'componentPage',
      component: () => import('@/views/ComponentPage.vue'),
      meta: {
        title: '元件',
        requiresAuth: true
      },
    },
    {
      path: '/sheetJs',
      name: 'sheetJs',
      component: () => import('@/views/SheetJs.vue'),
      meta: {
        title: 'SheetJs',
        requiresAuth: true
      },
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title} | 能源管理系統`

  if (to.meta.requiresAuth) return next()

  // if (!to.meta.requiresAuth) return next()
  // try {
  //   // 登入驗證
  //   const userStore = useUserStore()
  //   const isAuthenticated = await userStore.isAuthenticated()
  //   isAuthenticated ? next() : next({ name: 'Login' })
  // } catch {
  //   next({ name: 'Login' })
  // }
})

export default router
