<template>
  <div class="store-container flex-col" :class="props.isShort ? 'gap-3 pb-3' : 'pb-2'">
    <div class="store-image cursor-pointer" @click="goToRecommendationDetail">
      <template v-if="!props.isShort">
        <p class="store-date">
          {{ itemData.createdAt ? itemData.createdAt.split(' ')[0] : '' }}
        </p>
        <div class="store-star" style="filter: drop-shadow(0 0 2px black)">
          <font-awesome-icon
            v-for="sc in Math.floor(itemData.score || 0)"
            :key="'solid-' + itemData.id + '-' + sc"
            icon="fa-solid fa-star"
          />
          <font-awesome-icon
            v-for="sc in 5 - Math.ceil(itemData.score || 0)"
            :key="'regular-' + itemData.id + '-' + sc"
            icon="fa-regular fa-star"
          />
        </div>
      </template>

      <img
        v-if="props.isShort && itemData.photoUrl"
        :src="itemData.photoUrl"
        :alt="itemData.storeName"
      />
      <img
        v-else-if="itemData.photoUrls && itemData.photoUrls.length"
        :src="itemData.photoUrls[0]"
        :alt="itemData.storeName"
      />
      <font-awesome-icon v-else icon="fa-solid fa-shop" class="icon" />
    </div>

    <h3 class="store-name mx-2" :class="props.isShort ? 'text-center' : 'mt-2'">
      <template v-if="props.isStore">{{ itemData.storeName }}</template>
      <RouterLink
        v-else
        :to="{ name: 'storeRecommendations', params: { storeId: itemData.storeId } }"
      >
        {{ itemData.storeName }}
      </RouterLink>
    </h3>

    <template v-if="!props.isShort">
      <div class="flex flex-wrap gap-1 my-1 mx-2">
        <div
          v-for="cat in itemData.categoryNames?.slice(0, 3) || []"
          :key="cat"
          class="store-category"
        >
          {{ cat }}
        </div>
      </div>
      <p class="text-more-line mx-2">{{ itemData.reason }}</p>
    </template>

    <div v-else class="store-star !relative !left-0">
      <font-awesome-icon
        v-for="sc in Math.floor(itemData.score || 0)"
        :key="'solid-' + itemData.id + '-' + sc"
        icon="fa-solid fa-star"
      />
      <font-awesome-icon
        v-for="sc in 5 - Math.ceil(itemData.score || 0)"
        :key="'regular-' + itemData.id + '-' + sc"
        icon="fa-regular fa-star"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface GenericStoreCardData {
  id: string | number
  userId: number
  storeId?: number
  createdAt?: string
  score?: number
  photoUrl?: string | null
  photoUrls?: string[] | null
  storeName?: string
  categoryNames?: string[]
  reason?: string
}

const props = defineProps<{
  itemData: GenericStoreCardData
  isShort?: boolean
  isStore?: boolean
}>()

const router = useRouter()

// 導航到推薦詳細頁面
const goToRecommendationDetail = () => {
  if (props.itemData.userId && props.itemData.storeId) {
    router.push({
      name: 'recommendationDetail',
      params: {
        userId: props.itemData.userId,
        storeId: props.itemData.storeId
      }
    })
  } else {
    console.warn('User ID or Store ID is missing, cannot navigate to detail page.')
  }
}
</script>
