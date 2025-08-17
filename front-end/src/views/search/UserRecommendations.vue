<template>
  <SearchLayout>
    <div class="area items-center pr-3 pb-1 md:pb-2 md:w-4/12 md:!pt-5 lg:w-3/12 xl:w-3/12">
      <h4 v-if="userProfile" class="mb-2 text-sm md:hidden">
        {{ userProfile ? userProfile.name : '' }} 推薦列表
      </h4>
      <div
        class="flex-col items-center justify-center gap-2 md:flex md:gap-4"
        :class="isCategoryOpen ? 'flex' : 'hidden'"
      >
        <div v-if="userProfileLoading" class="loading">載入用戶資料...</div>
        <div v-else-if="userProfileError" class="error">
          載入用戶資料失敗: {{ userProfileError.message }}
        </div>
        <div
          v-else-if="userProfile"
          class="relative w-[70px] h-[70px] overflow-hidden rounded-full md:w-[172px] md:h-[172px]"
        >
          <img
            v-if="userProfile.profilePictureUrl"
            :src="userProfile.profilePictureUrl"
            alt="profile-picture"
          />
          <div v-else class="bg-gray-200"></div>
        </div>

        <div v-if="userProfile?.igUrl || userProfile?.ytUrl" class="flex items-center gap-3">
          <a v-if="userProfile.igUrl" :href="userProfile.igUrl" target="_blank">
            <img src="@/assets/images/icon-ig.png" alt="icon-ig" />
          </a>
          <a v-if="userProfile.ytUrl" :href="userProfile.ytUrl" target="_blank">
            <img src="@/assets/images/icon-yt.png" alt="icon-yt" />
          </a>
        </div>
        <div v-else class="text-center">找不到該用戶的公開資料</div>
        <CustomButton
          v-if="isCurrentUser"
          class="py-1.5 px-2 md:py-2 md:px-3"
          :buttonState="{
            color: 'secondary',
            label: '編輯',
            labelClass: 'text-xs md:text-base'
          }"
          @click="editDialogState.visible = true"
        />
      </div>
      <CustomButton
        class="bg-white !border-transparent md:hidden"
        :class="{ 'rotate-180': isCategoryOpen }"
        :buttonState="{
          color: 'white',
          icon: 'fa-solid fa-chevron-down'
        }"
        @click="isCategoryOpen = !isCategoryOpen"
      />
    </div>
    <div class="area gap-3 pr-0.5 pb-3 md:w-8/12 lg:w-9/12 xl:w-9/12">
      <div class="flex items-center mr-3">
        <h4 v-if="userProfile" class="hidden text-xl md:block">
          {{ userProfile ? userProfile.name : '' }} 推薦列表
        </h4>
        <div class="grid-list-icons ml-auto">
          <font-awesome-icon
            icon="fa-solid fa-grip"
            class="icon"
            :class="{ active: isGridView }"
            @click="isGridView = true"
          />
          <font-awesome-icon
            icon="fa-solid fa-list-ul"
            class="icon"
            :class="{ active: !isGridView }"
            @click="isGridView = false"
          />
        </div>
      </div>

      <ScrollBar
        class="py-1 pl-1 pr-3 md:max-h-[calc(100vh-184px)]"
        :class="
          isCategoryOpen
            ? 'max-h-[calc(100vh-340px)] sm:max-h-[calc(100vh-354px)]'
            : 'max-h-[calc(100vh-240px)] sm:max-h-[calc(100vh-248px)]'
        "
      >
        <p v-if="recommendationsLoading" class="loading">載入推薦中...</p>
        <p v-else-if="recommendationsError" class="error">
          錯誤: {{ recommendationsError.message || '無法載入推薦列表' }}
        </p>
        <p
          v-else-if="!recommendations.length && !recommendationsLoading && !recommendationsError"
          class="text-center"
        >
          目前沒有推薦的餐廳
        </p>

        <div
          v-else-if="recommendations.length && !recommendationsLoading"
          class="grid gap-2"
          :class="{ 'grid-cols-2 lg:grid-cols-3': isGridView }"
        >
          <template v-if="isGridView">
            <StoreCard v-for="item in recommendations" :key="item.id" :itemData="item" />
          </template>
          <template v-else>
            <StoreList v-for="item in recommendations" :key="item.id" :itemData="item" />
          </template>
        </div>
      </ScrollBar>
    </div>
  </SearchLayout>
  <CustomDialog :dialogState="editDialogState">
    <div class="flex flex-col gap-4 md:gap-7 md:flex-row">
      <!-- 大頭貼 -->
      <div class="relative w-fit h-fit mx-auto md:mx-0">
        <img
          v-if="profilePictureUrl"
          class="w-[70px] h-[70px] rounded-full overflow-hidden md:w-[116px] md:h-[116px]"
          :src="profilePictureUrl"
          :alt="values.name"
        />
        <label
          for="profilePictureFile"
          class="absolute bottom-0 right-0 flex items-center justify-center w-6 h-6 bg-white text-sm leading-none border border-gray-500 rounded-full overflow-hidden cursor-pointer"
        >
          <font-awesome-icon icon="fa-solid fa-camera" />
        </label>
        <input
          id="profilePictureFile"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleFileChange"
        />
      </div>
      <div class="grid gap-4 w-full md:w-[calc(100%-146px)]">
        <!-- 名稱 -->
        <Field v-slot="{ field, errorMessage }" name="name">
          <CustomInputText
            id="name"
            class="w-full"
            v-bind="field"
            :inputTextState="{
              width: 'md:!flex-row md:items-center',
              label: '名稱:',
              labelClass: 'w-[40px] md:!mb-0',
              modelValue: field.value,
              placeholder: '請輸入名稱',
              errorMessage: errorMessage
            }"
          />
        </Field>

        <!-- Instagram 連結 -->
        <div class="flex flex-col md:flex-row">
          <Field v-slot="{ field, errorMessage }" name="igUrl">
            <label for="igUrl" class="mb-2 md:-mb-1.5 md:w-[40px]">
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
            <label for="ytUrl" class="mb-2 md:-mb-1.5 md:w-[40px]">
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
        <div v-if="error" class="error">{{ error }}</div>
      </div>
    </div>
  </CustomDialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import SearchLayout from '@/components/layout/SearchLayout.vue'
import StoreCard from '@/components/forPage/StoreCard.vue'
import StoreList from '@/components/forPage/StoreList.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomDialog from '@/components/CustomDialog.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import {
  getUserPublicProfile,
  type UserPublicProfile,
  updateUserProfile
} from '@/services/userService'
import type { RecommendItem } from '@/types/recommend'
import { getRecommendationsByUserId } from '@/services/recommendService'
import { useUserStore } from '@/stores/user'
import { useForm, Field } from 'vee-validate'
import * as yup from 'yup'

const route = useRoute()
const userStore = useUserStore()

const isGridView = ref(true)
const isCategoryOpen = ref(false)

// 推薦列表相關狀態
const recommendations = ref<RecommendItem[]>([])
const recommendationsLoading = ref(true)
const recommendationsError = ref<Error | null>(null)

// 用戶資料相關狀態
const userProfile = ref<UserPublicProfile | null>(null)
const userProfileLoading = ref(true)
const userProfileError = ref<Error | null>(null)

const profilePictureFile = ref<File | null>(null)
const isLoading = ref(false)
const error = ref('')
const profilePictureUrl = ref<string | null>(null)

const validationSchema = yup.object({
  name: yup.string().required('名稱為必填項目'),
  ytUrl: yup.string().url('YouTube 連結格式不正確').nullable(),
  igUrl: yup.string().url('Instagram 連結格式不正確').nullable()
})

const { handleSubmit: veeValidateSubmit, setValues, values } = useForm({ validationSchema })

// 處理檔案變更
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    profilePictureFile.value = target.files[0]
    profilePictureUrl.value = URL.createObjectURL(profilePictureFile.value)
  } else {
    profilePictureFile.value = null
    profilePictureUrl.value = userProfile.value?.profilePictureUrl || null
  }
}

// 處理表單提交
const handleSubmit = veeValidateSubmit(async (values) => {
  isLoading.value = true
  error.value = ''
  try {
    const formData = new FormData()
    formData.append('name', values.name)
    formData.append('ytUrl', values.ytUrl || '')
    formData.append('igUrl', values.igUrl || '')
    if (profilePictureFile.value) {
      formData.append('profilePictureFile', profilePictureFile.value)
    }

    const updatedProfile = await updateUserProfile(formData)
    userStore.updateUser(updatedProfile)
    userProfile.value = updatedProfile
    profilePictureUrl.value = updatedProfile.profilePictureUrl || null
    editDialogState.visible = false
  } catch (err: any) {
    error.value = err.message || '更新失敗，請稍後再試'
  } finally {
    isLoading.value = false
  }
})

const editDialogState = reactive({
  visible: false,
  width: 'w-[90vw] md:!w-[60vw]',
  scrollBarClass: '!max-h-[calc(100vh-230px)] md:!max-h-[calc(100vh-200px)]',
  onSubmit: handleSubmit,
  cancelClick: () => {
    editDialogState.visible = false
  },
  confirmClick: () => handleSubmit()
})

const isCurrentUser = computed(() => {
  return userProfile.value?.id === userStore.user?.id
})

// 用戶推薦列表
const fetchRecommendations = async (userId: number) => {
  recommendationsLoading.value = true
  recommendationsError.value = null
  try {
    const data = await getRecommendationsByUserId(userId)
    recommendations.value = data
  } catch (err: any) {
    recommendationsError.value = err
    console.error('Error fetching user recommendations:', err)
  } finally {
    recommendationsLoading.value = false
  }
}

// 用戶公開資料
const fetchUserProfile = async (userId: number) => {
  userProfileLoading.value = true
  userProfileError.value = null
  try {
    const data = await getUserPublicProfile(userId)
    userProfile.value = data
    setValues({
      name: data.name,
      ytUrl: data.ytUrl || '',
      igUrl: data.igUrl || ''
    })
    profilePictureUrl.value = data.profilePictureUrl || null
  } catch (err: any) {
    userProfileError.value = err
    console.error('Error fetching user profile:', err)
  } finally {
    userProfileLoading.value = false
  }
}

onMounted(async () => {
  const userId = Number(route.params.userId)
  if (userId) {
    await Promise.all([fetchRecommendations(userId), fetchUserProfile(userId)])
  } else {
    userProfileError.value = new Error('無效的用戶 ID')
    recommendationsLoading.value = false
  }
})
</script>
