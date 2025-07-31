<template>
  <div class="header flex items-center justify-center top-5 left-5 w-7 h-7 p-1 bg-white rounded shadow-gray sm:hidden" @click.stop="isOpen = !isOpen">
    <font-awesome-icon icon="fa-solid fa-bars" class="text-lg text-primary-500" />
  </div>
  
  <div class="header hidden justify-between p-5 w-full cursor-pointer sm:flex md:p-7">
    <img src="@/assets/images/logo.svg" alt="logo" class="w-[90px] md:w-[102px]" style="filter: drop-shadow(0 0 4px white) drop-shadow(0 0 4px white) drop-shadow(0 0 4px white);"/>
    <button class="flex items-center gap-1.5 h-fit" @click.stop="isOpen = !isOpen">
      <p class="text-sm md:text-base" style="text-shadow: 0 0 8px black, 0 0 8px black;">userName</p>
      <font-awesome-icon
        icon="fa-solid fa-caret-down"
        style="filter: drop-shadow(0 0 4px black) drop-shadow(0 0 4px black);"
      />
    </button>
  </div>

  <ul v-if="isOpen" class="options">
    <li>個人推薦總覽</li>
    <li>登出</li>
  </ul>
</template>


<script setup lang="ts">
import { ref } from 'vue'

const isOpen = ref(false)
</script>

<style lang="scss" scoped>
.header, .options {
  @apply fixed z-[5000];
}
.header {
  @apply text-white;
}
.options {
  @apply top-[3.25rem] left-5 p-1.5 w-fit bg-white text-gray-600 rounded shadow-gray sm:top-16 sm:left-auto sm:right-6 sm:p-2;
  li {
    @apply py-1.5 px-2 cursor-pointer;
    &:hover {
      @apply bg-primary-100 rounded;
    }
  }
}
</style>
