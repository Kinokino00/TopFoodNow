<template>
  <ScrollBar v-if="isReady" class="max-h-screen">
    <div class="bg-right"></div>

    <div class="container mt-[16rem]">
      <div class="flex flex-col gap-4 w-full md:w-8/12 xl:w-6/12">
        <img
          src="@/assets/images/logo.svg"
          alt="logo"
          class="w-28 mx-auto max-w-[260px] sm:w-[24vw] md:w-[22vw]"
        />
        <div class="relative flex items-center w-full">
          <CustomInputText
            class="w-full"
            :inputTextState="inputTextState"
            @update:modelValue="inputTextState.modelValue = $event"
          />
          <CustomButton
            class="absolute right-[3px] top-[3px] !py-0.5 !px-[5px] !rounded-full md:top-1 md:right-1 sm:!py-0.5 sm:!px-1.5 md:!py-px md:!px-[7px]"
            :buttonState="{
              color: 'primary',
              icon: 'fa-solid fa-magnifying-glass',
              iconClass: 'relative -top-px !text-xs md:-top-0.5'
            }"
            @click="navigateToSearchResults(undefined, true)"
          />
        </div>
        <div class="flex item-center gap-1 sm:gap-2">
          <CustomButton
            v-for="category in filteredCategories"
            :key="category.id"
            class="py-0.5 !px-1.5 !rounded-full sm:!px-2"
            :buttonState="{
              color: 'primary',
              label: category.categoryName,
              labelClass: 'text-xs sm:text-sm'
            }"
            @click="navigateToSearchResults(category.categoryName)"
          />
        </div>
      </div>
    </div>

    <div class="relative bottom-0 mt-24 md:mt-32">
      <div class="container items-center gap-4 min-h-[46vh] pb-16">
        <div class="flex items-center gap-2 w-9/12 md:w-7/12">
          <div class="line"></div>
          <p class="whitespace-nowrap sm:text-sm md:text-base xl:text-lg">最新餐廳推薦</p>
          <div class="line"></div>
        </div>
        <div class="flex flex-col gap-4 w-full md:flex-row md:gap-6">
          <StoreCard v-for="item in stores" :key="item.id" :itemData="item" isShort />
        </div>
        <CustomButton
          class="py-1.5 mt-3 !rounded-full"
          :buttonState="{
            labelClass: 'text-sm',
            label: '查看更多',
            color: 'primary',
            iconRight: 'fa-solid fa-arrow-right',
            iconRightClass: 'ml-1 text-sm'
          }"
          @click="navigateToSearchResults(undefined, true)"
        />
      </div>
      <div class="bg-left"></div>
    </div>
  </ScrollBar>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import StoreCard from '@/components/forPage/StoreCard.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomButton from '@/components/CustomButton.vue'
import type { Store } from '@/types/store'
import { getStoresRandomly } from '@/services/storeService'
import type { Categories } from '@/types/categories'
import { getCategories } from '@/services/categoryService'
import type { InputTextState } from '@/components/CustomInputText.vue'

const router = useRouter()

const inputTextState = ref<InputTextState>({
  modelValue: '',
  placeholder: '搜尋',
  inputDivClass: '!border-primary-500 !rounded-full hover:!border-primary-900',
  inputClass: 'relative !text-xs sm:!text-sm sm:!-top-px xl:!text-base'
})

const isReady = ref(false)
const stores = ref<Store[]>([])
const categories = ref<Categories[]>([])

function getScreenSize() {
  if (window.innerWidth < 576) return 'sm'
  if (window.innerWidth < 992) return 'lg'
  if (window.innerWidth < 1200) return 'xl'
  return '2xl'
}
const screenSize = ref(getScreenSize())

window.addEventListener('resize', () => {
  screenSize.value = getScreenSize()
})

const fetchStores = async () => {
  try {
    const data = await getStoresRandomly(3)
    stores.value = data
  } catch (err: any) {
    stores.value = []
    console.error('Error fetching stores:', err)
  }
}

const filteredCategories = computed(() => {
  let count = 3
  if (screenSize.value === 'lg') count = 5
  else if (screenSize.value === 'xl' || screenSize.value === '2xl') count = 6
  return categories.value.slice(0, count)
})

const fetchCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (err: any) {
    categories.value = []
    console.error('Error fetching categories:', err)
  }
}

/**
 * 導航到搜尋結果頁面
 * @param specificTerm 可選的指定搜尋詞。如果提供，則忽略輸入框內容
 * @param forceNoTerm 如果為 true，則無論如何都不會包含 searchTerm 參數
 */
const navigateToSearchResults = (specificTerm?: string, forceNoTerm: boolean = false) => {
  const queryParams: { searchTerm?: string } = {}

  if (forceNoTerm) {
  } else if (specificTerm !== undefined) {
    queryParams.searchTerm = specificTerm
  } else if (inputTextState.value.modelValue) {
    queryParams.searchTerm = inputTextState.value.modelValue
  }
  router.push({ name: 'searchList', query: queryParams })
}

onMounted(async () => {
  await Promise.all([fetchStores(), fetchCategories()])
  isReady.value = true
})
</script>

<style lang="scss" scoped>
.container {
  @apply flex flex-col mx-auto w-10/12 sm:w-8/12 md:w-10/12;
}
.bg-right {
  @apply absolute -z-[1] top-0 right-0 w-[276px] h-[484px] bg-cover bg-no-repeat bg-[url('@/assets/images/bg-index.png')];
  @apply sm:w-[370px] sm:h-[650px];
  @apply md:w-[468px] md:h-[820px];
  @apply lg:w-[532px] lg:h-[930px];
  @apply xl:w-[576px] xl:h-[1010px];
  @apply 2xl:w-[646px] 2xl:h-[1130px];
}
</style>
