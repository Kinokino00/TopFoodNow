<template>
  <AuthLayout title="忘記密碼">
    <form class="flex flex-col gap-3" @submit="handleForgotPassword">
      <ScrollBar
        class="max-h-[calc(100vh-230px)] px-4 sm:max-h-[calc(100vh-252px)] md:max-h-[calc(100vh-298px)]"
      >
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
                placeholder: '請輸入註冊時的信箱',
                errorMessage: errorMessage
              }"
            />
          </Field>
        </div>
      </ScrollBar>

      <div v-if="successMessage" class="text-primary-500">{{ successMessage }}</div>
      <div v-if="error" class="error">{{ error }}</div>

      <CustomButton
        v-else
        id="login"
        class="w-[calc(100%-2rem)] mt-5 ml-4 md:mt-7"
        :buttonState="{
          size: 'md',
          color: 'primary',
          btnType: 'submit',
          label: '發送重設連結',
          labelClass: 'md:text-base'
        }"
      />
    </form>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useForm, Field } from 'vee-validate'
import * as yup from 'yup'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomButton from '@/components/CustomButton.vue'
import { forgotPassword } from '@/services/authService'

const validationSchema = yup.object({
  email: yup.string().required('信箱為必填').email('信箱格式不正確')
})

const { handleSubmit } = useForm({
  validationSchema
})

const error = ref<string | null>(null)
const successMessage = ref<string | null>(null)

const handleForgotPassword = handleSubmit(async (values) => {
  error.value = null
  successMessage.value = null

  try {
    await forgotPassword(values.email)
  } catch (err: any) {
    console.error('Forgot password error:', err)
  } finally {
    successMessage.value = '如果您的信箱存在於我們的系統中，重設密碼連結已發送'
  }
})
</script>
