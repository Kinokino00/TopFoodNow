<template>
  <SearchLayout>
    <div class="area pr-3 pb-1 md:w-4/12 xl:w-3/12">
      <div class="relative flex items-center w-full">
        <CustomInputText
          class="w-full"
          :inputTextState="inputTextState"
          @update:modelValue="(value) => (searchQuery = value)"
          @keyup.enter="handleSearch"
        />
        <CustomButton
          class="absolute right-[3px] top-[3px] !py-0.5 !px-[5px] !rounded-full md:top-1 md:right-1 sm:!py-0.5 sm:!px-1.5 md:!py-px md:!px-[7px]"
          :buttonState="{
            color: 'primary',
            icon: 'fa-solid fa-magnifying-glass',
            iconClass: 'relative -top-px !text-xs md:-top-0.5'
          }"
          :disabled="loading"
          @click="handleSearch"
        />
      </div>
      <div
        v-if="categories.length > 0"
        class="grid-cols-2 mt-2 md:grid"
        :class="isCategoryOpen ? 'grid' : 'hidden'"
      >
        <CustomCheckbox
          v-for="category in categories.slice(0, 11)"
          :key="category.id"
          :checkboxState="{
            modelValue: selectedCategories.includes(category.categoryName),
            name: category.categoryName,
            id: category.id
          }"
          @update:modelValue="(value) => handleCheckboxChange(value, category.id)"
        />
        <div class="line my-1.5 !bg-gray-100 col-span-2"></div>
        <CustomCheckbox
          v-for="category in categories.slice(11, 16)"
          :key="category.id"
          :checkboxState="{
            modelValue: selectedCategories.includes(category.categoryName),
            name: category.categoryName,
            id: category.id
          }"
          @update:modelValue="(value) => handleCheckboxChange(value, category.id)"
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

    <div class="area gap-3 pr-0.5 pb-3 md:w-8/12 xl:w-9/12">
      <div class="flex items-center gap-2 ml-auto mr-3">
        <CustomDropdown
          v-if="recommendations.length && !loading && !error"
          :dropdownState="sortDropdownState"
        />
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
        class="py-1 pl-1 pr-3 md:max-h-[calc(100vh-212px)]"
        :class="isCategoryOpen ? 'max-h-[calc(100vh-648px)]' : 'max-h-[calc(100vh-308px)]'"
      >
        <p v-if="loading" class="loading">載入推薦中...</p>
        <p v-if="error" class="error">錯誤: {{ error.message || '無法載入推薦列表' }}</p>
        <p v-else-if="!recommendations.length && !loading && !error" class="text-center">
          目前沒有推薦的餐廳
        </p>

        <div
          v-if="recommendations.length && !loading"
          class="grid gap-2"
          :class="{ 'grid-cols-2 xl:grid-cols-3': isGridView }"
        >
          <template v-if="isGridView">
            <StoreCard v-for="item in recommendations" :key="item.id" :itemData="item" />
          </template>
          <template v-else>
            <StoreList v-for="item in recommendations" :key="item.id" :itemData="item" />
          </template>
        </div>
      </ScrollBar>

      <div
        v-if="recommendations.length && !loading && !error"
        class="flex items-center justify-center w-full md:justify-between"
      >
        <div class="hidden items-center gap-1 text-sm md:flex">
          <span>每頁顯示</span>
          <CustomDropdown :dropdownState="pageSizeDropdownState" />
          <span>筆</span>
        </div>
        <div class="pagination">
          <CustomButton
            id="first-page-button"
            class="pagination-button pagination-arrow"
            :buttonState="{
              icon: 'fa-solid fa-angles-left',
              color: 'white'
            }"
            @click="goToPage(1)"
            :disabled="currentPage === 1"
          />
          <CustomButton
            id="previous-page-button"
            class="pagination-button pagination-arrow"
            :buttonState="{
              icon: 'fa-solid fa-angle-left',
              color: 'white'
            }"
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
          />
          <CustomButton
            v-for="page in totalPages"
            :key="page"
            class="pagination-button"
            :buttonState="{
              color: currentPage === page ? 'primary' : 'white',
              label: `${page}`
            }"
            @click="goToPage(page)"
          />
          <CustomButton
            id="next-page-button"
            class="pagination-button pagination-arrow"
            :buttonState="{
              icon: 'fa-solid fa-angle-right',
              color: 'white'
            }"
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage === totalPages"
          />
          <CustomButton
            id="last-page-button"
            class="pagination-button pagination-arrow"
            :buttonState="{
              icon: 'fa-solid fa-angles-right',
              color: 'white'
            }"
            @click="goToPage(totalPages)"
            :disabled="currentPage === totalPages"
          />
        </div>
      </div>
    </div>
  </SearchLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useRoute, useRouter, isNavigationFailure } from 'vue-router'
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import SearchLayout from '@/components/layout/SearchLayout.vue'
import StoreCard from '@/components/forPage/StoreCard.vue'
import StoreList from '@/components/forPage/StoreList.vue'
import CustomCheckbox from '@/components/CustomCheckbox.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomDropdown from '@/components/dropdown/CustomDropdown.vue'
import type { Categories } from '@/types/categories'
import { getCategories } from '@/services/categoryService'
import { getAllRecommendations } from '@/services/recommendService'
import type { RecommendItem } from '@/types/recommend'

const route = useRoute()
const router = useRouter()

const searchQuery = ref((route.query.searchTerm as string) || '')
const currentCategory = ref(searchQuery.value)

const isCategoryOpen = ref(false)
const loading = ref(false)
const error = ref<Error | null>(null)
const isGridView = ref(true)
const recommendations = ref<RecommendItem[]>([])
const categories = ref<Categories[]>([])
const selectedCategories = ref<string[]>([])

const currentPage = ref(Number(route.query.page) || 1)
const totalPages = ref(1)
const totalElements = ref(0)
const pageSize = ref(Number(route.query.size) || 10)

const inputTextState = ref({
  modelValue: searchQuery.value,
  placeholder: '搜尋',
  inputDivClass: '!border-primary-500 !rounded-full hover:!border-primary-900',
  inputClass: 'relative !text-xs sm:!text-sm sm:!-top-px xl:!text-base'
})

const sortOptions = [
  { name: '由新到舊', value: 1 },
  { name: '由舊到新', value: 2 },
  { name: '評分高到低', value: 3 },
  { name: '評分低到高', value: 4 }
]
const sortDropdownState = reactive({
  modelValue: Number(route.query.sortByOption) || sortOptions[0].value,
  options: sortOptions,
  width: 'w-[120px]'
})

const pageSizeDropdownState = reactive({
  modelValue: pageSize.value,
  options: [
    { name: '10', value: 10 },
    { name: '25', value: 25 },
    { name: '50', value: 50 }
  ],
  width: 'w-[60px]'
})

// 獲取分類列表
const fetchCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (err: any) {
    categories.value = []
    console.error('Error fetching categories:', err)
  }
}

const getSortField = (sortByValue: string | number | string[] | number[] | Object | boolean) => {
  switch (sortByValue) {
    case 1: // 由新到舊
    case 2: // 由舊到新
      return 'createdAt'
    case 3: // 評分高到低
    case 4: // 評分低到高
      return 'score'
    default:
      return 'id' // 預設排序
  }
}
const getSortOrder = (sortByValue: string | number | string[] | number[] | Object | boolean) => {
  switch (sortByValue) {
    case 1: // 由新到舊
    case 3: // 評分高到低
      return 'desc'
    case 2: // 由舊到新
    case 4: // 評分低到高
      return 'asc'
    default:
      return 'asc' // 預設升序
  }
}

// 獲取推薦列表的函數
const fetchRecommendations = async () => {
  loading.value = true
  error.value = null

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: getSortField(sortDropdownState.modelValue),
      sortOrder: getSortOrder(sortDropdownState.modelValue),
      searchTerm:
        selectedCategories.value.length > 0
          ? selectedCategories.value.join(' ')
          : searchQuery.value || undefined
    }
    const response = await getAllRecommendations(params)
    recommendations.value = response.data
    totalPages.value = response.totalPages ?? 1
    currentPage.value = response.pageable.pageNumber // 從 API 回應獲取當前頁碼
    pageSize.value = response.pageable.pageSize // 從 API 回應獲取每頁大小
    totalElements.value = response.totalElements ?? 0 // 更新總筆數

    updateUrlQueryParams()
  } catch (err: any) {
    error.value = err
    recommendations.value = []
    totalPages.value = 1 // 錯誤時重置頁數
    currentPage.value = 1
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// 更新 URL 查詢參數
const updateUrlQueryParams = () => {
  router
    .replace({
      query: {
        searchTerm:
          selectedCategories.value.length > 0
            ? selectedCategories.value.join(' ')
            : searchQuery.value || undefined,
        page: currentPage.value,
        sortByOption: sortDropdownState.modelValue,
        size: pageSize.value
      }
    })
    .catch((err) => {
      if (isNavigationFailure(err)) {
        console.warn('Navigation failure:', err)
      } else {
        throw err
      }
    })
}

// Checkbox 狀態改變事件
const handleCheckboxChange = (checked: boolean, categoryId: number) => {
  const category = categories.value.find((cat) => cat.id === categoryId)
  if (!category) return
  if (checked) {
    if (!selectedCategories.value.includes(category.categoryName)) {
      selectedCategories.value.push(category.categoryName)
    }
  } else {
    selectedCategories.value = selectedCategories.value.filter(
      (name) => name !== category.categoryName
    )
  }
  // 更新搜尋框內容
  searchQuery.value = selectedCategories.value.join(' ')
  inputTextState.value.modelValue = searchQuery.value
  currentCategory.value = '' // 取消高亮
  currentPage.value = 1
  fetchRecommendations()
}

// 搜尋按鈕點擊事件 (或輸入框按 Enter)
const handleSearch = () => {
  // 輸入框搜尋時清空所有勾選
  selectedCategories.value = []
  currentCategory.value = ''
  currentPage.value = 1
  fetchRecommendations()
}

// 分頁跳轉
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    fetchRecommendations()
  }
}

// 依據 searchTerm 同步 input 及 checkbox
const syncSearchTermAndCheckbox = (searchTerm: string) => {
  searchQuery.value = searchTerm
  inputTextState.value.modelValue = searchTerm
  if (searchTerm) {
    selectedCategories.value = searchTerm.split(' ').filter(Boolean)
  } else {
    selectedCategories.value = []
  }
  // 重新判斷分類高亮
  const matchedCategory = categories.value.find((cat) => cat.categoryName === searchTerm)
  if (matchedCategory) {
    currentCategory.value = matchedCategory.categoryName
  } else {
    currentCategory.value = ''
  }
}

// 元件掛載時首次載入資料和分類
onMounted(async () => {
  await fetchCategories()
  // 等分類資料載入後再同步勾選
  if (route.query.searchTerm) {
    syncSearchTermAndCheckbox(route.query.searchTerm as string)
  }
  // 初始化時從 URL 讀取排序選項和每頁大小
  sortDropdownState.modelValue = Number(route.query.sortByOption) || sortOptions[0].value
  pageSize.value = Number(route.query.size) || 10
  pageSizeDropdownState.modelValue = pageSize.value
  currentPage.value = Number(route.query.page) || 1
  fetchRecommendations()
})

// 監聽 URL query 參數變化，以便用戶手動修改 URL 也能觸發搜尋
watch(
  () => route.query,
  (newQuery) => {
    const newSearchTerm = (newQuery.searchTerm as string) || ''
    const newPage = Number(newQuery.page) || 1
    const newSortByOption = Number(newQuery.sortByOption) || sortOptions[0].value
    const newPageSize = Number(newQuery.size) || 10

    if (
      searchQuery.value !== newSearchTerm ||
      currentPage.value !== newPage ||
      sortDropdownState.modelValue !== newSortByOption ||
      pageSize.value !== newPageSize
    ) {
      syncSearchTermAndCheckbox(newSearchTerm)
      currentPage.value = newPage
      sortDropdownState.modelValue = newSortByOption
      pageSize.value = newPageSize
      pageSizeDropdownState.modelValue = newPageSize
      fetchRecommendations()
    }
  }
)

watch(
  () => sortDropdownState.modelValue,
  () => {
    currentPage.value = 1
    fetchRecommendations()
  }
)

watch(
  () => pageSizeDropdownState.modelValue,
  (newValue) => {
    pageSize.value = newValue
    currentPage.value = 1
    fetchRecommendations()
  }
)
</script>

<style lang="scss" scoped>
.pagination {
  @apply flex justify-center items-center gap-1 pr-2;
  &-button {
    @apply w-6 h-6 !px-1.5 text-xs !rounded sm:text-sm;
    &.button-white {
      @apply bg-white;
    }
  }
  &-arrow {
    @apply text-xxs md:text-xs;
    &,
    &:hover {
      @apply bg-white;
    }
    &:disabled {
      &,
      &:hover {
        @apply text-gray-200 bg-white;
      }
    }
  }
}
</style>
