<template>
  <SearchLayout>
    <div
      class="area gap-3 !p-2 sm:mx-auto sm:!p-4 sm:w-[400px] md:!p-3 md:max-w-[730px] md:w-[600px]"
    >
      <h1 v-if="props.title" class="my-5 text-center font-medium text-sm md:mb-7 md:text-xl">
        {{ props.title }}
      </h1>
      <slot />
    </div>
  </SearchLayout>
</template>

<script setup lang="ts">
import SearchLayout from '@/components/layout/SearchLayout.vue'

const props = defineProps({
  title: {
    type: String
  }
})
</script>
