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
      <div class="flex items-center justify-end mr-3 md:justify-between">
        <h4 v-if="userProfile" class="hidden text-xl md:block">
          {{ userProfile ? userProfile.name : '' }} 推薦列表
        </h4>
        <div class="grid-list-icons">
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import SearchLayout from '@/components/layout/SearchLayout.vue'
import StoreCard from '@/components/forPage/StoreCard.vue'
import StoreList from '@/components/forPage/StoreList.vue'
import CustomButton from '@/components/CustomButton.vue'
import { getUserPublicProfile, type UserPublicProfile } from '@/services/userService'
import type { RecommendItem } from '@/types/recommend'
import { getRecommendationsByUserId } from '@/services/recommendService'

const route = useRoute()

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

// 獲取用戶推薦列表的函數
const fetchRecommendations = async () => {
  const routeUserId = route.params.userId
  if (typeof routeUserId !== 'string') {
    recommendationsError.value = new Error('Invalid User ID provided for recommendations.')
    recommendationsLoading.value = false
    return
  }

  recommendationsLoading.value = true
  recommendationsError.value = null
  try {
    const data = await getRecommendationsByUserId(Number(routeUserId))
    recommendations.value = data
  } catch (err: any) {
    recommendationsError.value = err
    console.error('Error fetching user recommendations:', err)
  } finally {
    recommendationsLoading.value = false
  }
}

// 獲取用戶公共資料的函數
const fetchUserProfile = async () => {
  const routeUserId = route.params.userId
  if (typeof routeUserId !== 'string') {
    userProfileError.value = new Error('Invalid User ID provided for profile.')
    userProfileLoading.value = false
    return
  }

  userProfileLoading.value = true
  userProfileError.value = null
  try {
    const data = await getUserPublicProfile(Number(routeUserId))
    userProfile.value = data
  } catch (err: any) {
    userProfileError.value = err
    console.error('Error fetching user profile:', err)
  } finally {
    userProfileLoading.value = false
  }
}

onMounted(async () => {
  // 並行載入用戶推薦和用戶資料
  await Promise.all([fetchRecommendations(), fetchUserProfile()])
})
</script>

<style lang="scss" scoped></style>
