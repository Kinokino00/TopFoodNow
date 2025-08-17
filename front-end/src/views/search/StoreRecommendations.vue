<template>
  <SearchLayout>
    <div class="area pr-3 pb-1 md:w-4/12 xl:w-3/12">
      <h4 v-if="storeDetails" class="mb-2 text-center text-sm md:hidden">
        {{ storeDetails ? storeDetails.storeName : '' }} 推薦列表
      </h4>
      <div
        class="flex-col items-center justify-center gap-2 md:flex md:gap-3"
        :class="isCategoryOpen ? 'flex' : 'hidden'"
      >
        <div v-if="storeDetailsLoading" class="loading">載入店家資料...</div>
        <div v-else-if="storeDetailsError" class="error">
          載入店家資料失敗: {{ storeDetailsError.message }}
        </div>

        <template v-if="storeDetails">
          <h3 class="store-name hidden !text-lg md:block">{{ storeDetails.storeName }}</h3>
          <p>{{ storeDetails.address }}</p>
          <div class="store-star !relative !left-0 !bottom-0">
            <font-awesome-icon
              v-for="sc in Math.floor(storeDetails.averageScore || 0)"
              :key="'solid-' + storeDetails.id + '-' + sc"
              icon="fa-solid fa-star"
            />
            <font-awesome-icon
              v-for="sc in 5 - Math.ceil(storeDetails.averageScore || 0)"
              :key="'regular-' + storeDetails.id + '-' + sc"
              icon="fa-regular fa-star"
            />
          </div>
          <div class="flex flex-wrap gap-1 my-1 mx-2">
            <div v-for="cat in storeDetails.categoryNames || []" :key="cat" class="store-category">
              {{ cat }}
            </div>
          </div>
        </template>
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

    <div class="area gap-3 pr-0.5 pb-3 md:w-8/12 xl:w-9/12">
      <div class="flex items-center mr-3">
        <h4 v-if="storeDetails" class="hidden text-xl md:block">
          {{ storeDetails ? storeDetails.storeName : '' }} 推薦列表
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
            ? 'max-h-[calc(100vh-310px)] sm:max-h-[calc(100vh-330px)]'
            : 'max-h-[calc(100vh-236px)] sm:max-h-[calc(100vh-244px)]'
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
          目前沒有這間店家的推薦
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
import CustomButton from '@/components/CustomButton.vue'
import StoreCard from '@/components/forPage/StoreCard.vue'
import StoreList from '@/components/forPage/StoreList.vue'
import { getRecommendationsByStoreId } from '@/services/recommendService'
import type { RecommendItem } from '@/types/recommend'
import type { StoreDetails } from '@/types/store'
import { getStoreDetails } from '@/services/storeService'

const route = useRoute()

const isGridView = ref(true)
const isCategoryOpen = ref(false)

// 推薦列表相關狀態
const recommendations = ref<RecommendItem[]>([])
const recommendationsLoading = ref(true)
const recommendationsError = ref<Error | null>(null)

// 店家資料相關狀態
const storeDetails = ref<StoreDetails | null>(null)
const storeDetailsLoading = ref(true)
const storeDetailsError = ref<Error | null>(null)

const fetchStoreRecommendations = async (storeId: number) => {
  recommendationsLoading.value = true
  storeDetailsError.value = null
  try {
    const data = await getRecommendationsByStoreId(storeId)
    recommendations.value = data
  } catch (err: any) {
    storeDetailsError.value = err
    console.error(`Error fetching recommendations for storeId ${storeId}:`, err)
  } finally {
    recommendationsLoading.value = false
  }
}

// 店家公開資料
const fetchStoreDetails = async (storeId: number) => {
  storeDetailsLoading.value = true
  storeDetailsError.value = null
  try {
    const data = await getStoreDetails(storeId)
    storeDetails.value = data
  } catch (err: any) {
    storeDetailsError.value = err
    console.error('Error fetching store profile:', err)
  } finally {
    storeDetailsLoading.value = false
  }
}

onMounted(async () => {
  const storeId = Number(route.params.storeId)
  if (storeId) {
    await Promise.all([fetchStoreRecommendations(storeId), fetchStoreDetails(storeId)])
  } else {
    storeDetailsError.value = new Error('無效的店家 ID')
    recommendationsLoading.value = false
  }
})

onMounted(() => {})
</script>
