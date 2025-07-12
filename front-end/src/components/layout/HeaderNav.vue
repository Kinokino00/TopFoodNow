<template>
  <div>
    <button class="p-3 text-white rounded-lg" @click="toggleTheme">toggleTheme</button>
  </div>
</template>


<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const isDarkMode = ref(false)

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
}

watch(isDarkMode, (newValue) => {
  const themeStyle = document.getElementById('theme-style')
  themeStyle?.setAttribute('href', newValue ? '../src/assets/style/variables/dark.css' : '../src/assets/style/variables/light.css')
})
onMounted(() => {
  const link = document.createElement('link')
  link.id = 'theme-style'
  link.rel = 'stylesheet'
  link.href = '../src/assets/style/variables/light.css'
  document.head.appendChild(link)
})
</script>
