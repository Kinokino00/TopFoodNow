<template>
  <div class="store-container flex-col" :class="props.isShort ? 'gap-3 pb-3' : 'pb-2'">
    <div class="store-image">
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

      <img v-if="itemData.photoUrl" :src="itemData.photoUrl" :alt="itemData.storeName" />
      <font-awesome-icon v-else icon="fa-solid fa-shop" class="icon" />
    </div>

    <h3 class="store-name mx-2" :class="props.isShort ? 'text-center' : 'mt-2'">
      {{ itemData.storeName }}
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

    <div v-else class="store-star">
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
interface GenericStoreCardData {
  id: string | number
  createdAt?: string
  score?: number
  photoUrl?: string | null
  storeName?: string
  categoryNames?: string[]
  reason?: string
}

const props = defineProps<{
  itemData: GenericStoreCardData
  isShort?: boolean
}>()
</script>
