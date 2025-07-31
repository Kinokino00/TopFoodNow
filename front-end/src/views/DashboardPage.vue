<template>
  <ScrollBar v-if="isReady" class="max-h-screen">
    <div class="bg-right"></div>
    
    <!-- logo + input -->
    <div class="container mt-[16rem]">
      <div class="flex flex-col gap-4 w-full md:w-8/12 xl:w-6/12">
        <img src="@/assets/images/logo.svg" alt="logo" class="w-28 mx-auto max-w-[260px] sm:w-[24vw] md:w-[22vw]" />
        <div class="relative flex items-center w-full">
          <CustomInputText
            class="w-full"
            :inputTextState="{
              modelValue: '',
              placeholder: '搜尋',
              inputDivClass: '!border-primary-500 !rounded-full',
              inputClass: 'relative !text-xs sm:!text-sm sm:!-top-px xl:!text-base',
            }"
          />
          <CustomButton
            class="absolute right-[3px] top-[3px] !rounded-full sm:top-1 sm:right-1 sm:!py-[3px] sm:!px-2"
            :buttonState="{
              color: 'primary',
              icon: 'fa-solid fa-magnifying-glass',
              iconClass: 'relative sm:-top-px',
            }"
            @click="console.log('Button clicked')"
          />
        </div>
        <div class="flex item-center gap-2">
          <div v-for="category in filteredCategories" :key="category.id" class="tag">{{ category.name }}</div>
        </div>
      </div>
    </div>

    <!-- stores -->
    <div class="relative bottom-0 mt-24 md:mt-32">
      <div class="container items-center gap-4 min-h-[46vh] pb-16">
        <div class="flex items-center gap-2 w-9/12 md:w-7/12">
          <div class="line"></div>
          <p class="whitespace-nowrap sm:text-sm md:text-base xl:text-lg">最新餐廳推薦</p>
          <div class="line"></div>
        </div>
        <div class="flex flex-col gap-4 w-full md:flex-row md:gap-6">
          <div v-for="(store, i) in stores" :key="store.id" class="store-container" :class="{ 'mt-7': i === 1 }">
            <div class="store-image">
              <img v-if="store.photoUrl" :src="store.photoUrl" alt="store" />
              <font-awesome-icon v-else icon="fa-solid fa-shop" class="icon" />
            </div>
            <h3 class="store-name">{{ store.name }}</h3>
            <div class="store-star">
              <font-awesome-icon v-for="n in Math.floor(store.averageScore)" :key="'solid-' + store.id + '-' + n" icon="fa-solid fa-star" />
              <font-awesome-icon v-for="n in (5 - Math.ceil(store.averageScore))" :key="'regular-' + store.id + '-' + n" icon="fa-regular fa-star" />
            </div>
          </div>
        </div>
        <CustomButton
          class="py-1.5 mt-3 !rounded-full"
          :buttonState="{
            labelClass: 'text-sm',
            label: '查看更多',
            color: 'primary',
            iconRight: 'fa-solid fa-arrow-right',
            iconRightClass: 'ml-1 text-sm',
          }"
          @click="console.log('Button clicked')"
        />
      </div>
      <div class="bg-left"></div>
    </div>
  </ScrollBar>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue';
import ScrollBar from '@/components/scrollBar/ScrollBar.vue'
import CustomInputText from '@/components/CustomInputText.vue'
import CustomButton from '@/components/CustomButton.vue'
import type { Store } from '@/types/store'
import { getStoresRandomly } from '@/services/storeService'
import type { Categories } from '@/types/categories'
import { getCategories } from '@/services/categoryService'


const isReady = ref(false)
const stores = ref<Store[]>([])
const categories = ref<Categories[]>([])

function getScreenSize() {
  if (window.innerWidth < 576) return 'sm';
  if (window.innerWidth < 992) return 'lg';
  if (window.innerWidth < 1200) return 'xl';
  return '2xl';
}

const screenSize = ref(getScreenSize());

window.addEventListener('resize', () => {
  screenSize.value = getScreenSize();
});

const filteredCategories = computed(() => {
  let count = 3;
  if (screenSize.value === 'lg') count = 5;
  else if (screenSize.value === 'xl' || screenSize.value === '2xl') count = 6;
  return categories.value.slice(0, count);
});

const fetchStores = async () => {
  try {
    const data = await getStoresRandomly(3)
    stores.value = data;
  } catch (err: any) {
    stores.value = []
    console.error('Error fetching stores:', err)
  }
}

const fetchCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (err: any) {
    categories.value = []
    console.error('Error fetching categories:', err)
  }
}

const fetchAll = async () => {
  await Promise.all([fetchStores(), fetchCategories()])
  isReady.value = true
}

onMounted(() => {
  fetchAll()
})
</script>

<style lang="scss">
.container {
  @apply flex flex-col mx-auto w-10/12 sm:w-8/12 md:w-10/12;
}
.bg-right {
  @apply absolute top-0 right-0 w-[276px] h-[484px] bg-cover bg-no-repeat bg-[url('@/assets/images/bg-index-sm.png')];
  @apply  sm:w-[370px]  sm:h-[650px];
  @apply  md:w-[468px]  md:h-[820px];
  @apply  lg:w-[532px]  lg:h-[930px];
  @apply  xl:w-[576px]  xl:h-[1010px];
  @apply 2xl:w-[646px] 2xl:h-[1130px];
}
.tag {
  @apply py-1 px-2 bg-primary-500 text-xs text-white whitespace-nowrap rounded-full xl:text-sm;
}

// stores
.line {
  @apply w-full h-px bg-gray-400;
}
.store {
  &-container {
    @apply flex flex-col gap-3 w-full h-fit pb-3 bg-white rounded-xl overflow-hidden shadow-[0_0_4px_rgba(0,0,0,.25)];
  }
  &-image {
    @apply relative bg-gray-200 aspect-[4/3] overflow-hidden;
    .icon {
      @apply absolute inset-0 m-auto text-2xl text-gray-400;
    }
    &>img {
      @apply w-full h-full object-cover;
    }
  }
  &-name {
    @apply text-xs text-center font-semibold text-gray-800 sm:text-sm xl:text-base;
  }
  &-star {
    @apply flex items-center gap-0.5 justify-center text-star text-xxs sm:text-xs xl:text-sm;
  }
}

.bg-left {
  @apply absolute -z-[1] bottom-0 left-0 w-[130px] h-[196px] bg-cover bg-no-repeat bg-[url('@/assets/images/bg-left.png')];
  @apply  sm:w-[180px]  sm:h-[270px];
  @apply  lg:w-[210px]  lg:h-[310px];
  @apply  xl:w-[234px]  xl:h-[350px];
  @apply 2xl:w-[260px] 2xl:h-[390px];
}
</style>
