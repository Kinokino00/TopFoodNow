<template>
  <AuthLayout>
    <ScrollBar
      class="max-h-[calc(100vh-96px)] px-4 sm:max-h-[calc(100vh-120px)] md:max-h-[calc(100vh-134px)]"
    >
      <div class="flex flex-col items-center gap-3 p-3">
        <p v-if="loading" class="loading">正在驗證您的帳戶，請稍候...</p>
        <template v-else-if="success">
          <font-awesome-icon icon="fa-solid fa-check-circle" class="text-5xl text-primary-500" />
          <p class="text-lg text-primary-500">您的帳戶已成功驗證！</p>
          <p>
            您將在 <span class="font-bold">{{ countdown }}</span> 秒後自動跳轉到登入頁面
          </p>
        </template>
        <template v-else-if="error">
          <font-awesome-icon icon="fa-solid fa-times-circle" class="error text-5xl" />
          <p class="error text-lg">帳戶驗證失敗：{{ error }}</p>
          <RouterLink class="text-underline" to="/register">重新註冊</RouterLink>
        </template>
      </div>
    </ScrollBar>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import { verifyAccount } from '@/services/authService'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const success = ref(false)
const error = ref<string | null>(null)
const countdown = ref(5)

onMounted(async () => {
  const verificationCode = route.query.code as string

  if (!verificationCode) {
    error.value = '驗證碼遺失'
    loading.value = false
    return
  }

  try {
    await verifyAccount(verificationCode)
    success.value = true
    loading.value = false

    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        router.push({ name: 'Login' })
      }
    }, 1000)
  } catch (err: any) {
    error.value = err.response?.data?.message || '驗證帳戶時發生錯誤。'
    loading.value = false
  }
})
</script>
