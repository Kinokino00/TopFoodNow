<template>
  <SearchLayout>
    <!-- <CustomButton
      v-if="isCurrentUser"
      class="py-1.5 px-2 md:py-2 md:px-3"
      :buttonState="{
        color: 'secondary',
        label: '編輯',
        labelClass: 'text-xs md:text-base'
      }"
      @click="editDialogState.visible = true"
    /> -->
    <div class="area gap-3 pr-0.5 pb-3">
      <ScrollBar
        class="py-1 pl-1 pr-3 max-h-[calc(100vh-108px)] sm:max-h-[calc(100vh-116px)] md:max-h-[calc(100vh-144px)]"
      >
        <p v-if="loading" class="loading">載入推薦資訊中...</p>
        <p v-else-if="error" class="error">錯誤: {{ error.message || '無法載入推薦詳細資訊' }}</p>

        <div v-else-if="recommendation" class="flex flex-col gap-3 sm:flex-row sm:gap-4">
          <div
            v-if="recommendation.photoUrls && recommendation.photoUrls.length"
            class="store-image-container relative overflow-hidden sm:w-6/12 lg:w-4/12"
          >
            <div
              class="store-image-wrapper flex transition-transform duration-300 ease-in-out"
              :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
            >
              <div
                v-for="(url, index) in recommendation.photoUrls"
                :key="index"
                class="store-image flex-shrink-0 w-full"
              >
                <img :src="url" :alt="recommendation.storeName" />
              </div>
            </div>

            <div
              v-if="recommendation.photoUrls.length > 1"
              class="flex items-center justify-between gap-2 mt-1 mx-auto lg:w-[76%]"
            >
              <button class="text-sm" @click="prevImage">
                <font-awesome-icon icon="fa-solid fa-chevron-left" />
              </button>
              <div class="flex items-center gap-1">
                <font-awesome-icon
                  v-for="(_, index) in recommendation.photoUrls"
                  :key="'circle-' + index"
                  :icon="index === currentIndex ? 'fa-solid fa-circle' : 'fa-regular fa-circle'"
                  class="text-[8px] cursor-pointer"
                  @click="goToImage(index)"
                />
              </div>
              <button class="text-sm" @click="nextImage">
                <font-awesome-icon icon="fa-solid fa-chevron-right" />
              </button>
            </div>
          </div>
          <div v-else class="store-image">
            <font-awesome-icon icon="fa-solid fa-shop" class="icon" />
          </div>

          <div class="grid gap-2.5 sm:w-6/12 lg:w-8/12">
            <CustomInputText
              class="input-layout"
              :inputTextState="{
                modelValue: recommendation.createdAt.split(' ')[0],
                layout: 'row',
                label: '推薦時間:',
                labelClass: 'input-label',
                readonly: true
              }"
            />
            <CustomInputText
              class="input-layout"
              :inputTextState="{
                modelValue: recommendation.userName,
                userId: recommendation.userId,
                layout: 'row',
                label: '推薦用戶:',
                labelClass: 'input-label',
                readonly: true
              }"
            />
            <CustomInputText
              class="input-layout"
              :inputTextState="{
                modelValue: recommendation.storeName,
                storeId: recommendation.storeId,
                layout: 'row',
                label: '店家名稱:',
                labelClass: 'input-label',
                readonly: true
              }"
            />
            <CustomInputText
              class="input-layout"
              :inputTextState="{
                modelValue: recommendation.storeAddress,
                layout: 'row',
                label: '店家地址:',
                labelClass: 'input-label',
                readonly: true
              }"
            />
            <div class="input-layout component-row">
              <p class="labelText input-label">店家評分:</p>
              <div class="store-star !relative !top-0 !left-0 py-2 md:px-2">
                <font-awesome-icon
                  v-for="sc in Math.floor(recommendation.score || 0)"
                  :key="'solid-' + recommendation.id + '-' + sc"
                  icon="fa-solid fa-star"
                />
                <font-awesome-icon
                  v-for="sc in 5 - Math.ceil(recommendation.score || 0)"
                  :key="'regular-' + recommendation.id + '-' + sc"
                  icon="fa-regular fa-star"
                />
              </div>
            </div>
            <div v-if="displayedCategories" class="input-layout component-row">
              <p class="labelText input-label">用餐時段:</p>
              <p class="text-content">{{ displayedCategories }}</p>
              <!-- <div class="flex flex-wrap">
                <CustomCheckbox
                  v-for="category in categories?.slice(11, 16) || []"
                  :key="category.id"
                  class="mr-3"
                  :checkboxState="{
                    modelValue: selectedCategories.includes(category.categoryName),
                    name: category.categoryName,
                    id: category.id
                  }"
                />
              </div> -->
            </div>
            <div v-if="displayedCategoriesTypes" class="input-layout component-row">
              <p class="labelText input-label">餐廳類型:</p>
              <p class="text-content">{{ displayedCategoriesTypes }}</p>
              <!-- <div class="grid grid-cols-2 w-full">
                <CustomCheckbox
                  v-for="category in categories?.slice(0, 11) || []"
                  :key="category.id"
                  class="mr-3"
                  :checkboxState="{
                    modelValue: selectedCategories.includes(category.categoryName),
                    name: category.categoryName,
                    id: category.id
                  }"
                />
              </div> -->
            </div>
            <div class="input-layout component-row">
              <p class="labelText input-label">推薦原因:</p>
              <p class="text-content text-justify">{{ recommendation.reason }}</p>
            </div>
            <!-- <CustomTextarea
              :textareaState="{
                modelValue: recommendation.reason,
                label: '推薦原因:',
                readonly: true
              }"
            /> -->
          </div>
        </div>
        <p v-else class="text-center">找不到該推薦資訊。</p>
      </ScrollBar>
    </div>
  </SearchLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import SearchLayout from '@/components/layout/SearchLayout.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomButton from '@/components/CustomButton.vue'
import { getRecommendationByUserIdAndStoreId as fetchRecommendationDetail } from '@/services/recommendService'
import type { RecommendItem } from '@/types/recommend'
import type { Categories } from '@/types/categories'
import { getCategories } from '@/services/categoryService'

const route = useRoute()
const recommendation = ref<RecommendItem | null>(null)
const loading = ref(true)
const error = ref<Error | null>(null)

const currentIndex = ref(0)
const categories = ref<Categories[] | null>(null)
const selectedCategories = ref<string[]>([])

const displayedCategories = computed(() => {
  const filteredCategories = categories.value
    ?.slice(11, 16)
    .filter((category) => selectedCategories.value.includes(category.categoryName))
  return filteredCategories?.map((category) => category.categoryName).join('、')
})
const displayedCategoriesTypes = computed(() => {
  const filteredCategoriesTypes = categories.value
    ?.slice(0, 11)
    .filter((category) => selectedCategories.value.includes(category.categoryName))
  return filteredCategoriesTypes?.map((category) => category.categoryName).join('、')
})

const goToImage = (index: number) => {
  if (
    recommendation.value?.photoUrls &&
    index >= 0 &&
    index < recommendation.value.photoUrls.length
  ) {
    currentIndex.value = index
  }
}

const nextImage = () => {
  if (recommendation.value?.photoUrls && recommendation.value.photoUrls.length > 1) {
    currentIndex.value = (currentIndex.value + 1) % recommendation.value.photoUrls.length
  }
}

const prevImage = () => {
  if (recommendation.value?.photoUrls && recommendation.value.photoUrls.length > 1) {
    currentIndex.value =
      (currentIndex.value - 1 + recommendation.value.photoUrls.length) %
      recommendation.value.photoUrls.length
  }
}

// 分類列表
const fetchCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (err: any) {
    categories.value = []
    console.error('Error fetching categories:', err)
  }
}

const fetchRecommendation = async () => {
  try {
    const userId = route.params.userId
    const storeId = route.params.storeId
    if (typeof userId === 'string' && typeof storeId === 'string') {
      const response = await fetchRecommendationDetail(Number(userId), Number(storeId))
      if (response) {
        recommendation.value = response
        selectedCategories.value = response.categoryNames || []
      } else {
        recommendation.value = null
        selectedCategories.value = []
      }
    }
  } catch (err: any) {
    error.value = err
    console.error('Error fetching recommendation detail:', err)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchRecommendation()])
})
</script>

<style lang="scss">
.input {
  &-label {
    @apply min-w-[60px] m-0 font-semibold md:mx-2;
    &.labelText {
      @apply sm:py-1.5;
    }
  }
  &-layout {
    &:not(&:last-of-type) {
      @apply border-b border-gray-200;
    }
    &.component-row,
    & > .component-row {
      @apply flex-col items-baseline gap-1 md:flex-row md:gap-2.5;
      .inputDivReadonly {
        @apply px-0 md:px-2;
      }
      .text-content {
        @apply text-xs py-1 leading-[150%] sm:text-sm md:px-2;
      }
    }
  }
}
</style>
