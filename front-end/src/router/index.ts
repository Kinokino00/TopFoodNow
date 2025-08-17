import { createRouter, createWebHistory } from 'vue-router'
import auth from './auth'
import search from './search'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/DashboardPage.vue'),
      meta: {
        title: 'Dashboard',
        requiresAuth: true
      },
    },
    ...auth,
    ...search,
    {
      path: '/cc',
      name: 'componentPage',
      component: () => import('@/views/ComponentPage.vue'),
      meta: {
        title: '元件',
        requiresAuth: true
      },
    },
  ]
})

router.beforeEach(async (to, from, next) => {
  document.title = `${to.meta.title} | TopFoodNow`

  if (to.meta.requiresAuth) return next()

  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    const isAuthenticated = await userStore.isAuthenticated()

    if (isAuthenticated) {
      next()
    } else {
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

export default router
