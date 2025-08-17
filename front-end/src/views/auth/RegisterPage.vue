<template>
  <AuthLayout title="註冊">
    <form @submit="handleRegister">
      <ScrollBar class="max-h-[calc(100vh-250px)] px-4 md:max-h-[calc(100vh-330px)]">
        <div class="flex flex-col gap-3 md:flex-row md:gap-7">
          <!-- 大頭貼 -->
          <div
            class="flex items-center justify-center w-[70px] h-[70px] mx-auto border border-gray-500 rounded-full cursor-pointer md:w-[116px] md:h-[116px] md:mx-0"
          >
            <label for="profilePictureFile" class="text-[40px] leading-none">
              <font-awesome-icon icon="fa-solid fa-camera" />
            </label>
            <input
              id="profilePictureFile"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileChange"
            />
            <p v-if="profilePictureFileError" class="error">
              {{ profilePictureFileError }}
            </p>
          </div>
          <div class="grid gap-3 w-[calc(100%-146px)]">
            <!-- 名稱 -->
            <Field v-slot="{ field, errorMessage }" name="name">
              <CustomInputText
                id="name"
                class="w-full"
                v-bind="field"
                :inputTextState="{
                  width: 'md:!flex-row md:items-center',
                  label: '名稱:',
                  labelClass: 'w-[80px] md:!mb-0',
                  modelValue: field.value,
                  placeholder: '請輸入名稱',
                  errorMessage: errorMessage
                }"
              />
            </Field>

            <!-- 信箱 -->
            <Field v-slot="{ field, errorMessage }" name="email">
              <CustomInputText
                id="email"
                class="w-full"
                v-bind="field"
                :inputTextState="{
                  width: 'md:!flex-row md:items-center',
                  label: '信箱:',
                  labelClass: 'w-[80px] md:!mb-0',
                  modelValue: field.value,
                  placeholder: '請輸入信箱',
                  errorMessage: errorMessage
                }"
              />
            </Field>

            <!-- 密碼 -->
            <Field v-slot="{ field, errorMessage }" name="password">
              <CustomPassword
                id="password"
                class="w-full"
                v-bind="field"
                :passwordState="{
                  width: 'md:!flex-row md:items-center',
                  label: '密碼:',
                  labelClass: 'w-[80px] md:!mb-0',
                  modelValue: field.value,
                  placeholder: '請輸入密碼',
                  errorMessage: errorMessage
                }"
              />
            </Field>

            <!-- 確認密碼 -->
            <Field v-slot="{ field, errorMessage }" name="confirmPassword">
              <CustomPassword
                id="confirmPassword"
                class="w-full"
                v-bind="field"
                :passwordState="{
                  width: 'md:!flex-row md:items-center',
                  label: '確認密碼:',
                  labelClass: 'w-[80px] md:!mb-0',
                  modelValue: field.value,
                  placeholder: '請再次輸入密碼',
                  errorMessage: errorMessage
                }"
              />
            </Field>

            <!-- Instagram 連結 -->
            <div class="flex flex-col md:flex-row">
              <Field v-slot="{ field, errorMessage }" name="igUrl">
                <label for="igUrl" class="-mb-1.5 md:w-[80px]">
                  <img class="w-7 h-7" src="@/assets/images/icon-ig.png" alt="icon-ig" />
                </label>
                <CustomInputText
                  id="igUrl"
                  class="w-full"
                  v-bind="field"
                  :inputTextState="{
                    modelValue: field.value,
                    placeholder: '請輸入 Instagram 連結',
                    errorMessage: errorMessage
                  }"
                />
              </Field>
            </div>

            <!-- YouTube 連結 -->
            <div class="flex flex-col md:flex-row">
              <Field v-slot="{ field, errorMessage }" name="ytUrl">
                <label for="ytUrl" class="-mb-1.5 md:w-[80px]">
                  <img class="w-7 h-7" src="@/assets/images/icon-yt.png" alt="icon-yt" />
                </label>
                <CustomInputText
                  id="ytUrl"
                  class="w-full"
                  v-bind="field"
                  :inputTextState="{
                    modelValue: field.value,
                    placeholder: '請輸入 YouTube 連結',
                    errorMessage: errorMessage
                  }"
                />
              </Field>
            </div>
          </div>

          <div v-if="apiError" class="error">{{ apiError }}</div>
          <div v-if="successMessage" class="text-center">
            {{ successMessage }}
          </div>
        </div>
      </ScrollBar>

      <!-- 註冊 -->
      <CustomButton
        id="register"
        class="w-[calc(100%-2rem)] mt-5 ml-4 md:mt-7"
        :buttonState="{
          size: 'md',
          color: 'primary',
          btnType: 'submit',
          label: '註冊',
          labelClass: 'md:text-base'
        }"
      />
    </form>
    <p class="text-center">
      已有帳號?
      <RouterLink class="text-underline" to="/login">登入</RouterLink>
    </p>
  </AuthLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useForm, Field } from 'vee-validate'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomPassword from '@/components/CustomPassword.vue'
import CustomButton from '@/components/CustomButton.vue'
import { register, type RegisterPayload } from '@/services/authService'
import * as yup from 'yup'

const router = useRouter()

const apiError = ref<string | null>(null)
const successMessage = ref<string | null>(null)
const profilePictureFile = ref<File | null>(null)
const profilePictureFileError = ref<string | null>(null)

const validationSchema = yup.object({
  name: yup.string().required('姓名為必填'),
  email: yup.string().required('信箱為必填').email('信箱格式不正確'),
  password: yup.string().required('密碼為必填').min(8, '密碼至少為 8 個字元'),
  confirmPassword: yup
    .string()
    .required('請再次輸入密碼')
    .oneOf([yup.ref('password')], '兩次輸入的密碼不一致'),
  ytUrl: yup.string().url('YouTube 連結格式不正確').nullable(),
  igUrl: yup.string().url('Instagram 連結格式不正確').nullable()
})

const { handleSubmit, setFieldValue } = useForm({
  validationSchema
})

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (!file.type.startsWith('image/')) {
      profilePictureFileError.value = '請上傳圖片檔案'
      profilePictureFile.value = null
      return
    }
    if (file.size > 2 * 1024 * 1024) {
      profilePictureFileError.value = '圖片檔案大小不能超過 2MB'
      profilePictureFile.value = null
      return
    }
    profilePictureFile.value = file
    profilePictureFileError.value = null
  } else {
    profilePictureFile.value = null
    profilePictureFileError.value = null
  }
}

const handleRegister = handleSubmit(async (values) => {
  apiError.value = null
  successMessage.value = null

  try {
    const payload: RegisterPayload = {
      email: values.email,
      password: values.password,
      name: values.name,
      ytUrl: values.ytUrl || undefined,
      igUrl: values.igUrl || undefined,
      profilePictureFile: profilePictureFile.value
    }

    await register(payload)

    successMessage.value = '註冊成功！請檢查您的電子郵件以驗證帳戶'
    setFieldValue('name', '')
    setFieldValue('email', '')
    setFieldValue('password', '')
    setFieldValue('confirmPassword', '')
    setFieldValue('ytUrl', '')
    setFieldValue('igUrl', '')
    profilePictureFile.value = null
    ;(document.getElementById('profilePictureFile') as HTMLInputElement).value = ''

    setTimeout(() => {
      router.push({ name: 'Login' })
    }, 3000)
  } catch (err: any) {
    apiError.value = err.response?.data?.message || '註冊失敗，請重試'
    console.error('Registration error:', err)
  }
})
</script>
