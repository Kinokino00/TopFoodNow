<template>
  <AuthLayout title="登入">
    <form class="flex flex-col gap-3" @submit="handleLogin">
      <ScrollBar class="max-h-[calc(100vh-270px)] px-4 md:max-h-[calc(100vh-340px)]">
        <div class="grid gap-3">
          <Field v-slot="{ field, errorMessage }" name="email" type="email">
            <CustomInputText
              id="email"
              class="w-full"
              v-bind="field"
              :inputTextState="{
                width: 'md:!flex-row md:items-center',
                label: '信箱:',
                labelClass: 'w-[40px] md:!mb-0',
                modelValue: field.value,
                placeholder: '請輸入信箱',
                errorMessage: errorMessage
              }"
            />
          </Field>

          <Field v-slot="{ field, errorMessage }" name="password">
            <CustomPassword
              id="password"
              class="w-full"
              v-bind="field"
              :passwordState="{
                width: 'md:!flex-row md:items-center',
                label: '密碼:',
                labelClass: 'w-[40px] md:!mb-0',
                modelValue: field.value,
                placeholder: '請輸入密碼',
                errorMessage: errorMessage
              }"
            />
          </Field>
          <RouterLink
            class="text-underline -mt-2 text-end text-xxs md:text-sm"
            to="/forgot-password"
            >忘記密碼</RouterLink
          >
        </div>
      </ScrollBar>

      <div v-if="loading" class="loading">登入中...</div>
      <div v-if="error" class="error">{{ error }}</div>

      <CustomButton
        id="login"
        class="w-[calc(100%-2rem)] mt-5 ml-4 md:mt-7"
        :buttonState="{
          size: 'md',
          color: 'primary',
          btnType: 'submit',
          label: '登入',
          labelClass: 'md:text-base'
        }"
      />
    </form>
    <p class="text-center">
      還沒有帳號?
      <RouterLink class="text-underline" to="/register">註冊</RouterLink>
    </p>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useForm, Field } from 'vee-validate'
import { useRouter } from 'vue-router'
import * as yup from 'yup'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomPassword from '@/components/CustomPassword.vue'
import CustomButton from '@/components/CustomButton.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const validationSchema = yup.object({
  email: yup.string().required('信箱為必填').email('信箱格式不正確'),
  password: yup.string().required('密碼為必填')
})

const { handleSubmit } = useForm({
  validationSchema
})

const loading = ref(false)
const error = ref<string | null>(null)

const handleLogin = handleSubmit(async (values) => {
  loading.value = true
  error.value = null

  try {
    const userId = await userStore.handleLogin({
      email: values.email,
      password: values.password
    })
    router.push({ name: 'userRecommendations', params: { userId: userId.toString() } })
  } catch (err: any) {
    error.value = '登入失敗，請檢查您的信箱和密碼'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
})
</script>
