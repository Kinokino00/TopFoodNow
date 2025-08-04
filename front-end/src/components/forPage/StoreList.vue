<template>
  <div class="store-container items-center">
    <div class="store-image min-w-[139px] max-w-[139px]">
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
      <img
        v-if="itemData.photoUrls && itemData.photoUrls.length"
        :src="itemData.photoUrls[0]"
        :alt="itemData.storeName"
      />
      <font-awesome-icon v-else icon="fa-solid fa-shop" class="icon" />
    </div>

    <div class="grid gap-1 p-2">
      <h3 class="store-name">
        {{ itemData.storeName }}
      </h3>

      <div class="flex flex-wrap gap-1">
        <div
          v-for="cat in itemData.categoryNames?.slice(0, 3) || []"
          :key="cat"
          class="store-category"
        >
          {{ cat }}
        </div>
      </div>

      <p class="text-more-line">{{ itemData.reason }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface GenericStoreCardData {
  id: string | number
  createdAt?: string
  score?: number
  photoUrls?: string | null
  storeName?: string
  categoryNames?: string[]
  reason?: string
}

const props = defineProps<{
  itemData: GenericStoreCardData
}>()
</script>
