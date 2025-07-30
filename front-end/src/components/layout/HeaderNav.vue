<template>
  <div class="fixed flex items-center justify-center top-5 left-5 w-7 h-7 p-1 bg-white rounded shadow-gray sm:hidden" @click.stop="isOpen = !isOpen">
    <font-awesome-icon icon="fa-solid fa-bars" class="text-lg text-primary-500" />
  </div>
  
  <div class="hidden justify-between p-7 text-white sm:flex" @click.stop="isOpen = !isOpen">
    <img src="@/assets/images/logo.png" alt="logo" class="w-[102px]" />
    <p class="text-lg">
      userName
      <font-awesome-icon icon="fa-solid fa-caret-down" class="ml-1" />
    </p>
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
.options {
  @apply fixed top-[3.25rem] left-5 p-2 w-fit bg-white text-gray-600 rounded shadow-gray md:top-16 md:left-auto md:right-6;
  li {
    @apply py-1.5 px-2 cursor-pointer;
    &:hover {
      @apply bg-primary-100 rounded;
    }
  }
}
</style>
