<template>
  <div ref="scrollBar">
    <slot></slot>
  </div>
</template>


<script setup lang="ts">
import { ref, onMounted } from 'vue'
const scrollBar = ref<HTMLElement | null>(null)
let { OverlayScrollbars } = OverlayScrollbarsGlobal

onMounted(() => {
  if (scrollBar.value) {
    OverlayScrollbars(scrollBar.value, {
      overflowBehavior: {
        x: 'scroll',
        y: 'scroll'
      }
    })
  }
})
</script>


<style lang="scss">
div[data-overlayscrollbars-viewport~=scrollbarHidden]:has(.dashboard-instant):has(~.os-scrollbar-visible) {
  padding: 0 0.85rem 0 0 !important;
}

div[data-overlayscrollbars-viewport]:has(.buildLayer) {
  @apply rounded-lg;

  ~.os-scrollbar.os-scrollbar-vertical.os-scrollbar-cornerless {
    bottom: 2px !important;
    top: 2px !important;
  }

  ~.os-scrollbar-vertical .os-scrollbar-handle {
    right: 2px !important;
  }
}

.os-scrollbar .os-scrollbar-handle {
  @apply opacity-20 hover:opacity-40;
}
</style>